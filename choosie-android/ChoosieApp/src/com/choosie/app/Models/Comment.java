package com.choosie.app.Models;

import java.util.Date;

public class Comment {

	private final Date createdAtUTC;
	private final String text;
	private final String post_key;
	private final User user;

	public Comment(Date createdAtUTC, String text, String post_key, User user) {
		this.createdAtUTC = createdAtUTC;
		this.text = text;
		this.post_key = post_key;
		this.user = user;
	}

	public String getPost_key() {
		return this.post_key;
	}

	public String getText() {
		return this.text;
	}

	public User getUser() {
		return this.user;
	}

	public Date getCreatedAt() {
		return this.createdAtUTC;
	}
}
