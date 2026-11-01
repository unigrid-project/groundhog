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

package org.unigrid.groundhog.services;

import java.util.Timer;
import org.unigrid.groundhog.hedgehog.HedgehogMonitor;
import org.unigrid.groundhog.update.HedgehogUpdater;
import org.unigrid.groundhog.update.UpdateMonitor;

public class TimerService {
	private Timer hedgehogTimer;
	private int liveTime = 120000;

	public TimerService() {
		hedgehogTimer = new Timer(false);
	}

	public void pollHedgehog() {
		hedgehogTimer.scheduleAtFixedRate(new HedgehogMonitor(), 30000, liveTime);
	}

	public void pollUpdates(HedgehogUpdater updater, long intervalMillis) {
		hedgehogTimer.scheduleAtFixedRate(new UpdateMonitor(updater), intervalMillis, intervalMillis);
	}
}
