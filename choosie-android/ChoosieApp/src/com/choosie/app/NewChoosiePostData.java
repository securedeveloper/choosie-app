package com.choosie.app;

import java.io.Serializable;

import android.graphics.Bitmap;

public class NewChoosiePostData implements Serializable{

	public enum PostType {
		TOT, YesNo
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Bitmap image1;
	private Bitmap image2;
	private String question;
	private boolean shareOnFacebook;
	private PostType postType;

	public NewChoosiePostData(Bitmap image1, Bitmap image2, String question) {
		this(image1, image2, question, false);
	}
	
	public NewChoosiePostData(Bitmap image1, Bitmap image2, String question, boolean shareOnFacebook) {
		this(image1, image2, question, shareOnFacebook, PostType.TOT);
	}
	
	public NewChoosiePostData(Bitmap image1, Bitmap image2, String question, boolean shareOnFacebook, PostType postType) {
		this.setImage1(image1);
		this.setImage2(image2);
		this.setQuestion(question);
		this.setShareOnFacebook(shareOnFacebook);
		this.setPostType(postType);
	}


	public Bitmap getImage1() {
		return image1;
	}

	public void setImage1(Bitmap image1) {
		this.image1 = image1;
	}

	public Bitmap getImage2() {
		return image2;
	}

	public void setImage2(Bitmap image2) {
		this.image2 = image2;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public boolean isShareOnFacebook() {
		return shareOnFacebook;
	}

	public void setShareOnFacebook(boolean shareOnFacebook) {
		this.shareOnFacebook = shareOnFacebook;
	}

	public PostType getPostType() {
		return postType;
	}

	public void setPostType(PostType postType) {
		this.postType = postType;
	}

	public String getPostTypeAsString() {

		switch (this.postType) {
		case TOT:
			return "1";
		case YesNo:
			return "2";
		default:
			return "-1";
		}
	}
}
