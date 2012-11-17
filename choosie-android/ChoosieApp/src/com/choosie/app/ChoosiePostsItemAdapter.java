package com.choosie.app;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ChoosiePostsItemAdapter extends ArrayAdapter<ChoosiePostData> {
	public ChoosiePostsItemAdapter(Context context, int textViewResourceId, SuperController superController) {
		super(context, textViewResourceId);
		this.superController = superController;
	}
	int count = 0;
	private SuperController superController;
	synchronized int getCountAndIncrement() {
		return ++count;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(ChoosieConstants.LOG_TAG, "ChoosiePostsItemAdapter: creating view for item. Times called: " + getCountAndIncrement());
		
		ChoosiePostData item = getItem(position);
		ChoosiePostView itemView = new ChoosiePostView(this.getContext(), this.superController);
		
		
		itemView.loadChoosiePost(item);
		
		return itemView;
	}

}
