package com.choosie.app.Models;

import java.util.Date;

public class Comment {

	private final String fb_uid;
	private final Date createdAtUTC;
	private final String text;
	private final String post_key;
	private final User user;

	public Comment(String fb_uid, Date createdAtUTC, String text,
			String post_key, User user) {
		this.fb_uid = fb_uid;
		this.createdAtUTC = createdAtUTC;
		this.text = text;
		this.post_key = post_key;
		this.user = user;
	}

	public String getPost_key() {
		return post_key;
	}

	public String getText() {
		return text;
	}

	public User getUser() {
		return user;
	}
}