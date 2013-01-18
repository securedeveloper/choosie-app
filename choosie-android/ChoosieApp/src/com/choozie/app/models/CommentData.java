package com.choozie.app.models;

public class CommentData {

	private final String name;
	private final String comment;
	private final String commentierPhotoUrl;
	private final String fbUid;
	private final CharSequence createdAt;
	private final boolean isDummyComment;

	public CommentData(String name, String comment, String commentierPhotoUrl,
			CharSequence createdAt, String fbUid) {
		this.name = name;
		this.comment = comment;
		this.commentierPhotoUrl = commentierPhotoUrl;
		this.createdAt = createdAt;
		this.isDummyComment = false;
		this.fbUid = fbUid;
	}

	public CommentData(boolean isDummyComment) {
		this.name = null;
		this.comment = null;
		this.commentierPhotoUrl = null;
		this.createdAt = null;
		this.isDummyComment = true;
		this.fbUid = null;
	}

	public String getName() {
		return this.name;
	}

	public String getComment() {
		return this.comment;
	}

	public String getCommentierPhotoUrl() {
		return this.commentierPhotoUrl;
	}

	public CharSequence getCreatedAt() {
		return this.createdAt;
	}

	public boolean checkIfDummyComment() {
		return isDummyComment;
	}

	public User getUser() {
		return new User(getName(), getCommentierPhotoUrl(), this.fbUid);
	}
}
