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

class HedgehogVersionTest {

	private static HedgehogVersion version(final String text) {
		return HedgehogVersion.parse(text).orElseThrow();
	}

	@Test
	void ordersPlainReleasesNumerically() {
		assertTrue(version("0.0.9").compareTo(version("0.0.8")) > 0);
		assertTrue(version("0.0.10").compareTo(version("0.0.9")) > 0);
		assertTrue(version("0.1.0").compareTo(version("0.0.99")) > 0);
	}

	@Test
	void ordersDevBuildsBetweenReleases() {
		assertTrue(version("0.0.9-dev.3").compareTo(version("0.0.8")) > 0);
		assertTrue(version("0.0.9-dev.3").compareTo(version("0.0.9")) < 0);
		assertTrue(version("0.0.9-dev.10").compareTo(version("0.0.9-dev.9")) > 0);
	}

	@Test
	void readsTheVersionOutOfVersionOutput() {
		assertEquals(version("0.0.8"), version("Unigrid Hedgehog 0.0.8\n"));
		assertEquals("0.0.9-dev.3", version("Unigrid Hedgehog 0.0.9-dev.3").toString());
	}

	@Test
	void garbageHasNoVersion() {
		assertTrue(HedgehogVersion.parse("bash: not found").isEmpty());
	}
}
