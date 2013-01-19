package com.choozie.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.choozie.app.R;
import com.choozie.app.NewChoosiePostData.PostType;
import com.choozie.app.caches.CacheCallback;
import com.choozie.app.caches.Caches;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.User;
import com.choozie.app.models.Vote;
import com.choozie.app.models.VoteData;

/*
 * class VotePopupWindowUtils: basic utils for poping a window for votes
 * for activate - create new instance and activate popUpVotesWindow
 */
public class VotePopupWindowUtils {
	Activity activity;

	public VotePopupWindowUtils(Activity activity) {
		this.activity = activity;
	}

	public void popUpVotesWindow(String postKey) {
		L.d("VotePopupWindow: entered popUpVotesWindow, postKey = " + postKey);

		// We need to get the instance of the LayoutInflater, use the
		// context of this activity
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.popup_layout,
				(ViewGroup) activity.findViewById(R.id.popup_element));

		final ProgressBar progressBar = (ProgressBar) layout
				.findViewById(R.id.votesPopupWindow_progressBar);

		PopupWindow pw;

		// set view and size
		pw = new PopupWindow(layout, Utils.getScreenWidth() - 30,
				Utils.getScreenHeight() / 2, true);
		pw.setAnimationStyle(R.style.PopupWindowAnimation);

		// for closing when touching outside - set the background not null
		pw.setBackgroundDrawable(new BitmapDrawable());

		// show it!
		pw.showAtLocation(layout, Gravity.BOTTOM, 0, 10);

		Caches.getInstance()
				.getPostsCache()
				.getValue(postKey,
						new CacheCallback<String, ChoosiePostData>() {
							@Override
							public void onValueReady(String key,
									ChoosiePostData result) {
								if (result == null) {
									L.e("ERROR : param is 'null'");
									// TODO: Handle error
									// Toast.makeText(getActivity(),
									// "Failed to update post.",
									// Toast.LENGTH_SHORT).show();
									return;
								}
								L.d("popUpVotesWindow: on finish- got choosiePostDat");
								progressBar.setVisibility(View.GONE);
								createAndShowPopup(result, layout);
							}
						});
	}

	private void createAndShowPopup(ChoosiePostData choosiePost,
			RelativeLayout layout) {
		L.d("VotePopupWindow, starting createAndShowPopup");

		if (choosiePost.getPostType() == PostType.YesNo) {
			((ImageView) layout
					.findViewById(R.id.votesPopupWindow_votes1_image))
					.setImageResource(R.drawable.thumbs_up);
			((ImageView) layout
					.findViewById(R.id.votesPopupWindow_votes2_image))
					.setImageResource(R.drawable.thumbs_down);
		} else {
			((ImageView) layout
					.findViewById(R.id.votesPopupWindow_votes1_image))
					.setVisibility(View.GONE);
			((ImageView) layout
					.findViewById(R.id.votesPopupWindow_votes2_image))
					.setVisibility(View.GONE);
		}

		// set the votes numbers
		final TextView textViewVotes1 = (TextView) layout
				.findViewById(R.id.votesPopupWindow_votes1);
		final TextView textViewVotes2 = (TextView) layout
				.findViewById(R.id.votesPopupWindow_votes2);

		textViewVotes1.setText(choosiePost.getVotes1() + " votes");
		textViewVotes2.setText(choosiePost.getVotes2() + " votes");

		// create the votes list
		ArrayList<User> usersList = new ArrayList<User>();
		ArrayList<Integer> voteForList = new ArrayList<Integer>();

		for (Vote vote : choosiePost.getVotes()) {
			User user = vote.getUser();
			usersList.add(user);
			voteForList.add(vote.getVote_for());
		}

		// create the adapter
		ArrayAdapter<VoteData> voteScreenAdapter = makeVotesScreenAdapter(
				usersList, voteForList);

		// attache the listView, where the votes will be displayed
		ListView listView = (ListView) layout
				.findViewById(R.id.votesPopupWindow_listView);
		listView.setAdapter(voteScreenAdapter);
	}

	private ArrayAdapter<VoteData> makeVotesScreenAdapter(
			ArrayList<User> users, ArrayList<Integer> voteForList) {

		L.d("VotePopupWindowe, starting makeVotesScreenAdapter");

		ArrayAdapter<VoteData> adi = new ArrayAdapter<VoteData>(activity,
				R.layout.view_votes) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				VoteData item = getItem(position);

				return createViewVotes(item, convertView, parent, position);
			}
		};

		ArrayList<User> votersToPhoto1 = new ArrayList<User>();
		ArrayList<User> votersToPhoto2 = new ArrayList<User>();

		// fill each array with its suite votes
		divideVoters(votersToPhoto1, votersToPhoto2, users, voteForList);

		for (int i = 0; i < Math.max(votersToPhoto1.size(),
				votersToPhoto2.size()); i++) {

			User user1 = null;
			User user2 = null;

			if (i < votersToPhoto1.size()) {
				user1 = votersToPhoto1.get(i);
			}
			if (i < votersToPhoto2.size()) {
				user2 = votersToPhoto2.get(i);
			}
			VoteData newVoteData = new VoteData(user1, user2);
			adi.add(newVoteData);
		}
		return adi;
	}

	private void divideVoters(ArrayList<User> votersToPhoto1,
			ArrayList<User> votersToPhoto2, ArrayList<User> users,
			ArrayList<Integer> voteForList) {

		for (int i = 0; i < users.size(); i++) {
			if (voteForList.get(i) == 1) {
				votersToPhoto1.add(users.get(i));
			} else {
				votersToPhoto2.add(users.get(i));
			}
		}
	}

	private View createViewVotes(final VoteData item, View convertView,
			View parentView, int position) {

		L.d("VotePopupWindow, starting to createViewVotes, position = "
				+ position);

		LinearLayout itemView = null;
		VoteViewHolder voteViewHolder = null;

		if (convertView == null) {
			// we have to inflate new view, and create new holder

			itemView = new LinearLayout(parentView.getContext());

			LayoutInflater inflater = (LayoutInflater) parentView.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.view_votes, itemView);

			voteViewHolder = new VoteViewHolder();
			voteViewHolder.tv1 = (TextView) itemView
					.findViewById(R.id.voteView_name1);
			voteViewHolder.tv2 = (TextView) itemView
					.findViewById(R.id.voteView_name2);
			voteViewHolder.voterPhotoImageView1 = (ImageView) itemView
					.findViewById(R.id.voteView_voterPhoto1);
			voteViewHolder.voterPhotoImageView2 = (ImageView) itemView
					.findViewById(R.id.voteView_voterPhoto2);
			itemView.setTag(voteViewHolder);
		} else {
			// we can reuse the convertView and get the holder
			itemView = (LinearLayout) convertView;
			voteViewHolder = (VoteViewHolder) itemView.getTag();
		}

		// first, clean the image and text in any case
		voteViewHolder.tv1.setText("");
		voteViewHolder.tv2.setText("");
		voteViewHolder.voterPhotoImageView1.setImageBitmap(null);
		voteViewHolder.voterPhotoImageView2.setImageBitmap(null);

		final VoteViewHolder holder = voteViewHolder;
		if (item.getUser1() != null) {
			// set the voter1 names
			setTextOntv(item.getUser1().getUserName(), voteViewHolder.tv1);
			// set the voter1 photo
			L.d("createViewVotes - name1 != null, getting from cache, name = "
					+ item.getUser1().getUserName());
			Caches.getInstance()
					.getPhotosCache()
					.getValue(item.getUser1().getPhotoURL(),
							new CacheCallback<String, Bitmap>() {
								@Override
								public void onValueReady(String key,
										Bitmap result) {
									L.d("createViewVotes, got param for name = "
											+ item.getUser1().getPhotoURL()
											+ "param = " + result);
									holder.voterPhotoImageView1
											.setImageBitmap(result);

									// set the listeners for the profile
									// holder.voterPhotoImageView1.setOnClickListener(new
									// OnClickListener() {
									//
									// public void onClick(View v) {
									// User user1 = new User(item.getName1(),
									// item.getVoterPhotoUrl1(), )
									//
									// }
									// });

								}
							});
		}

		if (item.getUser2() != null) {
			// set the voter2 names
			setTextOntv(item.getUser2().getUserName(), voteViewHolder.tv2);

			// set the voter2 photo
			L.d("name2 != null, getting from cache, name = "
					+ item.getUser2().getUserName());
			Caches.getInstance()
					.getPhotosCache()
					.getValue(item.getUser2().getPhotoURL(),
							new CacheCallback<String, Bitmap>() {
								@Override
								public void onValueReady(String key,
										Bitmap result) {
									L.d("createViewVotes, got param for name = "
											+ item.getUser2().getPhotoURL()
											+ "param = " + result);
									holder.voterPhotoImageView2
											.setImageBitmap(result);
								};
							});
		}

		return itemView;
	}

	private class VoteViewHolder {
		TextView tv1;
		ImageView voterPhotoImageView1;
		TextView tv2;
		ImageView voterPhotoImageView2;
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
