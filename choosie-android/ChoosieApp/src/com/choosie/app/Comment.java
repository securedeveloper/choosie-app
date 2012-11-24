package com.choosie.app;

import java.util.Date;

public class Comment {

	String fb_uid;
	Date createdAt;
	String text;	
	String post_key;
	
	public Comment(String fb_uid, Date created_at, String text, String post_key) {
		this.fb_uid = fb_uid;
		this.createdAt = created_at;
		this.text = text;		
		this.post_key = post_key;
	}

	public CharSequence getText() {
		return text;
	}

}
