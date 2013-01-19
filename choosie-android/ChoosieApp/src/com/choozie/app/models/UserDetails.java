package com.choozie.app.models;


/*
 * Class to save all the User Details such as:
 * Nickname, Info, #Posts, #Votes, etc.
 */
public class UserDetails {
	
	public User user;
	public String nickname;
	public String info;
	public int numPosts;
	public int numVotes;
	
	public UserDetails(User user) {
		this.user = user;
	}
	
}
