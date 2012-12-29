package com.choosie.app;

import com.choosie.app.Constants.Notifications;
import android.os.Parcel;
import android.os.Parcelable;

public class PushNotification implements Parcelable {

	private String notificationType;
	private String text;
	private String postKey;
	private String deviceId;

	public PushNotification(String notificationType, String text,
			String postKey, String deviceId) {
		this.notificationType = notificationType;
		this.text = createTextForNotification(notificationType, text);
		this.postKey = postKey;
		this.deviceId = deviceId;
	}

	private String createTextForNotification(String notificationType,
			String text) {
		int type = Integer.valueOf(notificationType);

		switch (type) {
		case Notifications.NEW_POST_NOTIFICATION_TYPE:
			return text + " needs your help!";
		case Notifications.NEW_COMMENT_NOTIFICATION_TYPE:
			return text + " commented on your Choozie!";
		case Notifications.NEW_VOTE_NOTIFICATION_TYPE:
			return text + " voted on your Choozie!";
		default:
			return text;
		}
	}

	private PushNotification(Parcel in) {
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
		notificationType = in.readString();
		text = in.readString();
		postKey = in.readString();
		deviceId = in.readString();
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPostKey() {
		return postKey;
	}

	public void setPostKey(String postKey) {
		this.postKey = postKey;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(notificationType);
		dest.writeString(text);
		dest.writeString(postKey);
		dest.writeString(deviceId);
	}

	public static final Parcelable.Creator<PushNotification> CREATOR = new Parcelable.Creator<PushNotification>() {

		public PushNotification createFromParcel(Parcel in) {
			return new PushNotification(in);
		}

		public PushNotification[] newArray(int size) {
			return new PushNotification[size];
		}
	};

	@Override
	public String toString() {
		String str = "Push Notification:\n";

		str += "type = " + this.notificationType + "\n";
		str += "text = " + this.text + "\n";
		str += "post key = " + this.postKey + "\n";
		str += "device id = " + this.deviceId + "\n";

		return str;

	}
}
