package com.choosie.app;

public class EnlargeDetails {
	private String image1Path;
	private String image2Path;
	private int votes1;
	private int votes2;
	private boolean isAlreadyVoted;

	public EnlargeDetails(String image1Path, String image2Path, int votes1,
			int votes2, boolean isAlreadyVoted) {
		this.image1Path = image1Path;
		this.image2Path = image2Path;
		this.votes1 = votes1;
		this.votes2 = votes2;
		this.isAlreadyVoted = isAlreadyVoted;
	}
	
	public String getImagePath1(){
		return image1Path;
	}
	
	public String getImagePath2(){
		return image2Path;
	}
	
    public int getVotes1(){
		return votes1;
	}
	
	public int getVotes2(){
		return votes2;
	}
	
	public boolean checkIfAlreadyVoted(){
		return isAlreadyVoted;
	}
}
