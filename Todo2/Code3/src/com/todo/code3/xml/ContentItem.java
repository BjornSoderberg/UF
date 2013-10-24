package com.todo.code3.xml;

import android.content.Intent;
import android.content.res.Resources;

public class ContentItem {

	protected Intent mIntent;
	protected int mIconId;
	protected CharSequence title;
	//protected CharSequence mCondText;
	protected int id;
	protected int folderId;

	protected boolean mEnabled = true;

	public int getIconId() {
		return mIconId;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public int getId() {
		return id;
	}

	public CharSequence getTitle() {
		return title;
	}
	
	public int getFolderId() {
		return folderId;
	}

//	public CharSequence getTitleCondensed() {
//		return mCondText;
//	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public ContentItem setEnabled(boolean enabled) {
		mEnabled = enabled;
		return this;
	}

	public ContentItem setIcon(int iconRes) {
		mIconId = iconRes;
		return this;
	}

	public ContentItem setIntent(Intent intent) {
		mIntent = intent;
		return this;
	}

	public ContentItem setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

	public ContentItem setTitle(int title, Resources resc) {
		this.title = resc.getString(title);
		return this;
	}

//	public ContentItem setTitleCondensed(CharSequence title) {
//		mCondText = title;
//		return this;
//	}

	public ContentItem setId(int id) {
		this.id = id;
		return this;
	}
	
	public ContentItem setFolderId(int i) {
		folderId = i;
		return this;
	}

}
