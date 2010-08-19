package org.braiden.fpm2.model;

public class LauncherItem implements Cloneable {

	private String title;
	private String cmdline;
	private String copyUser;
	private String copyPassword;
	
	public String getCmdline() {
		return cmdline;
	}
	
	public void setCmdline(String cmdline) {
		this.cmdline = cmdline;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCopyUser() {
		return copyUser;
	}

	public void setCopyUser(String copyUser) {
		this.copyUser = copyUser;
	}

	public String getCopyPassword() {
		return copyPassword;
	}

	public void setCopyPassword(String copyPassword) {
		this.copyPassword = copyPassword;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
