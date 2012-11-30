package com.choosie.app;

public class Constants {
	public static final String LOG_TAG = "Choosie";

	public static class URIs {

		public static final String ROOT_URL = "http://choosieapp.appspot.com";
		public static final String FEED_URI = "http://choosieapp.appspot.com/feed";
		public static final String NEW_VOTE_URI = "http://choosieapp.appspot.com/votes/new";
		public static final String NEW_POSTS_URI = "http://choosieapp.appspot.com/posts/new";
		public static final String NEW_COMMENT_URI = "http://choosieapp.appspot.com/comments/new";
		public static final String POSTS_URI = "http://choosieapp.appspot.com/posts";
	}

	public static class RequestCodes {
		public static final int TAKE_FIRST_PICTURE = 1;
		public static final int TAKE_SECOND_PICTURE = 2;
		public static final int COMMENT = 3;

	}
}
