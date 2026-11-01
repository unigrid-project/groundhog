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
package org.unigrid.groundhog.command;

import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.unigrid.groundhog.hedgehog.Hedgehog;
import org.unigrid.groundhog.model.GroundhogModel;
import org.unigrid.groundhog.model.HedgehogModel;
import org.unigrid.groundhog.services.TimerService;
import org.unigrid.groundhog.update.HedgehogUpdater;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "start")
public class Start implements Runnable {
	private final static String DEFAULT_VALUE = "0";
	private final static String DEFAULT_LOCATION = System.getProperty("user.home") + "/.local/bin/";
	private final static long INSTALL_RETRY_SECONDS = 60;

	@Option(names = {"-hedgehogport", "--hp"}, description = "Port for hedgehog", defaultValue = DEFAULT_VALUE)
	private int hedgehogPort;

	@Option(names = {"-ui", "--update-interval"}, defaultValue = "60",
		description = "Minutes between checks for a new Hedgehog release. 0 turns updating off.")
	private long updateMinutes;

	@Option(names = {"-hl", "--hedgehoglocation"}, description = "Set the hedgehog location for custom users names.",
		defaultValue = "")
	public void hedgehogLocation(String loc) {
		if(loc.equals("")) {
			loc = DEFAULT_LOCATION;
		}
		System.out.printf("hedgehog location: %s\n", loc);
		GroundhogModel.getInstance().setHedgehogLocation(loc);
	}
	
	@Override
	public void run() {
		HedgehogModel.getInstance().setP2pPort(hedgehogPort);
		Hedgehog hedgehog = new Hedgehog();
		HedgehogUpdater updater = new HedgehogUpdater(hedgehog);

		if (updateMinutes > 0) {
			awaitInstalled(updater);
		}

		hedgehog.startHedgehog();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				hedgehog.stopHedgehog();
			}
		}));

		TimerService timer = new TimerService();
		timer.pollHedgehog();

		if (updateMinutes > 0) {
			timer.pollUpdates(updater, TimeUnit.MINUTES.toMillis(updateMinutes));
		}

		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	/* Starting a Hedgehog that is not there would fail, so a first install is retried until it works. */
	private void awaitInstalled(HedgehogUpdater updater) {
		while (!updater.update() && Files.notExists(GroundhogModel.getInstance().getHedgehogBinary())) {
			try {
				TimeUnit.SECONDS.sleep(INSTALL_RETRY_SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}
