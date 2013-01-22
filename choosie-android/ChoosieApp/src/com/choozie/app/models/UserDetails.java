package com.choozie.app.models;

import android.os.Parcel;
import android.os.Parcelable;


/*
 * Class to save all the User Details such as:
 * Nickname, Info, #Posts, #Votes, etc.
 */
public class UserDetails implements Parcelable{
	
	public User user;
	public String nickname;
	public String info;
	public int numPosts;
	public int numVotes;
	
	public UserDetails(User user) {
		this.user = user;
		this.nickname = "";
		this.info = "";
		this.numPosts = 0;
		this.numVotes = 0;
	}

	public UserDetails(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		user = in.readParcelable(User.class.getClassLoader());
		nickname = in.readString();
		info = in.readString();
		numPosts = in.readInt();
		numVotes = in.readInt();
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeParcelable(user, flags);
		dest.writeString(nickname);
		dest.writeString(info);
		dest.writeInt(numPosts);
		dest.writeInt(numVotes);
	}
	
	public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {

		public UserDetails createFromParcel(Parcel in) {
			return new UserDetails(in);
		}

		public UserDetails[] newArray(int size) {
			return new UserDetails[size];
		}
	};

	
}
