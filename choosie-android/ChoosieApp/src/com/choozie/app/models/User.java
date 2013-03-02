package com.choozie.app.models;

import com.choozie.app.client.Client;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{
	private String userName;
	private String photoURL;
	private String fbUid;
	private String displayName;
   
	public User(String userName, String photoURL, String fbUid, String displayName) {
		this.userName = userName;
		this.photoURL = photoURL;
		this.fbUid = fbUid;
		this.displayName = displayName;
	}

	public User(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.userName = in.readString();
		this.photoURL = in.readString();
		this.fbUid = in.readString();
		this.displayName = in.readString();
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userName);
		dest.writeString(photoURL);
		dest.writeString(fbUid);
		dest.writeString(displayName);
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};
	
	public boolean equals(User user) {
		//User u = (User) user;
		return user.getFbUid().equals(this.fbUid);
	}
	
	public boolean isActiveUser() {
		return this.fbUid.equals(Client.getInstance().getActiveUser().getFbUid());
	}
	
	@Override
	public String toString() {
		String str = "User: " + this.getUserName() + "\n" +
				"fb_uid: " + this.getFbUid() + "\n" + 
				"Photo URL: " + this.getPhotoURL() + "\n" +
				"Display Name: " + this.getDisplayName();
		return str;
	}

}
