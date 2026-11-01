/*
	Groundhog
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

package org.unigrid.groundhog.update;

import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A Hedgehog version such as 0.0.8 or 0.0.9-dev.3. A build with a suffix is older than its plain release. */
public record HedgehogVersion(int major, int minor, int patch, String suffix) implements Comparable<HedgehogVersion> {
	private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z0-9.]+))?");
	private static final Pattern TRAILING_NUMBER = Pattern.compile("(\\d+)$");
	private static final Comparator<HedgehogVersion> ORDER = Comparator
		.comparingInt(HedgehogVersion::major)
		.thenComparingInt(HedgehogVersion::minor)
		.thenComparingInt(HedgehogVersion::patch)
		.thenComparing(HedgehogVersion::isRelease)
		.thenComparingInt(HedgehogVersion::build)
		.thenComparing(HedgehogVersion::suffix);

	/** Finds the first version in the text, so the output of --version can be given as it is. */
	public static Optional<HedgehogVersion> parse(final String text) {
		final Matcher matcher = VERSION.matcher(text);

		if (!matcher.find()) {
			return Optional.empty();
		}

		return Optional.of(new HedgehogVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
			Integer.parseInt(matcher.group(3)), matcher.group(4) == null ? "" : matcher.group(4)));
	}

	private boolean isRelease() {
		return suffix.isEmpty();
	}

	private int build() {
		final Matcher matcher = TRAILING_NUMBER.matcher(suffix);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
	}

	@Override
	public int compareTo(final HedgehogVersion other) {
		return ORDER.compare(this, other);
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + patch + (isRelease() ? "" : "-" + suffix);
	}
}
