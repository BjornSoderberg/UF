package com.todo.code2;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.todo.code2.misc.App;
import com.todo.code2.xml.FlyOutContainer;

public class MenuHandler {

	private MainActivity activity;

	private LinearLayout menu, list; 
	private FlyOutContainer root;

	public MenuHandler(MainActivity activity, FlyOutContainer root, LinearLayout menu) {
		this.activity = activity;
		this.menu = menu;
		this.root = root;

		init();
	}

	private void init() {
		Log.i("menu handler init", menu.getChildCount() + "");

		ScrollView sv = (ScrollView) menu.findViewById(R.id.menuScroll);
		list = (LinearLayout) sv.findViewById(R.id.menuList);
		
		final Button add = new Button(activity);
		add.setText("+   Add new folder");
		add.setBackgroundColor(0);
		add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(activity);
				alert.setTitle("Add new folder");

				// Set edit text to get user input
				final EditText input = new EditText(activity);
				alert.setView(input);
				alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = input.getText().toString();
						activity.addFolder(name);
					}
				});

				alert.setNegativeButton("Cancel", null);

				alert.show();
			}
		});

		list.addView(add);

		// if (list == null) return;
		//
		// list.addView(new FolderItem(context, "This is a folder"));
	}

	public void updateData() {
		//new UpdateMenu().execute();
		
		
		
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				JSONObject data = activity.getData();

				Log.i("Menu Handler", "Updating data...");
				
				//list.removeAllViews();

				try {
					JSONObject folder = null;
					for (int i = 0; i < data.getInt(App.NUM_CHILDREN); i++) {
						if (data.has(App.FOLDER + i)) {
							folder = new JSONObject(data.getString(App.FOLDER + i));

							int numChildren = 0;

							for (int j = 0; j < folder.getInt(App.NUM_CHILDREN); j++) {
								JSONObject child = new JSONObject(folder.getString(folder.getString(App.CONTENT_TYPE) + j));
								if (child.has(App.REMOVED) && !child.getBoolean(App.REMOVED)) numChildren++;
								else if (!child.has(App.REMOVED)) numChildren++;
							}

							//if ((folder.has(App.REMOVED) && !folder.getBoolean(App.REMOVED)) || !folder.has(App.REMOVED)) //
							//list.addView(new FolderItem(activity, folder.getString(App.NAME) + " - " + numChildren));
							list.addView(new Button(activity));
							//list.removeAllViews();

						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (list.getChildCount() == 0) {
					TextView tv = new TextView(activity);
					tv.setText("There was an error loading the folders");
					//list.addView(tv);
				}

				list.postInvalidate();
				menu.postInvalidate();
				root.postInvalidate();
				
				list.invalidate();
				menu.invalidate();
				root.invalidate();
				
				menu.recomputeViewAttributes(list);
				root.recomputeViewAttributes(menu);
				
				menu.refreshDrawableState();
				list.refreshDrawableState();
				root.refreshDrawableState();
				
				//Log.i("Update data - root", root + "");				
			}});
		
	}
	
	/*
	class UpdateMenu extends AsyncTask<Void, Void, FlyOutContainer> {
		protected FlyOutContainer doInBackground(Void... arg0) {
			JSONObject data = activity.getData();

			Log.i("Menu Handler", "Updating data...");
			
			//list.removeAllViews();

			try {
				JSONObject folder = null;
				for (int i = 0; i < data.getInt(App.NUM_CHILDREN); i++) {
					if (data.has(App.FOLDER + i)) {
						folder = new JSONObject(data.getString(App.FOLDER + i));

						int numChildren = 0;

						for (int j = 0; j < folder.getInt(App.NUM_CHILDREN); j++) {
							JSONObject child = new JSONObject(folder.getString(folder.getString(App.CONTENT_TYPE) + j));
							if (child.has(App.REMOVED) && !child.getBoolean(App.REMOVED)) numChildren++;
							else if (!child.has(App.REMOVED)) numChildren++;
						}

						//if ((folder.has(App.REMOVED) && !folder.getBoolean(App.REMOVED)) || !folder.has(App.REMOVED)) //
						//list.addView(new FolderItem(activity, folder.getString(App.NAME) + " - " + numChildren));
						list.addView(new Button(activity));
						//list.removeAllViews();

					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (list.getChildCount() == 0) {
				TextView tv = new TextView(activity);
				tv.setText("There was an error loading the folders");
				//list.addView(tv);
			}

			list.postInvalidate();
			menu.postInvalidate();
			root.postInvalidate();
			
			list.invalidate();
			menu.invalidate();
			root.invalidate();
			return root;
		}
	}*/
}
