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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The Linux executable of the newest release, as the signed SHA256SUMS of that release lists it. */
public record LatestRelease(HedgehogVersion version, String sha256) {
	private static final Pattern LINE = Pattern.compile(
		"^([0-9a-f]{64}) [ *]hedgehog-(\\d+\\.\\d+\\.\\d+(?:-[A-Za-z0-9.]+)?)-x86_64-linux-gnu\\.bin$",
		Pattern.MULTILINE
	);

	public static Optional<LatestRelease> parse(final String sha256Sums) {
		final Matcher matcher = LINE.matcher(sha256Sums);

		return matcher.find()
			? HedgehogVersion.parse(matcher.group(2)).map(version -> new LatestRelease(version, matcher.group(1)))
			: Optional.empty();
	}

	public String asset() {
		return "hedgehog-" + version + "-x86_64-linux-gnu.bin";
	}
}
