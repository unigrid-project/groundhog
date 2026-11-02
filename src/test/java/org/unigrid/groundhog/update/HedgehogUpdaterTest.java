/*
    Unigrid Groundhog
    Copyright © 2021-2026 Stiftelsen The Unigrid Foundation

    Stiftelsen The Unigrid Foundation (org. nr: 802482-2408)

    This program is free software: you can redistribute it and/or modify it under the terms of the
    GNU General Public License as published by the Free Software Foundation, version 3 of the License
    (see LICENSE).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/groundhog>.
 */

package org.unigrid.groundhog.update;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.unigrid.groundhog.hedgehog.HedgehogNode;

class HedgehogUpdaterTest {
	private static final byte[] RELEASE = "the release".getBytes(StandardCharsets.UTF_8);
	private static final byte[] OLD = "an old build".getBytes(StandardCharsets.UTF_8);
	private static final String ASSET = "hedgehog-0.0.8-x86_64-linux-gnu.bin";
	private static final String DOWNLOADS = "/releases/download/v0.0.8/";

	@TempDir
	Path dir;

	private HttpServer server;
	private final Map<String, byte[]> served = new ConcurrentHashMap<>();
	private final List<String> requested = new CopyOnWriteArrayList<>();
	private final FakeNode node = new FakeNode();
	private boolean sumsSigned = true;
	private boolean assetSigned = true;
	private Path binary;

	@BeforeEach
	void start() throws IOException {
		Assumptions.assumeTrue(System.getProperty("os.name").equals("Linux")
			&& System.getProperty("os.arch").matches("amd64|x86_64"));

		binary = dir.resolve("hedgehog.bin");
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			final byte[] body = served.get(exchange.getRequestURI().getPath());
			requested.add(exchange.getRequestURI().getPath());
			exchange.sendResponseHeaders(body == null ? 404 : 200, body == null ? -1 : body.length);

			if (body != null) {
				exchange.getResponseBody().write(body);
			}

			exchange.close();
		});
		server.start();
		publish(sha256(RELEASE));
	}

	@AfterEach
	void stop() {
		if (server != null) {
			server.stop(0);
		}
	}

	private void publish(final String checksum) {
		served.put("/releases/latest/download/SHA256SUMS",
			(checksum + "  " + ASSET + "\n").getBytes(StandardCharsets.UTF_8));
		served.put("/releases/latest/download/SHA256SUMS.asc", new byte[] {1});
		served.put(DOWNLOADS + ASSET, RELEASE);
		served.put(DOWNLOADS + ASSET + ".asc", new byte[] {1});
	}

	private HedgehogUpdater updater() {
		return updater(Duration.ofSeconds(60));
	}

	private HedgehogUpdater updater(final Duration limit) {
		final ReleaseKey key = new ReleaseKey(null) {
			@Override
			public boolean verify(final byte[] data, final byte[] signature) {
				return sumsSigned;
			}

			@Override
			public boolean verify(final Path data, final byte[] signature) {
				return assetSigned;
			}
		};

		return new HedgehogUpdater(node, binary, URI.create("http://localhost:" + server.getAddress().getPort()
			+ "/releases/"), key, limit, limit);
	}

	private static String sha256(final byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private boolean downloaded() {
		return requested.contains(DOWNLOADS + ASSET);
	}

	private boolean leftPartialFile() throws IOException {
		return Files.exists(dir.resolve("hedgehog.bin.part"));
	}

	@Test
	void doesNothingWhenTheInstalledFileIsTheRelease() throws IOException {
		Files.write(binary, RELEASE);

		assertTrue(updater().update());
		assertFalse(downloaded());
		assertEquals(List.of(), node.calls);
	}

	@Test
	void installsAFirstDownloadWithoutTouchingTheNode() throws IOException {
		assertTrue(updater().update());
		assertArrayEquals(RELEASE, Files.readAllBytes(binary));
		assertTrue(Files.isExecutable(binary));
		assertFalse(leftPartialFile());
		assertEquals(List.of(), node.calls);
	}

	@Test
	void replacesAnOldFileAndRestartsARunningNode() throws IOException {
		Files.write(binary, OLD);
		node.running = true;

		assertTrue(updater().update());
		assertArrayEquals(RELEASE, Files.readAllBytes(binary));
		assertEquals(List.of("stop", "start"), node.calls);
		assertFalse(leftPartialFile());
	}

	@Test
	void keepsTheOldFileWhenTheDownloadIsNotTheListedRelease() throws IOException {
		Files.write(binary, OLD);
		publish(sha256("something else".getBytes(StandardCharsets.UTF_8)));

		assertFalse(updater().update());
		assertArrayEquals(OLD, Files.readAllBytes(binary));
		assertFalse(leftPartialFile());
	}

	@Test
	void keepsTheOldFileWhenTheDownloadIsNotSigned() throws IOException {
		Files.write(binary, OLD);
		assetSigned = false;

		assertFalse(updater().update());
		assertArrayEquals(OLD, Files.readAllBytes(binary));
		assertFalse(leftPartialFile());
	}

	@Test
	void ignoresChecksumsThatAreNotSigned() throws IOException {
		Files.write(binary, OLD);
		sumsSigned = false;

		assertFalse(updater().update());
		assertFalse(downloaded());
		assertArrayEquals(OLD, Files.readAllBytes(binary));
	}

	@Test
	void keepsTheOldFileWhenTheServerAnswers404() throws IOException {
		Files.write(binary, OLD);
		served.remove(DOWNLOADS + ASSET);

		assertFalse(updater().update());
		assertArrayEquals(OLD, Files.readAllBytes(binary));
		assertFalse(leftPartialFile());
	}

	@Test
	void keepsTheOldFileWhenTheRunningNodeWillNotStop() throws IOException {
		Files.write(binary, OLD);
		node.running = true;
		node.stops = false;

		assertFalse(updater().update());
		assertArrayEquals(OLD, Files.readAllBytes(binary));
		assertEquals(List.of("stop"), node.calls);
		assertFalse(leftPartialFile());
	}

	@Test
	void neverReplacesAnInstalledFileNewerThanTheRelease() throws IOException {
		final Path runs = dir.resolve("runs");
		final String script = "#!/bin/sh\necho run >> " + runs + "\necho 'Unigrid Hedgehog 9.9.9'\n";
		Files.writeString(binary, script);
		binary.toFile().setExecutable(true);
		final HedgehogUpdater updater = updater();

		assertTrue(updater.update());
		assertTrue(updater.update());
		assertFalse(downloaded());
		assertEquals(script, Files.readString(binary));
		assertEquals(1, Files.readAllLines(runs).size());
	}

	/* Answers with the start of the body and then goes quiet, as a connection that stalls does. */
	private void stall(final String path) {
		server.createContext(path, exchange -> {
			exchange.sendResponseHeaders(200, 1000);
			exchange.getResponseBody().write(new byte[10]);
			exchange.getResponseBody().flush();
			sleep(2000);
			exchange.close();
		});
	}

	private static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Test
	void givesUpOnChecksumsThatStall() throws IOException {
		Files.write(binary, OLD);
		stall("/releases/latest/download/SHA256SUMS");
		final long start = System.nanoTime();

		assertFalse(updater(Duration.ofMillis(500)).update());
		assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 1500);
		assertArrayEquals(OLD, Files.readAllBytes(binary));
	}

	@Test
	void givesUpOnADownloadThatStalls() throws IOException {
		Files.write(binary, OLD);
		stall(DOWNLOADS + ASSET);
		final long start = System.nanoTime();

		assertFalse(updater(Duration.ofMillis(500)).update());
		assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 1500);
		assertArrayEquals(OLD, Files.readAllBytes(binary));
		assertFalse(leftPartialFile());
	}

	@Test
	void refusesChecksumsThatAreTooLarge() throws IOException {
		Files.write(binary, OLD);
		served.put("/releases/latest/download/SHA256SUMS", new byte[100_000]);

		assertFalse(updater().update());
		assertFalse(downloaded());
		assertArrayEquals(OLD, Files.readAllBytes(binary));
	}

	private static final class FakeNode implements HedgehogNode {
		final List<String> calls = new CopyOnWriteArrayList<>();
		boolean running;
		boolean stops = true;

		@Override
		public boolean isRunning() {
			return running;
		}

		@Override
		public boolean stopAndAwait() {
			calls.add("stop");
			return stops;
		}

		@Override
		public void startHedgehog() {
			calls.add("start");
		}
	}
}
