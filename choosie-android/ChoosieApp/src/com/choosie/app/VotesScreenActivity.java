package com.choosie.app;

import java.util.ArrayList;

import com.choosie.app.Models.VoteData;
import com.google.analytics.tracking.android.EasyTracker;
import com.nullwire.trace.ExceptionHandler;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class VotesScreenActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_votes_screen);
		ExceptionHandler.register(this, Constants.URIs.CRASH_REPORT);

		L.i("in votes screen");

		final Intent intent = getIntent();

		fillVotesView(intent);

		ArrayAdapter<VoteData> voteScreenAdapter = makeVotesScreenAdapter(intent);

		ListView listView = (ListView) findViewById(R.id.votesListView);
		listView.setAdapter(voteScreenAdapter);
	}

	private void fillVotesView(Intent intent) {

		final ImageView imageViewPhoto1 = (ImageView) findViewById(R.id.votesScreen_photo1);
		final ImageView imageViewPhoto2 = (ImageView) findViewById(R.id.votesScreen_photo2);
		final ImageView imageViewUserPhoto = (ImageView) findViewById(R.id.votesScreen_userPhoto);

		// get the images Strings from the intent

		String photo1Path = intent
				.getStringExtra(Constants.IntentsCodes.photo1Path);
		String photo2Path = intent
				.getStringExtra(Constants.IntentsCodes.photo2Path);
		String userPhotoPath = intent
				.getStringExtra(Constants.IntentsCodes.userPhotoPath);

		setImageFromPath(photo1Path, imageViewPhoto1);
		setImageFromPath(photo2Path, imageViewPhoto2);
		setImageFromPath(userPhotoPath, imageViewUserPhoto);

		// set the question
		((TextView) findViewById(R.id.votesScreen_question)).setText(intent
				.getStringExtra(Constants.IntentsCodes.question));
	}

	private void setImageFromPath(String photoPath, ImageView imageViewPhoto) {
		if  (photoPath.contains("gabay")) {
			int a =5; 
		}
		if (photoPath != null) {
			imageViewPhoto.setImageBitmap(BitmapFactory.decodeFile(photoPath));
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private ArrayAdapter<VoteData> makeVotesScreenAdapter(final Intent intent) {
		ArrayAdapter<VoteData> adi = new ArrayAdapter<VoteData>(this,
				R.layout.view_votes) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				VoteData item = getItem(position);

				return createViewVotes(item);
			}
		};
		// fill the adapter:
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<String> votersPhotoUrlList = new ArrayList<String>();
		ArrayList<Integer> voteForList = new ArrayList<Integer>();

		nameList = intent
				.getStringArrayListExtra(Constants.IntentsCodes.nameList);
		votersPhotoUrlList = intent
				.getStringArrayListExtra(Constants.IntentsCodes.votersPhotoUrlList);
		voteForList = intent
				.getIntegerArrayListExtra(Constants.IntentsCodes.voteForList);

		ArrayList<MiniVoteData> votersToPhoto1 = new ArrayList<MiniVoteData>();
		ArrayList<MiniVoteData> votersToPhoto2 = new ArrayList<MiniVoteData>();
		devideVoters(votersToPhoto1, votersToPhoto2, nameList,
				votersPhotoUrlList, voteForList);

		for (int i = 0; i < Math.max(votersToPhoto1.size(),
				votersToPhoto2.size()); i++) {

			String voterName1 = null;
			String photoUrl1 = null;
			String voterName2 = null;
			String photoUrl2 = null;

			if (i < votersToPhoto1.size()) {
				voterName1 = votersToPhoto1.get(i).getName();
				photoUrl1 = votersToPhoto1.get(i).getphotoUrl();
			}
			if (i < votersToPhoto2.size()) {
				voterName2 = votersToPhoto2.get(i).getName();
				photoUrl2 = votersToPhoto2.get(i).getphotoUrl();
			}
			VoteData newVoteData = new VoteData(voterName1, photoUrl1,
					voterName2, photoUrl2);
			adi.add(newVoteData);
		}
		return adi;
	}

	private class MiniVoteData {
		private final String name;
		private final String photoUrl;

		public MiniVoteData(String name, String photoUrl) {
			this.name = name;
			this.photoUrl = photoUrl;
		}

		public String getName() {
			return this.name;
		}

		public String getphotoUrl() {
			return this.photoUrl;
		}
	}

	private void devideVoters(ArrayList<MiniVoteData> votersToPhoto1,
			ArrayList<MiniVoteData> votersToPhoto2, ArrayList<String> nameList,
			ArrayList<String> votersPhotoUrlList, ArrayList<Integer> voteForList) {

		for (int i = 0; i < nameList.size(); i++) {
			if (voteForList.get(i) == 1) {
				votersToPhoto1.add(new MiniVoteData(nameList.get(i),
						votersPhotoUrlList.get(i)));
			} else {
				votersToPhoto2.add(new MiniVoteData(nameList.get(i),
						votersPhotoUrlList.get(i)));
			}
		}
	}

	private View createViewVotes(VoteData item) {
		LinearLayout itemView = new LinearLayout(this);
		// itemView.inflate(this.getContext(), R.id.LinearLayout_view_comment,
		// parent);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_votes, itemView);

		// set the voter1 names
		if (item.getName1() != null) {
			TextView tv1 = (TextView) itemView
					.findViewById(R.id.voteView_name1);
			setTextOntv(item.getName1(), tv1);

			// set the voter1 photo
			ImageView voterPhotoImageView1 = (ImageView) itemView
					.findViewById(R.id.voteView_voterPhoto1);
			voterPhotoImageView1.setImageBitmap(BitmapFactory.decodeFile(item
					.getVoterPhotoUrl1()));
		}

		// set the voter2 names
		if (item.getName2() != null) {
			TextView tv2 = (TextView) itemView
					.findViewById(R.id.voteView_name2);
			setTextOntv(item.getName2(), tv2);

			// set the voter2 photo
			ImageView voterPhotoImageView2 = (ImageView) itemView
					.findViewById(R.id.voteView_voterPhoto2);
			voterPhotoImageView2.setImageBitmap(BitmapFactory.decodeFile(item
					.getVoterPhotoUrl2()));
		}

		return itemView;
	}

	private void setTextOntv(String name, TextView tv) {
		final SpannableStringBuilder sb = new SpannableStringBuilder(name);

		// Same as the User textColor in the XML.
		// TODO: Make it a resource that both use
		final ForegroundColorSpan blueLinkColor = new ForegroundColorSpan(
				Color.rgb(42, 30, 176));

		// Span to make text bold
		int charsToBoldify = name.length();
		sb.setSpan(blueLinkColor, 0, charsToBoldify,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// Span to set text color to some RGB value
		final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

		// Set the text color for first charsToBoldify characters
		sb.setSpan(bss, 0, charsToBoldify, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		tv.setText(sb);
	}

}
