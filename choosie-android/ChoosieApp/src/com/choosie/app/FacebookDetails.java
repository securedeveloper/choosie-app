package com.choosie.app;

public class FacebookDetails {
	
	private String fb_uid;
	private String access_token;
	private long access_token_expdate;
	
	public FacebookDetails(String fb_uid, String access_token, long access_token_expdate) {
		this.setFb_uid(fb_uid);
		this.setAccess_token(access_token);
		this.setAccess_token_expdate(access_token_expdate);
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getFb_uid() {
		return fb_uid;
	}

	public void setFb_uid(String fb_uid) {
		this.fb_uid = fb_uid;
	}

	public long getAccess_token_expdate() {
		return access_token_expdate;
	}

	public void setAccess_token_expdate(long access_token_expdate) {
		this.access_token_expdate = access_token_expdate;
	}
	
	
}
