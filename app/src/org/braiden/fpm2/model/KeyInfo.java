package org.braiden.fpm2.model;

public class KeyInfo {

	private String cipher;
	private String salt;
	private String vstring;
	
	public String getCipher() {
		return cipher;
	}
	
	public void setCipher(String cipher) {
		this.cipher = cipher;
	}
	
	public String getSalt() {
		return salt;
	}
	
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	public String getVstring() {
		return vstring;
	}
	
	public void setVstring(String vstring) {
		this.vstring = vstring;
	}
	
}
