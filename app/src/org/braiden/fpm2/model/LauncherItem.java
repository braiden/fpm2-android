package org.braiden.fpm2.model;

/**
 * Copyright (c) 2009 Braiden Kindt
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

public class LauncherItem implements Cloneable, FpmDataObject {

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
