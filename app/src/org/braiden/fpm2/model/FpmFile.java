package org.braiden.fpm2.model;

/**
 * Copyright (c) 2010 Braiden Kindt
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FpmFile implements DataObject {

	private String fullVersion;
	private String minVersion;
	private String displayVersion;
	private KeyInfo keyInfo;
	private List<LauncherItem> launcherItems = new LinkedList<LauncherItem>();
	private List<PasswordItem> passwordItems = new LinkedList<PasswordItem>();
	
	public String getDisplayVersion() {
		return displayVersion;
	}
	
	public void setDisplayVersion(String displayVersion) {
		this.displayVersion = displayVersion;
	}

	public String getFullVersion() {
		return fullVersion;
	}

	public void setFullVersion(String fullVersion) {
		this.fullVersion = fullVersion;
	}

	public String getMinVersion() {
		return minVersion;
	}

	public void setMinVersion(String minVersion) {
		this.minVersion = minVersion;
	}

	public KeyInfo getKeyInfo() {
		return keyInfo;
	}

	public void setKeyInfo(KeyInfo keyInfo) {
		this.keyInfo = keyInfo;
	}

	public List<LauncherItem> getLauncherItems() {
		return launcherItems;
	}

	public void setLauncherItems(List<LauncherItem> launcher) {
		this.launcherItems = launcher;
	}

	public List<PasswordItem> getPasswordItems() {
		return passwordItems;
	}

	public void setPasswordItems(List<PasswordItem> passwordItems) {
		this.passwordItems = passwordItems;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		FpmFile clone = (FpmFile) super.clone();
		clone.keyInfo = (KeyInfo) keyInfo.clone();
		clone.launcherItems = new ArrayList<LauncherItem>(launcherItems.size());
		clone.passwordItems = new ArrayList<PasswordItem>(passwordItems.size());
		
		for (LauncherItem l : launcherItems) {
			clone.launcherItems.add((LauncherItem) l.clone());
		}
		
		for (PasswordItem p : passwordItems) {
			clone.passwordItems.add((PasswordItem) p.clone());
		}
		
		return clone;
	}
	
	
}
