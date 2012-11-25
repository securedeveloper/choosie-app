package com.choosie.app;

import java.util.Date;

public class Comment {

	String fb_uid;
	Date createdAt;
	private String text;	
	private String post_key;
	
	public Comment(String fb_uid, Date created_at, String text, String post_key) {
		this.fb_uid = fb_uid;
		this.createdAt = created_at;
		this.text = text;		
		this.setPost_key(post_key);
	}

	public String getPost_key() {
		return post_key;
	}

	public void setPost_key(String post_key) {
		this.post_key = post_key;
	}

	public CharSequence getText() {
		return text;
	}
}
