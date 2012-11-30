package com.choosie.app.Models;

import java.util.Date;

public class Vote {

	private final String fb_uid;
	private final Date created_at;
	private final int vote_for;

	public Vote(String fb_uid, Date created_at, int vote_for) {
		this.fb_uid = fb_uid;
		this.created_at = created_at;
		this.vote_for = vote_for;
	}

	public int getVote_for() {
		return vote_for;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public String getFb_uid() {
		return fb_uid;
	}

}
