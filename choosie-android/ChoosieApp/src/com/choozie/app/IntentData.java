package com.choozie.app;

import android.os.Parcel;
import android.os.Parcelable;

public class IntentData implements Parcelable {
	private final int startingImage;
	private int votes1;
	private int votes2;
	private final String postKey;
	private final String photo1Path;
	private final String photo2Path;
	private final String userPhotoPath;
	private final String userName;
	private final String question;
	private boolean isVotedAlready;
	private final boolean noSecondPhoto;
	private boolean isVotedAlready1;
	private boolean isVotedAlready2;

	public IntentData(Parcel source) {
		/*
		 * Reconstruct from the Parcel
		 */
		L.v("parcel ota",
				"ParcelData(Parcel source): time to put back parcel data");
		startingImage = source.readInt();
		votes1 = source.readInt();
		votes2 = source.readInt();
		photo1Path = source.readString();
		photo2Path = source.readString();
		userPhotoPath = source.readString();
		userName = source.readString();
		question = source.readString();
		isVotedAlready = source.readByte() == 1;
		noSecondPhoto = source.readByte() == 1;
		isVotedAlready1 = source.readByte() == 1;
		isVotedAlready2 = source.readByte() == 1;
		postKey = source.readString();
	}

	public IntentData(int startingImage, int votes1, int votes2,
			String photo1Path, String photo2Path, String userPhotoPath,
			String userName, String question, boolean isVotedAlready,
			boolean noSecondPhoto, boolean isVotedAlready1,
			boolean isVotedAlready2, String postKey) {
		this.startingImage = startingImage;
		this.votes1 = votes1;
		this.votes2 = votes2;
		this.photo1Path = photo1Path;
		this.photo2Path = photo2Path;
		this.userPhotoPath = userPhotoPath;
		this.userName = userName;
		this.question = question;
		this.isVotedAlready = isVotedAlready;
		this.noSecondPhoto = noSecondPhoto;
		this.isVotedAlready1 = isVotedAlready1;
		this.isVotedAlready2 = isVotedAlready2;
		this.postKey = postKey;
	}

	public int getStartingImage() {
		return startingImage;
	}

	public int getVotes1() {
		return votes1;
	}

	public int getVotes2() {
		return votes2;
	}

	public String getphoto1Path() {
		return photo1Path;
	}

	public String getphoto2Path() {
		return photo2Path;
	}

	public String getUserPhotoPath() {
		return userPhotoPath;
	}

	public String getUserName() {
		return userName;
	}

	public String getQuestion() {
		return question;
	}

	public boolean checkIfVotedAlready() {
		return isVotedAlready;
	}

	public boolean checkIfVotedAlready(int photoNumber) {
		if (photoNumber == 1) {
			return isVotedAlready1;
		} else {
			return isVotedAlready2;
		}
	}

	public String getPostKey() {
		return postKey;
	}

	public void setVotes1(int votes) {
		votes1 = votes;
	}

	public void setVotes2(int votes) {
		votes2 = votes;
	}

	public void setIsVotedAlread() {
		isVotedAlready = true;
	}

	public void setVotedFor(int photoNumber) {
		if (photoNumber == 1) {
			isVotedAlready1 = true;
			isVotedAlready2 = false;
		} else {
			isVotedAlready2 = true;
			isVotedAlready2 = false;
		}

	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(startingImage);
		dest.writeInt(votes1);
		dest.writeInt(votes2);
		dest.writeString(photo1Path);
		dest.writeString(photo2Path);
		dest.writeString(userPhotoPath);
		dest.writeString(userName);
		dest.writeString(question);
		dest.writeByte((byte) (isVotedAlready ? 1 : 0));
		dest.writeByte((byte) (noSecondPhoto ? 1 : 0));
		dest.writeByte((byte) (isVotedAlready1 ? 1 : 0));
		dest.writeByte((byte) (isVotedAlready2 ? 1 : 0));
		dest.writeString(postKey);
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static Parcelable.Creator CREATOR = new Parcelable.Creator<IntentData>() {

		public IntentData createFromParcel(Parcel source) {
			return new IntentData(source);
		}

		public IntentData[] newArray(int size) {
			return new IntentData[size];
		}
	};

	public boolean getNoSecondPhoto() {
		return noSecondPhoto;
	}

}