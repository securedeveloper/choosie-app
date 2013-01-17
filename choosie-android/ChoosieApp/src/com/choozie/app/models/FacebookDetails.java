package com.choozie.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FacebookDetails implements Parcelable{

	private String fb_uid;
	private String access_token;
	private long access_token_expdate;
	private String firstName;
	private String lastName;

	public FacebookDetails(String fb_uid, String access_token,
			long access_token_expdate, String firstName, String lastName) {
		this.fb_uid = fb_uid;
		this.access_token = access_token;
		this.access_token_expdate = access_token_expdate;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getAccess_token() {
		return access_token;
	}

	public String getFb_uid() {
		return fb_uid;
	}

	public long getAccess_token_expdate() {
		return access_token_expdate;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	private FacebookDetails(Parcel in) {
		readFromParcel(in);
	}

	/**
	 * 
	 * Called from the constructor to create this object from a parcel.
	 * 
	 * @param in
	 *            parcel from which to re-create object
	 */
	private void readFromParcel(Parcel in) {

		// readParcelable needs the ClassLoader
		// but that can be picked up from the class
		// This will solve the BadParcelableException
		// because of ClassNotFoundException
		this.fb_uid = in.readString();
		this.access_token = in.readString();
		this.access_token_expdate = in.readLong();
		this.firstName = in.readString();
		this.lastName = in.readString();
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(fb_uid);
		dest.writeString(access_token);
		dest.writeLong(access_token_expdate);
		dest.writeString(firstName);
		dest.writeString(lastName);
	}
	
	public static final Parcelable.Creator<FacebookDetails> CREATOR = new Parcelable.Creator<FacebookDetails>() {

		public FacebookDetails createFromParcel(Parcel in) {
			return new FacebookDetails(in);
		}

		public FacebookDetails[] newArray(int size) {
			return new FacebookDetails[size];
		}
	};

}
