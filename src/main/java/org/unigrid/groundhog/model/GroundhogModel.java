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

package org.unigrid.groundhog.model;

import java.util.ArrayList;
import java.util.List;

public class GroundhogModel {

	private static GroundhogModel single_instance = null;

	private Boolean testing;
	private String location;
	private String[] legecyInputs;
	private String hedgehogLocation;

	private GroundhogModel() {
		// 
	}

	public static GroundhogModel getInstance() {
		if (single_instance == null) {
			single_instance = new GroundhogModel();
		}

		return single_instance;
	}

	public Boolean getTesting() {
		return this.testing;
	}

	public void setTesting(Boolean bool) {
		this.testing = bool;
	}

	public void setLocation(String loc) {
		this.location = loc;
	}

	public String getLocation() {
		return this.location;
	}
	
	public void setHedgehogLocation(String loc) {
		this.hedgehogLocation = loc;
	}
	
	public String getHedgehogLocation() {
		return hedgehogLocation;
	}
	
	public String[] getLegecyInputs () {
		return this.legecyInputs;
	}
	
	public void setLegecyInputs (String[] args) {
		
		legecyInputs = new String[args.length];
		
		for (int i = 0; i < args.length; i++) {
			legecyInputs[i] = "-" + args[i];
		}
	}
}
