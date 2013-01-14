package com.choozie.app.models;

public class User {
	private final String userName;
	private final String photoURL;
	private final String fbUid;
   
	public User(String userName, String photoURL, String fbUid) {
		this.userName = userName;
		this.photoURL = photoURL;
		this.fbUid = fbUid;
	}

	public String getUserName() {
		return userName;
	}

	public String getPhotoURL() {
		return photoURL;
	}

	public String getFbUid() {
		return fbUid;
	}

}
