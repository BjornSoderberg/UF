package com.todo.code3.dialog;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import com.todo.code3.MainActivity;
import com.todo.code3.item.ContentItem;
import com.todo.code3.xml.HierarchyParent;

public class FolderSelectionDialog extends Dialog {

	private ArrayList<ContentItem> selectedItems;
	private AlertDialog alert;
	private JSONObject data;
	private HierarchyParent hierarchy;

	public FolderSelectionDialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, ArrayList<ContentItem> selectedItems, JSONObject data) {
		super(activity, title, message, hasVoiceRecognition);
		this.selectedItems = selectedItems;
		this.data = data;

		init();
	}

	public FolderSelectionDialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, ArrayList<ContentItem> selectedItems, JSONObject data, String posButtonString, String negButtonString) {
		super(activity, title, message, hasVoiceRecognition, posButtonString, negButtonString);
		this.selectedItems = selectedItems;
		this.data = data;

		init();
	}

	protected void init() {
		super.init();

		alert = create();

		hierarchy = new HierarchyParent(activity, data, selectedItems) {
			public void onItemSelected(int id, boolean selected) {
				alert.getButton(android.app.Dialog.BUTTON1).setEnabled(selected);
			}
		};

		alert.setView(hierarchy);
	}

	public void onVoiceRecognitionResult(Bundle result) {

	}

	protected Integer getResult() {
		return hierarchy.getSelectedItem();
	}

	public AlertDialog show() {
		alert.show();
		
		// This sets the position and prevents the alert dialog from
		// "lagging" when its children are animated
		alert.getWindow().setLayout((int)(activity.getContentWidth() * 0.9), LayoutParams.FILL_PARENT);
		alert.getWindow().setGravity(Gravity.CENTER);
		
		// The move button is disabled as default
		alert.getButton(android.app.Dialog.BUTTON1).setEnabled(false);
		
		return alert;
	}

	public AlertDialog getAlertDialog() {
		return alert;
	}
}
