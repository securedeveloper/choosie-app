package com.choozie.app.models;

import com.choozie.app.client.Client;

public class CommentData {

	private final String comment;
	private final CharSequence createdAt;
	private final boolean isDummyComment;
	private final User user;

	public CommentData(String comment, CharSequence createdAt, User user) {
		this.comment = comment;
		this.createdAt = createdAt;
		this.isDummyComment = false;
		this.user = user;
	}

	public CommentData(boolean isDummyComment) {
		this.comment = null;
		this.createdAt = null;
		this.isDummyComment = true;
		this.user = null;
	}

	public String getName() {
		if (user != null)
			return this.user.getDisplayName();
		return "";
	}

	public String getComment() {
		return this.comment;
	}

	public String getCommentierPhotoUrl() {
		if (user != null)
			return this.user.getPhotoURL();
		return "";
	}

	public CharSequence getCreatedAt() {
		return this.createdAt;
	}

	public boolean checkIfDummyComment() {
		return isDummyComment;
	}

	public User getUser() {
		return user;
	}
}
