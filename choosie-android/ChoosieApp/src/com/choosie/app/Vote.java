package com.choosie.app;

import java.util.Date;


public class Vote {
	
	private String fb_uid;
	private Date created_at;
	private int vote_for;
	
	public Vote(String fb_uid, Date created_at, int vote_for) {
		this.fb_uid = fb_uid;
		this.created_at = created_at;
		this.vote_for = vote_for;
	}

	public int getVote_for() {
		return vote_for;
	}

	public void setVote_for(int vote_for) {
		this.vote_for = vote_for;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public String getFb_uid() {
		return fb_uid;
	}

	public void setFb_uid(String fb_uid) {
		this.fb_uid = fb_uid;
	}

}
