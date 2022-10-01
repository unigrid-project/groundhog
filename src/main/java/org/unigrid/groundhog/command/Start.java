/*
	The Janus Wallet
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
package org.unigrid.groundhog.command;

import org.unigrid.groundhog.hedgehog.Hedgehog;
import org.unigrid.groundhog.legacyDaemon.LegecyDaemon;
import org.unigrid.groundhog.model.GroundhogModel;
import org.unigrid.groundhog.services.TimerService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "start")
public class Start implements Runnable {
	private final static String DEFAULT_VALUE = "0";

	@Option(names = {"-hedgehogport", "--hp"}, description = "Port for hedgehog", defaultValue = DEFAULT_VALUE)
	private int hedgehogPort;

	@Option(names = {"-daemonport", "--dp"}, description = "Port for legecy daemon", defaultValue = DEFAULT_VALUE)
	private int legecyDaemonPort;

	@Option(names = {"-t", "--testing"}, description = "Set the environment for testing or production. Default value is false.")
	public void appState(Boolean bool) {
		System.out.println("testing: " + bool);
		GroundhogModel.getInstance().setTesting(bool);
	}

	private LegecyDaemon daemon;

	@Override
	public void run() {
		if (legecyDaemonPort == 0) {
			daemon = new LegecyDaemon();
		} else {
			daemon = new LegecyDaemon(legecyDaemonPort);
		}

		daemon.startDaemon();

		//Hedgehog hedgehog = new Hedgehog();
		//hedgehog.startHedgehog();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				daemon.stopDaemon();
				//hedgehog.stopHedgehog();
			}
		}));

		TimerService timer = new TimerService();
		timer.pollLegecyDaemon();
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
}
