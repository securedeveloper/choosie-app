package com.choosie.app.Models;

import java.util.ArrayList;
import java.util.List;

import com.choosie.app.FacebookDetails;
import com.choosie.app.Vote;

/**
 * Represents the data that is returned from the server.
 */
public class ChoosiePostData {

	private int votes1;
	private int votes2;
	private String photo1URL;
	private String photo2URL;
	private String question;
	private String userName;
	private String userPhotoURL;
	private String user_fb_uid;
	private List<Vote> lstVotes;

	private FacebookDetails fbDetails;

	public ChoosiePostData(FacebookDetails fbDetails) {
		this.lstVotes = new ArrayList<Vote>();
		this.fbDetails = fbDetails;
	}

	public String getKey() {
		// HACK: Get post key from photo1URL
		String url = getPhoto1URL();
		String key = url.substring(url.indexOf("post_key=")
				+ "post_key=".length());
		return key;
	}

	public boolean isVotedAlready(int vote_for) {
		for (Vote vote : this.lstVotes) {
			if (vote.getFb_uid().equals(this.user_fb_uid) &&
					vote.getVote_for() == vote_for)
				return true;
		}
		return false;
	}

	public boolean isPostByMe() {
		if (fbDetails.getFb_uid().equals(this.user_fb_uid)) {
			return true;
		}

		return false;
	}

	public int getVotes1() {
		return votes1;
	}

	public void setVotes1(int votes1) {
		this.votes1 = votes1;
	}

	public int getVotes2() {
		return votes2;
	}

	public void setVotes2(int votes2) {
		this.votes2 = votes2;
	}

	public String getPhoto1URL() {
		return photo1URL;
	}

	public void setPhoto1URL(String photo1URL) {
		this.photo1URL = photo1URL;
	}

	public String getPhoto2URL() {
		return photo2URL;
	}

	public void setPhoto2URL(String photo2URL) {
		this.photo2URL = photo2URL;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPhotoURL() {
		return userPhotoURL;
	}

	public void setUserPhotoURL(String userPhotoURL) {
		this.userPhotoURL = userPhotoURL;
	}

	public List<Vote> getLstVotes() {
		return lstVotes;
	}

	public void setLstVotes(List<Vote> lstVotes) {
		this.lstVotes = lstVotes;
	}

	public String getUser_fb_uid() {
		return user_fb_uid;
	}

	public void setUser_fb_uid(String user_fb_uid) {
		this.user_fb_uid = user_fb_uid;
	}

}
