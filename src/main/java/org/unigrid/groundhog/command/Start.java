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

import java.util.List;
import org.unigrid.groundhog.hedgehog.Hedgehog;
import org.unigrid.groundhog.legacyDaemon.LegecyDaemon;
import org.unigrid.groundhog.model.GroundhogModel;
import org.unigrid.groundhog.services.TimerService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "start")
public class Start implements Runnable {
	private final static String DEFAULT_VALUE = "0";
	private final static String DEFAULT_LOCATION = "/usr/local/bin/";

	@Option(names = {"-hedgehogport", "--hp"}, description = "Port for hedgehog", defaultValue = DEFAULT_VALUE)
	private int hedgehogPort;

	@Option(names = {"-daemonport", "--dp"}, description = "Port for legecy daemon", defaultValue = DEFAULT_VALUE)
	private int legecyDaemonPort;

	@Option(names = {"-t", "--testing"},
		description = "Set the environment for testing or production. Default value is false.",
		defaultValue = "false")
	public void appState(Boolean bool) {
		System.out.printf("testing: %s\n", bool);
		GroundhogModel.getInstance().setTesting(bool);
	}

	@Option(names = {"-ll", "--legecylocation"}, description = "Set the daemon location for custom users names.",
		defaultValue = "")
	public void appLocation(String loc) {
		System.out.printf("legecy location: %s\n", loc);
		GroundhogModel.getInstance().setLocation(loc);
	}
	
	@Option(names = {"-hl", "--hedgehoglocation"}, description = "Set the hedgehog location for custom users names.",
		defaultValue = "")
	public void hedgehogLocation(String loc) {
		if(loc.equals("")) {
			loc = DEFAULT_LOCATION;
		}
		System.out.printf("hedgehog location: %s\n", loc);
		GroundhogModel.getInstance().setHedgehogLocation(loc);
	}
	
	@Option(names = {"-li", "--legecyinputs"},
		description = "Start inputs to the legecy daemon, args added without -. (testnet)")
	public String[] legecyInputs;

	private LegecyDaemon daemon;

	@Override
	public void run() {
		
		if(GroundhogModel.getInstance().getHedgehogLocation() == null) {
			GroundhogModel.getInstance().setHedgehogLocation(DEFAULT_VALUE +
				"hedgehog-1.0.0-SNAPSHOT-jar-with-dependencies.jar");
		}
		
		if (legecyInputs != null) {
			GroundhogModel.getInstance().setLegecyInputs(legecyInputs);
		}
		
		if (legecyDaemonPort == 0) {
			daemon = new LegecyDaemon();
		} else {
			daemon = new LegecyDaemon(legecyDaemonPort);
		}


		Hedgehog hedgehog = new Hedgehog();
		hedgehog.startHedgehog();

		daemon.startDaemon();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				daemon.stopDaemon();
				hedgehog.stopHedgehog();
			}
		}));

		TimerService timer = new TimerService();
		timer.pollLegecyDaemon();
		
		TimerService hedgehogTimer = new TimerService();
		timer.pollHedgehog();
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
}
