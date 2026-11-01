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

package org.unigrid.groundhog.update;

import java.util.TimerTask;

public class UpdateMonitor extends TimerTask {
	private final HedgehogUpdater updater;

	public UpdateMonitor(final HedgehogUpdater updater) {
		this.updater = updater;
	}

	/* An exception out of a timer task would cancel the timer, and the liveness check shares it. */
	@Override
	public void run() {
		try {
			updater.update();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
}
