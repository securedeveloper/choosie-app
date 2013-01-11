package com.choosie.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class IntentData implements Parcelable {
	private final int startingImage;
	private final int votes1;
	private final int votes2;
	private final String photo1Path;
	private final String photo2Path;
	private final String userPhotoPath;
	private final String userName;
	private final String question;
	private final boolean isVotedAlready;
	private final boolean noSecondPhoto;

	public IntentData(Parcel source) {
		/*
		 * Reconstruct from the Parcel
		 */
		Log.v("parcel ota",
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
	}

	public IntentData(int startingImage, int votes1, int votes2,
			String photo1Path, String photo2Path, String userPhotoPath,
			String userName, String question, boolean isVotedAlready,
			boolean noSecondPhoto) {
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