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
import org.unigrid.groundhog.model.GroundhogModel;
import org.unigrid.groundhog.model.HedgehogModel;
import org.unigrid.groundhog.services.TimerService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "start")
public class Start implements Runnable {
	private final static String DEFAULT_VALUE = "0";
	private final static String DEFAULT_LOCATION = "/usr/local/bin/";

	@Option(names = {"-hedgehogport", "--hp"}, description = "Port for hedgehog", defaultValue = DEFAULT_VALUE)
	private int hedgehogPort;

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
		hedgehog.startHedgehog();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				hedgehog.stopHedgehog();
			}
		}));

		TimerService timer = new TimerService();
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
