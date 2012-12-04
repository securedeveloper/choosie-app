package com.choosie.app.Models;

public class CommentData {

	private String name;
	private String comment;

	public CommentData(String name, String comment) {
		this.name = name;
		this.comment = comment;
	}

	public String getName() {
		return this.name;
	}

	public String getComment() {
		return this.comment;
	}
}
