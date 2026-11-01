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

import java.util.TimerTask;
import org.unigrid.groundhog.model.HedgehogModel;

public class HedgehogMonitor extends TimerTask {

	/* An exception out of a timer task would cancel the timer, and the update check shares it. */
	@Override
	public void run() {
		try {
			check();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	private void check() {
		Process process = HedgehogModel.getInstance().getProcess();

		if (process == null) {
			return;
		}

		if (!process.isAlive()) {
			Hedgehog h = new Hedgehog();
			h.stopHedgehog();
			h.startHedgehog();
		} else {
			System.out.println("hedgehog is alive");
		}
	}
	
}
