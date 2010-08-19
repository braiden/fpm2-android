package org.braiden.fpm2.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Fpm implements Cloneable {

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
		Fpm clone = (Fpm) super.clone();
		clone.keyInfo = (KeyInfo) keyInfo.clone();
		clone.launcherItems = new ArrayList<LauncherItem>(launcherItems.size());
		clone.passwordItems = new ArrayList<PasswordItem>(passwordItems.size());
		
		for (LauncherItem l : launcherItems)
		{
			clone.launcherItems.add((LauncherItem) l.clone());
		}
		
		for (PasswordItem p : passwordItems)
		{
			clone.passwordItems.add((PasswordItem) p.clone());
		}
		
		return clone;
	}
	
	
}
