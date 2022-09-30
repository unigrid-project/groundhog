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
		"          .:^~~~~~^::..",
		"     .^~7J55PPP5YYP55YYJJJJJ?7!!~~!!!~~^^::::...",
		"  .~J5555YYY55Y5YJPP5Y5555PP5PPPP5PPPP5PP5555YYY??!~:",
		".~?55YY55YJ7777J77JYY5555555PPPPP5P5555PPPPPP5555555YJ~.",
		"J5JJ??777?!!!7777??JYY555555PPPPPPP5PPPP5PPPP555Y5Y5555J^",
		".7J?7!!!!!~!77??JJJJ5555555PPPPPPPPP5PP5PPPPPPP55555555PPJ^",
		"  .^7~!7?77?777??JYJY5555555PGP5PPPPPPPP5P55P5P55P5555PP5P5!",
		"       :!7777?JJJJJY5YY555PPPP55PPPPPPPP5555555PPPP55P5P5PP5?.",
		"         :^!?JJ??JY5YY5555P55555PPPPP5PPP5PPPPPPP5P5555PPPP55J.",
		"           .:~?JJJYYYYYYYY555PP55P55P5PP55PPPPPPP55555555PP55P?",
		"              :!?YYYYJYY5YY5555555PP555PP55PP555555555555555555^",
		"                .?55YY5YJYJYY55P555PPY5P55555555P5555PP555555J~^::.....",
		"                ?P55YYJYJYYYY555PPPP5?YPPPP555P5555PP55P555PPPP5555YYJJJ?7!~^:",
		"                ?GGPJYJJJJJYY55JJYJJY?JY5PPPPP55P555PPP5555GPPPPPPPPPPPPPPPPPP?.",
		"                 ~?Y~7JJJJYY5YJ~::?Y5P5JJY55PP55P5555Y?7~^~J5PPPPGGGGGGGGPPPP57.",
		"                    .^?JJYYYJ!.  ~J!?!7~~??JY5YJJ?7!^:.      .:::^~~7??777?7~^.",
		"                  ~55PPG5J7~.        ^7?JPPPPPJ~:..",
		"                  ~^?7~7:           .J5G575J!:",
		"                                       .    Unigrid Groundhog 0.0.1-SNAPSHOT",   
	}, subcommands = {CLI.class, Start.class}
)
public class Groundhog {

	public static void main(String[] args) {
		System.exit(new CommandLine(Groundhog.class).execute(args));
	}
}
