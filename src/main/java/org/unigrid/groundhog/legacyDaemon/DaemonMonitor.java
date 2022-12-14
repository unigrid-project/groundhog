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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.unigrid.groundhog.model.GroundhogModel;

public class DaemonMonitor extends TimerTask {

	private String testExec = "/usr/local/bin/";
	private String arg = "getblockcount";
	private String liveExec;

	@Override
	public void run() {
		String message = "";
		boolean isTesting = GroundhogModel.getInstance().getTesting();
		liveExec = GroundhogModel.getInstance().getLocation().concat("unigrid-cli");
		try {
			ProcessBuilder pb = new ProcessBuilder()
				.redirectInput(ProcessBuilder.Redirect.INHERIT)
				.redirectError(ProcessBuilder.Redirect.INHERIT)
				.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			if(isTesting) {
				pb.command(liveExec, "-testnet", arg);				
			} else {
				pb.command(liveExec, arg);
			}
			Process p = pb.start();
			p.waitFor(120, TimeUnit.SECONDS);
			InputStream out = p.getInputStream();
			byte[] arr = out.readAllBytes();
			message = new String(arr, StandardCharsets.UTF_8);
			if(message.equals("-1")) {
				System.out.println("syncing");
			} else {
				System.out.println("blocks: " + message);
			}
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		try {
			Integer.parseInt(message);
		} catch(NumberFormatException e) {
			LegecyDaemon daemon = new LegecyDaemon();
			daemon.startDaemon();
		}
	}
	
}
