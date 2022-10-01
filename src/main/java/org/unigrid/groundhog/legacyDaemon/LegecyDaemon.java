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
package org.unigrid.groundhog.legacyDaemon;

import java.io.IOException;

import org.unigrid.groundhog.model.GroundhogModel;

public class LegecyDaemon {

	private int port = 0;

	private String testingStartCommand = System.getProperty("user.home") + "/.unigrid/dependencies/bin/unigridd";
	private String startCommand = "/home/unigrid/.local/bin/unigridd";
	private String[] startArgs = {"-daemon", "-server", "port="};
	private String testCli = System.getProperty("user.home") + "/.unigrid/dependencies/bin/unigrid-cli";
	private String cli = "/home/unigrid/.local/bin/unigrid-cli";
	private String stop = "stop";

	public LegecyDaemon() {
		/* Empty on purpuse */
		
	}
	
	public LegecyDaemon(int port) {
		this.port = port;
		System.out.println(port);
	}

	public void stopDaemon() {
		try {
			Process p = new ProcessBuilder().command(cli, stop).start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void startDaemon() {

		Boolean isTesting = GroundhogModel.getInstance().getTesting();

		try {
			ProcessBuilder pb = new ProcessBuilder();
			if(port > 0) {
				pb.command(isTesting? testingStartCommand : startCommand, startArgs[0], startArgs[1], startArgs[2] + port);
			} else {
				pb.command(isTesting? testingStartCommand : startCommand, startArgs[0], startArgs[1]);
			}
			System.out.println(isTesting? testingStartCommand : startCommand);
			Process p = pb.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
