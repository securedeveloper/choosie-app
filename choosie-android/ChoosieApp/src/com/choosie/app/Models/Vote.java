package com.choosie.app.Models;

import java.util.Date;

public class Vote {
	private final Date createdAtUTC;
	private final int vote_for;
	private final User user;

	public Vote(Date createdAtUTC, int vote_for, User user) {
		this.user = user;
		this.createdAtUTC = createdAtUTC;
		this.vote_for = vote_for;
	}

	public int getVote_for() {
		return vote_for;
	}

	public Date getCreated_at() {
		return createdAtUTC;
	}

	public User getUsers() {
		return user;
	}
}
