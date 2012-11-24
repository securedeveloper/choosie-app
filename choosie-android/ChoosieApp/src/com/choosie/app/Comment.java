package com.choosie.app;

import java.util.Date;

public class Comment {

	String fb_uid;
	Date createdAt;
	String text;	
	String post_key;
	
	public Comment(String fb_uid, Date createdAt, String text, String post_key) {
		this.fb_uid = fb_uid;
		this.createdAt = createdAt;
		this.text = text;		
		this.post_key = post_key;
	}

}
