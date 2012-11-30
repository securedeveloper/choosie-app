package com.choosie.app.Models;

import java.io.Serializable;

public class FacebookDetails implements Serializable {

	private static final long serialVersionUID = 4755253194442117895L;

	private final String fb_uid;
	private final String access_token;
	private final long access_token_expdate;

	public FacebookDetails(String fb_uid, String access_token,
			long access_token_expdate) {
		this.fb_uid = fb_uid;
		this.access_token = access_token;
		this.access_token_expdate = access_token_expdate;
	}

	public String getAccess_token() {
		return access_token;
	}

	public String getFb_uid() {
		return fb_uid;
	}

	public long getAccess_token_expdate() {
		return access_token_expdate;
	}

}
