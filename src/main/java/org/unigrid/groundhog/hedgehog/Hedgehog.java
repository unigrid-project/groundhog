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

package org.unigrid.groundhog.hedgehog;

import java.io.IOException;
import org.unigrid.groundhog.model.GroundhogModel;
import org.unigrid.groundhog.model.HedgehogModel;

public class Hedgehog {
	private String testHedgehogStart = "";
	private String hedgehogStart = "java -jar /home/unigrid/.local/bin/hedgehog.jar";
	private String java = "java";
	private String jar = "-jar";
	private String arg = "daemon";
	private String module = "--add-opens java.base/java.lang=ALL-UNNAMED";
	private String testHedgehogStop = "";
	private String hedgehogStop = "";
	
	public void startHedgehog() {
		try {
			String path = GroundhogModel.getInstance().getHedgehogLocation();
			System.out.println("hedgehog start command" + java + jar + path + arg);
			Process p = new ProcessBuilder().command(java, module, jar, path, arg).start();
			HedgehogModel.getInstance().setProcess(p);
			p.isAlive();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void stopHedgehog() {
		try {
			String path = GroundhogModel.getInstance().getHedgehogLocation();
			Process p = new ProcessBuilder().command(java, module, jar, path, "cli", "stop").start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
