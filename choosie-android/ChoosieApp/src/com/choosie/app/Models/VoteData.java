package com.choosie.app.Models;


public class VoteData {

	private final String name1;
	private final String voterPhotoPath1;
	private final String name2;
	private final String voterPhotoPath2;

	public VoteData(String name1,String voterPhotoPath1, String name2, String voterPhotoPath2) {
		this.name1 = name1;
		this.voterPhotoPath1 = voterPhotoPath1;
		this.name2 = name2;
		this.voterPhotoPath2 = voterPhotoPath2;
	}

	public String getName1() {
		return this.name1;
	}
	
	public String getVoterPhotoPath1() {
		return this.voterPhotoPath1;
	}
	
	
	public String getName2() {
		return this.name2;
	}
	
	public String getVoterPhotoPath2() {
		return this.voterPhotoPath2;
	}	
}
