package com.choozie.app.models;

import java.util.Date;

public class Comment {

	private final Date createdAtUTC;
	private final String text;
	private final String post_key;
	private final User user;
	private boolean isNeedTosave;

	public Comment(Date createdAtUTC, String text, String post_key, User user) {
		this.createdAtUTC = createdAtUTC;
		this.text = text;
		this.post_key = post_key;
		this.user = user;
		this.isNeedTosave = true;
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
	
	public Boolean getIsNeedToSave(){
		return isNeedTosave;
	}
	
	public void setIsNeedToSave(){
		isNeedTosave = false;
	}
}
