package com.choozie.app;

import com.choozie.app.models.Contact;
import com.choozie.app.models.ContactList;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsActivity extends Activity {

	ArrayAdapter<Contact> adapter;
	private ListView contactsListView;
	private ImageButton confirmButton;
	private OnItemClickListener listItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> l, View v, int position,
				long id) {
			onListItemClick(l, v, position, id);
		}
	};
	private OnClickListener confirmClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			confirm();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		contactsListView = (ListView) findViewById(R.id.contacts_listView);
		confirmButton = (ImageButton)findViewById(R.id.contacts_confirm_button);
		
		ContactList contactList = this.getContacts();
		adapter = new ContactAdapter(this,
				contactList.getContacts());
		//setListAdapter(adapter);
		contactsListView.setOnItemClickListener(listItemClickListener);
		contactsListView.setAdapter(adapter);
		
		
		confirmButton.setOnClickListener(confirmClickListener);

	}

	protected void confirm() {
		
	}	

//	@Override
	protected void onListItemClick(AdapterView<?> l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		//Object o = this.getListAdapter().getItem(position);
		
		Object o = adapter.getItem(position);
		//Object o = contactsListView.getAdapter().getItem(position);
		Contact c = (Contact) o;
		L.i("onListItemClick() : clicked " + c.getDisplayName());
		//Toast.makeText(this, c.getDisplayName(), Toast.LENGTH_SHORT).show();
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
