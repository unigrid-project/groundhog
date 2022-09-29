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

package org.unigrid.groundhog;

import org.unigrid.groundhog.command.Start;
import org.unigrid.groundhog.command.CLI;
import picocli.CommandLine.Command;
import picocli.CommandLine;

@Command(name = "groundhog", mixinStandardHelpOptions = true, version = "Unigrid Groundhog 0.0.1-SNAPSHOT",
	scope = CommandLine.ScopeType.INHERIT, header = {
		"",
		"  Unigrid Groundhog 0.0.1-SNAPSHOT",
		""
	}, subcommands = {CLI.class, Start.class}
)
public class Groundhog {

	public static void main(String[] args) {
		System.exit(new CommandLine(Groundhog.class).execute(args));
	}
}
