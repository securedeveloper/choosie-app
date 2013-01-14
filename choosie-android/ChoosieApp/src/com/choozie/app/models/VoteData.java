package com.choozie.app.models;

public class VoteData {

	private final String name1;
	private final String voterPhotoUrl1;
	private final String name2;
	private final String voterPhotoUrl2;

	public VoteData(String name1, String voterPhotoUrl1, String name2,
			String voterPhotoUrl2) {
		this.name1 = name1;
		this.voterPhotoUrl1 = voterPhotoUrl1;
		this.name2 = name2;
		this.voterPhotoUrl2 = voterPhotoUrl2;
	}

	public String getName1() {
		return this.name1;
	}

	public String getVoterPhotoUrl1() {
		return this.voterPhotoUrl1;
	}

	public String getName2() {
		return this.name2;
	}

	public String getVoterPhotoUrl2() {
		return this.voterPhotoUrl2;
	}
}
