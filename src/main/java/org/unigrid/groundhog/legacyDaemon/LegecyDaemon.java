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
import java.util.ArrayList;
import java.util.List;

import org.unigrid.groundhog.model.GroundhogModel;

public class LegecyDaemon {

	private int port = 0;
	private boolean isTesting;

	private String testingStartCommand = "";
	private String startCommand;
	private String[] startArgs = {"-daemon", "-server", "port="};
	private String testCli = System.getProperty("user.home") + "/.unigrid/dependencies/bin/unigrid-cli -testnet";
	private String cli;
	private String stop = "stop";

	public LegecyDaemon() {
		System.out.println("legecy location = " + GroundhogModel.getInstance().getLocation());
		cli = GroundhogModel.getInstance().getLocation().concat("unigrid-cli");
		startCommand = GroundhogModel.getInstance().getLocation().concat("unigridd");
	}

	public LegecyDaemon(int port) {
		this.port = port;
		System.out.println(port);
	}

	public void stopDaemon() {
		try {
			if(isTesting) {
				Process p = new ProcessBuilder().command(cli, stop).start();				
			} else {
				Process p = new ProcessBuilder().command(cli, "-testnet", stop).start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void startDaemon() {

		isTesting = GroundhogModel.getInstance().getTesting();
		try {
			String[] args;
			ProcessBuilder pb = new ProcessBuilder();
			if (GroundhogModel.getInstance().getLegecyInputs() != null) {
				args = new String[4 + GroundhogModel.getInstance().getLegecyInputs().length];
				args[0] = startCommand;
				args[1] = startArgs[0];
				args[2] = startArgs[1];
				
				for (int i = 0; i < GroundhogModel.getInstance().getLegecyInputs().length; i++) {
					args[i + 3] = GroundhogModel.getInstance().getLegecyInputs()[i];
				}
			} else {
				args = new String[1];
			}
			
			if(port > 0) {
				if (GroundhogModel.getInstance().getLegecyInputs() != null) {
					args = new String[4 + GroundhogModel.getInstance().getLegecyInputs().length];
					args[0] = startCommand;
					args[1] = startArgs[0];
					args[2] = startArgs[1];
				
					for (int i = 0; i < GroundhogModel.getInstance().getLegecyInputs().length; i++) {
						args[i + 3] = GroundhogModel.getInstance().getLegecyInputs()[i];
					}
					args[args.length - 1] = startArgs[2] + port;
					pb.command(args);
				} else {
					pb.command(isTesting? testingStartCommand : startCommand, startArgs[0], startArgs[1], startArgs[2] + port);					
				}
			} else {
				if (GroundhogModel.getInstance().getLegecyInputs() != null) {
					args = new String[3 + GroundhogModel.getInstance().getLegecyInputs().length];
					args[0] = startCommand;
					args[1] = startArgs[0];
					args[2] = startArgs[1];
				
					for (int i = 0; i < GroundhogModel.getInstance().getLegecyInputs().length; i++) {
						args[i + 3] = GroundhogModel.getInstance().getLegecyInputs()[i];
					}
					for(String arg : args) {
						System.out.println(arg);
					}
					pb.command(args);
				} else {
					System.out.println(String.format("%s %s %s ", isTesting? testingStartCommand : startCommand, startArgs[0], isTesting? "-testnet" : ""));
					//pb.command(isTesting? testingStartCommand : startCommand, startArgs[0], isTesting? "-testnet" : "");
					if(isTesting) {
						pb.command(startCommand, startArgs[0], startArgs[1], "-testnet");
					} else {
						pb.command(startCommand, startArgs[0], startArgs[1]);
					}
				}
			}
			if (GroundhogModel.getInstance().getLegecyInputs() != null) {
				pb.command(GroundhogModel.getInstance().getLegecyInputs());
			}
			System.out.println(isTesting? testingStartCommand : startCommand);
			Process p = pb.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
