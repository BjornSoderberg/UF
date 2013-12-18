package com.todo.code3.xml.form;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.todo.code3.misc.App;

public class FormParent extends LinearLayout {

	ArrayList<FormChild> children;

	public FormParent(Context context) {
		super(context);
		init();
	}

	public FormParent(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setBackgroundColor(0xff888888);
		
		children = new ArrayList<FormChild>();
	}
	
	public void addItem(String title,  String hint,int type) {
		children.add(new FormChild(getContext(), title, hint, type, this));
		addView(children.get(children.size() - 1));
	}
	
	public void hideOther(FormChild child) {
		for(FormChild f : children) {
			if(f != child && f.isContentVisible()) f.hideContent();
		}
		
		App.hideKeyboard(getContext(), this);
	}
	
	public void next() {
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).isContentVisible()){
				if(i+1 < children.size()) {
					children.get(i+1).showContent();
					hideOther(children.get(i+1));
				}
			}
		}
	}
}
