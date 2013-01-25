package com.choozie.app.models;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Class to save all the User Details such as:
 * Nickname, Info, #Posts, #Votes, etc.
 */
public class UserDetails implements Parcelable {

	private User user;
	private String nickname;
	private String info;
	private String gender;
	private int numPosts;
	private int numVotes;

	public UserDetails(User user) {
		this.setUser(user);
		this.setNickname("");
		this.setInfo("");
		this.setNumPosts(0);
		this.setNumVotes(0);
		this.setGender("");
	}

	public UserDetails(User user, String nickname, String info, int numPosts,
			int numVotes, String gender) {
		this.setUser(user);
		this.setNickname(nickname);
		this.setInfo(info);
		this.setNumPosts(numPosts);
		this.setNumVotes(numVotes);
		this.setGender(gender);
	}

	public UserDetails(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		setUser((User) in.readParcelable(User.class.getClassLoader()));
		setNickname(in.readString());
		setInfo(in.readString());
		setNumPosts(in.readInt());
		setNumVotes(in.readInt());
		setGender(in.readString());
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeParcelable(getUser(), flags);
		dest.writeString(getNickname());
		dest.writeString(getInfo());
		dest.writeInt(getNumPosts());
		dest.writeInt(getNumVotes());
		dest.writeString(getGender());
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getNumPosts() {
		return numPosts;
	}

	public void setNumPosts(int numPosts) {
		this.numPosts = numPosts;
	}

	public int getNumVotes() {
		return numVotes;
	}

	public void setNumVotes(int numVotes) {
		this.numVotes = numVotes;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {

		public UserDetails createFromParcel(Parcel in) {
			return new UserDetails(in);
		}

		public UserDetails[] newArray(int size) {
			return new UserDetails[size];
		}
	};

	@Override
	public String toString() {
		String str = "User Details: \n" + user.toString() +
				"nick: " + this.getNickname() + "\n" +
				"info: " + this.getInfo() + "\n" +
				"gender: " + this.getGender() + "\n" +
				"num_posts: " + this.getNumPosts() + "\n" +
				"num_votes: " + this.getNumVotes() + "\n";
		return str;
	}
}
