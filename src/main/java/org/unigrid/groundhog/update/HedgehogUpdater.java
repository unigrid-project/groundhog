/*
	Groundhog
	Copyright © 2021-2022 The Unigrid Foundation, UGD Software AB

	This program is free software: you can redistribute it and/or modify it under the terms of the
	addended GNU Affero General Public License as published by the Free Software Foundation, version 3
	of the License (see COPYING and COPYING.addendum).

	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
	even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU Affero General Public License for more details.

	You should have received an addended copy of the GNU Affero General Public License with this program.
	If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/janus-java>.
 */

package org.unigrid.groundhog.update;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.unigrid.groundhog.hedgehog.HedgehogNode;
import org.unigrid.groundhog.model.GroundhogModel;

/**
 * Keeps the Hedgehog executable on the newest stable release. What arrives is used only when it is the
 * file the signed SHA256SUMS lists and is also signed by the Unigrid Foundation: the checksum says which
 * release it is, the signature says who made it.
 */
public class HedgehogUpdater {
	public static final URI RELEASES = URI.create("https://github.com/unigrid-project/hedgehog/releases/");

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
	private static final Duration VERSION_TIMEOUT = Duration.ofSeconds(60);
	private static final String SUMS = "SHA256SUMS";
	private static final String SIGNATURE = ".asc";
	private static final String PARTIAL = ".part";
	private static final int OK = 200;

	private final HedgehogNode node;
	private final Path binary;
	private final URI releases;
	private final ReleaseKey key;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT)
		.followRedirects(HttpClient.Redirect.NORMAL).build();

	/* The checksum of an installed executable found newer than the release, so it is asked for its version once. */
	private String newerChecksum = "";

	public HedgehogUpdater(final HedgehogNode node) {
		this(node, GroundhogModel.getInstance().getHedgehogBinary(), RELEASES, new ReleaseKey());
	}

	HedgehogUpdater(final HedgehogNode node, final Path binary, final URI releases, final ReleaseKey key) {
		this.node = node;
		this.binary = binary;
		this.releases = releases;
		this.key = key;
	}

	/** Returns false when the check failed, which leaves the installed executable and the node as they were. */
	public boolean update() {
		if (!supportedPlatform()) {
			System.out.println("Hedgehog is released only for Linux x86_64, so it is not updated");
			return true;
		}

		try {
			final LatestRelease latest = latest();

			if (!isCurrent(latest)) {
				install(latest);
			}

			return true;
		} catch (IOException | IllegalStateException e) {
			System.out.printf("Hedgehog was not updated: %s\n", e.getMessage());
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static boolean supportedPlatform() {
		final String arch = System.getProperty("os.arch");
		return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")
			&& (arch.equals("amd64") || arch.equals("x86_64"));
	}

	private LatestRelease latest() throws IOException, InterruptedException {
		final byte[] sums = fetch(releases.resolve("latest/download/" + SUMS));

		if (!key.verify(sums, fetch(releases.resolve("latest/download/" + SUMS + SIGNATURE)))) {
			throw new IllegalStateException(SUMS + " is not signed by the Unigrid Foundation");
		}

		return LatestRelease.parse(new String(sums, StandardCharsets.UTF_8)).orElseThrow(
			() -> new IllegalStateException(SUMS + " lists no Linux executable of Hedgehog")
		);
	}

	private boolean isCurrent(final LatestRelease latest) throws IOException {
		if (Files.notExists(binary)) {
			return false;
		}

		final String installed = sha256(binary);

		if (installed.equals(latest.sha256())) {
			System.out.printf("Hedgehog %s is up to date\n", latest.version());
			return true;
		}

		if (installed.equals(newerChecksum)) {
			return true;
		}

		if (installedVersion().filter(version -> version.compareTo(latest.version()) > 0).isPresent()) {
			System.out.printf("The installed Hedgehog is newer than the release %s, so it is kept\n", latest.version());
			newerChecksum = installed;
			return true;
		}

		return false;
	}

	/* Going back a version can make Hedgehog set its sporks aside, so a newer executable is never replaced. */
	private Optional<HedgehogVersion> installedVersion() {
		try {
			final Process process = new ProcessBuilder(binary.toString(), "--version")
				.redirectErrorStream(true).start();

			if (!process.waitFor(VERSION_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
				process.destroyForcibly();
				return Optional.empty();
			}

			return HedgehogVersion.parse(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			return Optional.empty();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

	private void install(final LatestRelease latest) throws IOException, InterruptedException {
		final URI asset = releases.resolve("download/v" + latest.version() + "/" + latest.asset());
		final Path partial = binary.resolveSibling(binary.getFileName() + PARTIAL);

		try {
			Files.createDirectories(binary.getParent());
			System.out.printf("Downloading Hedgehog %s\n", latest.version());
			final byte[] signature = fetch(URI.create(asset + SIGNATURE));

			if (!latest.sha256().equals(download(asset, partial))) {
				throw new IllegalStateException("The downloaded Hedgehog is not the release " + latest.version());
			}

			if (!key.verify(partial, signature)) {
				throw new IllegalStateException("The downloaded Hedgehog is not signed by the Unigrid Foundation");
			}

			replaceBinary(partial);
			System.out.printf("Hedgehog is now %s\n", latest.version());
		} finally {
			Files.deleteIfExists(partial);
		}
	}

	private void replaceBinary(final Path partial) throws IOException {
		final boolean wasRunning = node.isRunning();

		if (wasRunning && !node.stopAndAwait()) {
			throw new IllegalStateException("Hedgehog did not stop, so the update waits for the next check");
		}

		try {
			partial.toFile().setExecutable(true);
			Files.move(partial, binary, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			if (wasRunning) {
				node.startHedgehog();
			}
		}
	}

	private byte[] fetch(final URI source) throws IOException, InterruptedException {
		final HttpResponse<byte[]> response = http.send(request(source), HttpResponse.BodyHandlers.ofByteArray());
		requireOk(response.statusCode(), source);
		return response.body();
	}

	/* Returns the checksum of what was written, worked out on the way so the file is not read again. */
	private String download(final URI source, final Path partial) throws IOException, InterruptedException {
		final HttpResponse<InputStream> response = http.send(request(source), HttpResponse.BodyHandlers.ofInputStream());

		try (InputStream body = response.body()) {
			requireOk(response.statusCode(), source);
			final MessageDigest digest = digest();
			Files.copy(new DigestInputStream(body, digest), partial, StandardCopyOption.REPLACE_EXISTING);
			return HexFormat.of().formatHex(digest.digest());
		}
	}

	private static HttpRequest request(final URI source) {
		return HttpRequest.newBuilder(source).timeout(REQUEST_TIMEOUT).GET().build();
	}

	private static void requireOk(final int status, final URI source) throws IOException {
		if (status != OK) {
			throw new IOException(source + " answered " + status);
		}
	}

	private static String sha256(final Path file) throws IOException {
		final MessageDigest digest = digest();

		try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
			in.transferTo(OutputStream.nullOutputStream());
		}

		return HexFormat.of().formatHex(digest.digest());
	}

	private static MessageDigest digest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("This Java platform offers no SHA-256", e);
		}
	}
}
