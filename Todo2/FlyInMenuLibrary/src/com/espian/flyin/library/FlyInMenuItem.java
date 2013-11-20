package com.espian.flyin.library;

import android.content.Intent;
import android.content.res.Resources;

public class FlyInMenuItem {

	private Intent mIntent;
	private int mIconId;
	private CharSequence mText;
	private CharSequence mCondText;
	private int id;

	private boolean mEnabled;

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
		return mText;
	}

	public CharSequence getTitleCondensed() {
		return mCondText;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public FlyInMenuItem setEnabled(boolean enabled) {
		mEnabled = enabled;
		return this;
	}

	public FlyInMenuItem setIcon(int iconRes) {
		mIconId = iconRes;
		return this;
	}

	public FlyInMenuItem setIntent(Intent intent) {
		mIntent = intent;
		return this;
	}

	public FlyInMenuItem setTitle(CharSequence title) {
		mText = title;
		return this;
	}

	public FlyInMenuItem setTitle(int title, Resources resc) {
		mText = resc.getString(title);
		return this;
	}

	public FlyInMenuItem setTitleCondensed(CharSequence title) {
		mCondText = title;
		return this;
	}

	public FlyInMenuItem setId(int id) {
		this.id = id;
		return this;
	}

}
