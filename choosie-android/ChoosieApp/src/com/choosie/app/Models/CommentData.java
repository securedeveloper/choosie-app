package com.choosie.app.Models;

public class CommentData {

	private final String name;
	private final String comment;
	private final String commentierPhotoPath;

	public CommentData(String name, String comment, String commentierPhotoUrlList) {
		this.name = name;
		this.comment = comment;
		this.commentierPhotoPath = commentierPhotoUrlList;
	}

	public String getName() {
		return this.name;
	}

	public String getComment() {
		return this.comment;
	}
	
	public String getcommentierPhotoPath() {
		return this.commentierPhotoPath;
	}
}
