package com.choozie.app.client;

import java.util.List;

import com.choozie.app.models.ChoosiePostData;

public class FeedResponse {
	private boolean append;
	private List<ChoosiePostData> posts;
	private String cursor;

	public FeedResponse(boolean append, String cursor,
			List<ChoosiePostData> posts) {
		this.append = append;
		this.cursor = cursor;
		this.posts = posts;
	}

	public List<ChoosiePostData> getPosts() {
		return posts;
	}

	public String getCursor() {
		return cursor;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
}
