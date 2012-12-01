package com.choosie.app;

public class Constants {
	public static final String LOG_TAG = "Choosie";

	public static class URIs {

		public static final String ROOT_URL = "http://choosieapp.appspot.com";
		// public static final String ROOT_URL =
		// "http://choosie-dev.appspot.com";
		public static final String FEED_URI = ROOT_URL + "/feed";
		public static final String NEW_VOTE_URI = ROOT_URL + "/votes/new";
		public static final String NEW_POSTS_URI = ROOT_URL + "/posts/new";
		public static final String NEW_COMMENT_URI = ROOT_URL + "/comments/new";
		public static final String POSTS_URI = ROOT_URL + "/posts";
	}

	public static class RequestCodes {
		public static final int TAKE_FIRST_PICTURE_FROM_CAMERA = 1;
		public static final int TAKE_SECOND_PICTURE_FROM_CAMERA = 2;
		public static final int TAKE_FIRST_PICTURE_FROM_GALLERY = 3;
		public static final int TAKE_SECOND_PICTURE_FROM_GALLERY = 4;
		public static final int CROP_FIRST = 5;
		public static final int CROP_SECOND = 6;
		public static final int COMMENT = 7;
		public static final int GALLERY = 8;

	}
}
