package com.choozie.app;

import com.choozie.app.models.Contact;
import com.choozie.app.models.ContactList;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_contacts);
//		LinearLayout view = (LinearLayout)findViewById(R.layout.activity_contacts);
//		view.addView(view);

//		RelativeLayout rl = (RelativeLayout)findViewById(R.id.header_layout);
//		view.addView(rl);
		
		ContactList contactList = this.getContacts();
		ArrayAdapter<Contact> adapter = new ContactAdapter(this,
				contactList.getContacts());
		setListAdapter(adapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		Contact c = (Contact) o;
		Toast.makeText(this, c.getDisplayName(), Toast.LENGTH_SHORT).show();
	}

	private ContactList getContacts() {
		ContactList contactList = new ContactList();
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		ContentResolver cr = getContentResolver();
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor cur = cr.query(uri, null, null, null, sortOrder);
		if (cur.getCount() > 0) {
			String id;
			String name;
			while (cur.moveToNext()) {
				Contact c = new Contact();
				id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				c.setId(id);
				c.setDisplayName(name);
				contactList.addContact(c);
			}
		}
		cur.close();
		return contactList;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_contacts, menu);
		return true;
	}

}
