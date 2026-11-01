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

package org.unigrid.groundhog.hedgehog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.unigrid.groundhog.model.GroundhogModel;
import org.unigrid.groundhog.model.HedgehogModel;

public class Hedgehog implements HedgehogNode {
	private static final int REST_PORT = 52884;
	private static final Duration STOP_WAIT = Duration.ofSeconds(60);
	private static final Duration KILL_WAIT = Duration.ofSeconds(10);
	private static final long POLL_MILLIS = 500;
	private static final String DAEMON = "daemon";

	@Override
	public void startHedgehog() {
		try {
			int p2pPort = HedgehogModel.getInstance().getP2pPort();
			String path = binary().toString();
			System.out.println("hedgehog start command " + path + " " + DAEMON);
			ProcessBuilder pb = p2pPort == 0
				? new ProcessBuilder().command(path, DAEMON)
				: new ProcessBuilder().command(path, DAEMON, "--netport=" + p2pPort);
			Process p = pb.start();
			HedgehogModel.getInstance().setProcess(p);
			p.waitFor(10, TimeUnit.SECONDS);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public void stopHedgehog() {
		try {
			requestStop().waitFor(30, TimeUnit.SECONDS);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public boolean isRunning() {
		return restPortOpen() || descendants().findAny().isPresent();
	}

	/* The exit code of "cli stop" says nothing, as the launcher ignores that of the node it runs. */
	@Override
	public boolean stopAndAwait() {
		stopHedgehog();

		if (awaitDown(STOP_WAIT)) {
			return true;
		}

		System.out.println("hedgehog did not stop, killing it");
		descendants().forEach(ProcessHandle::destroyForcibly);
		Process launcher = HedgehogModel.getInstance().getProcess();

		if (launcher != null) {
			launcher.destroyForcibly();
		}

		return awaitDown(KILL_WAIT);
	}

	private boolean awaitDown(Duration limit) {
		long deadline = System.nanoTime() + limit.toNanos();

		try {
			while (isRunning()) {
				if (System.nanoTime() > deadline) {
					return false;
				}

				Thread.sleep(POLL_MILLIS);
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		}

		return true;
	}

	private Process requestStop() throws IOException {
		return new ProcessBuilder().command(binary().toString(), "cli", "stop").start();
	}

	private static Stream<ProcessHandle> descendants() {
		Process launcher = HedgehogModel.getInstance().getProcess();
		return launcher == null ? Stream.empty() : Stream.concat(Stream.of(launcher.toHandle()), launcher.descendants())
			.filter(ProcessHandle::isAlive);
	}

	private static boolean restPortOpen() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("localhost", REST_PORT), 1000);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	private static Path binary() {
		return GroundhogModel.getInstance().getHedgehogBinary();
	}
}
