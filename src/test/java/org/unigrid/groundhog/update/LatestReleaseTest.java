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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LatestReleaseTest {
	private static final String LINUX = "a".repeat(64);
	private static final String MACOS = "b".repeat(64);

	private static final String SUMS = LINUX + "  hedgehog-0.0.8-x86_64-linux-gnu.bin\n"
		+ MACOS + "  hedgehog-0.0.8-osx-arm64.bin\n"
		+ "c".repeat(64) + "  hedgehog-0.0.8-jar-with-dependencies.jar\n";

	@Test
	void picksTheLinuxLine() {
		final LatestRelease release = LatestRelease.parse(SUMS).orElseThrow();

		assertEquals(LINUX, release.sha256());
		assertEquals("0.0.8", release.version().toString());
		assertEquals("hedgehog-0.0.8-x86_64-linux-gnu.bin", release.asset());
	}

	@Test
	void missingLinuxLineGivesNothing() {
		assertTrue(LatestRelease.parse(MACOS + "  hedgehog-0.0.8-osx-arm64.bin\n").isEmpty());
	}
}
