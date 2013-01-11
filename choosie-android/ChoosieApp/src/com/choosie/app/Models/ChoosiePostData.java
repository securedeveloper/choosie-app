package com.choosie.app.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.choosie.app.NewChoosiePostData.PostType;

import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

/**
 * Represents the data that is returned from the server. This class is all
 * read-only.
 */
public class ChoosiePostData {

	private String postKey;
	private String photo1URL;
	private String photo2URL;
	private String question;
	private User author;
	private Date createdAtUTC;
	private List<Vote> votes;
	private List<Comment> comments;
	private FacebookDetails loggedInUser;
	private boolean isPostByMe;
	private SparseIntArray votesCounter;
	private SparseBooleanArray votedAlready;
	private PostType postType;

	public ChoosiePostData(FacebookDetails loggedInUser, String postKey,
			String photo1URL, String photo2URL, String question, User author,
			Date createdAtUTC, List<Vote> votes, List<Comment> comments,
			PostType postType) {
		this.loggedInUser = loggedInUser;
		this.postKey = postKey;
		this.photo1URL = photo1URL;
		this.photo2URL = photo2URL;
		this.question = question;
		this.author = author;
		this.createdAtUTC = createdAtUTC;
		this.postType = postType;

		initVotes(votes);
		initComments(comments);
		// TODO: Remove this null check after fixing the 'Loading item' hack
		// from FeedListAdapter
		this.isPostByMe = author != null
				&& (loggedInUser.getFb_uid().equals(author.getFbUid()));
	}

	private void initVotes(List<Vote> votes) {
		this.votes = new ArrayList<Vote>();
		this.votesCounter = new SparseIntArray(2);
		this.votesCounter.put(1, 0);
		this.votesCounter.put(2, 0);
		this.votedAlready = new SparseBooleanArray(2);
		this.votedAlready.put(1, false);
		this.votedAlready.put(2, false);
		// TODO: Remove this null check after fixing the 'Loading item' hack
		// from FeedListAdapter
		if (votes == null) {
			return;
		}
		for (Vote vote : votes) {

			// Check if post is by me
			if (vote.getUsers().getFbUid() == loggedInUser.getFb_uid()) {
				this.isPostByMe = true;
			}

			// Increment the relevant counter by 1
			int vote_for = vote.getVote_for();
			votesCounter.put(vote_for, votesCounter.get(vote_for) + 1);

			// Remember what the logged in user already voted for
			if (vote.getUsers().getFbUid()
					.equals(this.loggedInUser.getFb_uid())) {
				votedAlready.put(vote_for, true);
			}

			// Save a reference to the Vote object
			this.votes.add(vote);
		}
	}

	private void initComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getPostKey() {
		return this.postKey;
	}

	/**
	 * function checks if the logged in user voted for the selected picture (one
	 * / two)
	 */
	public boolean isVotedAlready(int vote_for) {
		return votedAlready.get(vote_for);
	}

	/**
	 * function checks if the logged in user voted for either one of the
	 * pictures
	 */
	public boolean isVotedAlready() {
		return (isVotedAlready(1) || isVotedAlready(2));
	}

	public boolean isPostByMe() {
		return this.isPostByMe;
	}

	public int getVotes1() {
		return votesCounter.get(1);
	}

	public int getVotes2() {
		return votesCounter.get(2);
	}

	public String getPhoto1URL() {
		return photo1URL;
	}

	public String getPhoto2URL() {
		return photo2URL;
	}

	public String getQuestion() {
		return question;
	}

	public User getAuthor() {
		return author;
	}

	public List<Comment> getComments() {
		return Collections.unmodifiableList(comments);
	}

	public List<Vote> getVotes() {
		return Collections.unmodifiableList(votes);
	}

	public Date getCreatedAt() {
		return createdAtUTC;
	}

	public PostType getPostType() {
		return postType;
	}
}
