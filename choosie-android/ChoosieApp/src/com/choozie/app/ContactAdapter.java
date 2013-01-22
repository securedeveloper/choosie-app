package com.choozie.app;

import java.util.List;

import com.choozie.app.models.Contact;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Contact> {
	private final List<Contact> _contacts;
	private final Activity _context;

	public ContactAdapter(Activity context, List<Contact> contacts) {
		super(context, R.layout.contactlistitem, contacts);
		this._contacts = contacts;
		this._context = context;
	}

	static class ViewHolder {
		//protected TextView text;
		protected CheckBox _cbContactName;
		private Contact _contact;

		protected void setContact(Contact contact) {
			_cbContactName.setText(contact.getDisplayName());
			_contact = contact;
		}

		protected Contact getContact() {
			return _contact;
		}
	}

	@Override
	public Contact getItem(int position) {
		return _contacts.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflater = _context.getLayoutInflater();
			view = inflater.inflate(R.layout.contactlistitem, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder._cbContactName = (CheckBox) view.findViewById(R.id.cbDisplayName);
			viewHolder.setContact(_contacts.get(position));
			view.setTag(viewHolder);
		}

		return view;
	}
}
