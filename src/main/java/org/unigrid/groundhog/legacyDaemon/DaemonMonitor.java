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

public class DaemonMonitor extends TimerTask {

	private String testExec = System.getProperty("user.home") + "/.unigrid/dependencies/bin/unigrid-cli";
	private String arg = "getblockcount";
	private String liveExec = "unigrid-cli getblockcount";
	
	@Override
	public void run() {
		String message = "";
		try {
			Process p = new ProcessBuilder()
				.redirectInput(ProcessBuilder.Redirect.INHERIT)
				.redirectError(ProcessBuilder.Redirect.INHERIT)
				.redirectOutput(ProcessBuilder.Redirect.INHERIT)
				.command(testExec, arg).start();
			InputStream out = p.getInputStream();
			byte[] arr = out.readAllBytes();
			message = new String(arr, StandardCharsets.UTF_8);
			System.out.println(message);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			Integer.parseInt(message);
		} catch(NumberFormatException e) {
			LegecyDaemon daemon = new LegecyDaemon();
		}
	}
	
}
