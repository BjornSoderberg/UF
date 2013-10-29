package com.todo.code3;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.espian.flyin.library.FlyInFragmentActivity;
import com.espian.flyin.library.FlyInMenu;
import com.espian.flyin.library.FlyInMenuItem;
import com.todo.code3.misc.App;
import com.todo.code3.misc.SPEditor;
import com.todo.code3.view.ChecklistView;
import com.todo.code3.view.ContentView;
import com.todo.code3.view.TaskContentView;
import com.todo.code3.view.TaskView;

public class MainActivity extends FlyInFragmentActivity {

	private SharedPreferences prefs;
	private SPEditor editor;

	private LinearLayout wrapper;
	private TextView nameTV;
	private Button mAddFolderButton;

	private Button dragButton, backButton;

	private JSONObject data;

	// the first folder should always be the inbox folder
	// this folder is selected in getDataFromSharedPreferences();
	// this may later be alterable
	private int currentFolder = -1;
	private int currentChecklist = -1;
	private int currentTask = -1;
	private String folderContentType;

	public ArrayList<ContentView> contentViews;

	// For scrolling between a checklist and its tasks
	private Scroller scroller;
	private Runnable scrollRunnable;
	private Handler scrollHandler;
	private boolean isMoving = false;
	private int scrollDuration = 300;
	private int currentContentOffset = 0;
	private int posInWrapper = 0;
	private long scrollFps = 1000 / 60;

	private int width, height;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this makes the app go fullscreen
		// this solved the issue about the wrapper being to big
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//scroller = new Scroller(this, new SmoothInterpolator());
		scroller = new Scroller(this, AnimationUtils.loadInterpolator(this,
				android.R.anim.decelerate_interpolator));
		scrollRunnable = new AnimationRunnable();
		scrollHandler = new Handler();

		DisplayMetrics dm = getResources().getDisplayMetrics();
		width = dm.widthPixels;
		height = dm.heightPixels;

		setContentView(R.layout.wrapper);

		prefs = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = new SPEditor(prefs);

		contentViews = new ArrayList<ContentView>();

		initXML();
		initBar();

		loadFlyInMenu();

		getDataFromSharedPreferences();
	}

	private void getDataFromSharedPreferences() {
		try {
			String d = prefs.getString(App.DATA, null);

			if (d == null) {
				data = new JSONObject();
				data.put(App.NUM_CHILDREN, 0);

				addFolder("Inbox", App.TASK, false);

			} else {
				data = new JSONObject(d);
			}

			for (int i = 0; i < data.getInt(App.NUM_CHILDREN); i++) {
				JSONObject o = new JSONObject(data.getString(App.FOLDER + i));

				if (o.getString(App.NAME).equalsIgnoreCase("Inbox")) {
					openFolder(i);
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateData();
	}

	private void initXML() {
		wrapper = (LinearLayout) findViewById(R.id.wrapper);

		nameTV = (TextView) findViewById(R.id.name);

		dragButton = (Button) findViewById(R.id.dragButton);
		backButton = (Button) findViewById(R.id.backButton);

		mAddFolderButton = new Button(this);
		mAddFolderButton.setText("+  Add new folder");
		mAddFolderButton.setBackgroundColor(0);
		mAddFolderButton.setTextColor(0xff888888);
		mAddFolderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
				alert.setTitle("Add new folder");

				// Set edit text to get user input
				final EditText input = new EditText(MainActivity.this);
				alert.setView(input);
				alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = input.getText().toString();
						addFolder(name);
					}
				});

				alert.setNegativeButton("Cancel", null);

				alert.show();
			}
		});

		getFlyInMenu().setCustomView(mAddFolderButton);
	}

	private void initBar() {
		int barHeight = height / 12 - height / 120;
		int buttonSize = barHeight;
		int borderHeight = height / 120;

		Button[] buttons = { dragButton, backButton, (Button) findViewById(R.id.addButton) };

		for (int i = 0; i < buttons.length; i++) {
			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) buttons[i].getLayoutParams();
			p.height = buttonSize;
			p.width = buttonSize;
			buttons[i].setLayoutParams(p);
		}

		LinearLayout l = (LinearLayout) findViewById(R.id.barBorder);
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) l.getLayoutParams();
		p.height = borderHeight;
		l.setLayoutParams(p);
	}

	private void updateData() {
		// removes the view that are not next to the right of the view the user
		// sees
		for (int i = 0; i < contentViews.size(); i++) {
			if (i > posInWrapper + 1) contentViews.remove(i);
		}

		wrapper.removeAllViews();
		for (ContentView view : contentViews) {
			wrapper.addView(view);
			view.update(data);
		}

		updateMenu();
	}

	private void updateMenu() {
		FlyInMenu menu = getFlyInMenu();

		menu.clearMenuItems();

		try {
			for (int i = 0; i < data.getInt(App.NUM_CHILDREN); i++) {
				if (data.has(App.FOLDER + i)) {
					JSONObject folder = new JSONObject(data.getString(App.FOLDER + i));
					FlyInMenuItem mi = new FlyInMenuItem();
					mi.setTitle(folder.getString(App.NAME) + " - " + folder.getInt(App.NUM_CHILDREN));
					mi.setId(folder.getInt(App.ID));
					// mi.setIcon(res id);
					menu.addMenuItem(mi);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		menu.setMenuItems();
	}

	public boolean onFlyInItemClick(FlyInMenuItem item, int position) {
		try {
			JSONObject folder = new JSONObject(data.getString(App.FOLDER + item.getId()));

			openFolder(folder.getInt(App.ID));
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void toggleMenu(View v) {
		getFlyInMenu().toggleMenu();
	}

	public void viewAddTaskDialog(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Add new task");
		alert.setMessage("What to you have to do?");

		// Set edit text to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = input.getText().toString();
				if (folderContentType.equals(App.TASK) || currentChecklist != -1) {
					addTask(name);
				} else if (folderContentType.equals(App.CHECKLIST)) addChecklist(name);
			}
		});

		alert.setNegativeButton("Cancel", null);

		alert.show();
	}

	private void addTask(String name) {
		Log.i("Main Activity", "Added task " + name);

		try {
			JSONObject taskData = new JSONObject();
			taskData.put(App.NAME, name);

			JSONObject folder = new JSONObject(data.getString(App.FOLDER + currentFolder));

			if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
				JSONObject cl = new JSONObject(folder.getString(App.CHECKLIST + currentChecklist));

				taskData.put(App.PARENT_CONTENT_TYPE, App.CHECKLIST);
				taskData.put(App.PARENT_ID, currentChecklist);

				taskData.put(App.ID, cl.getInt(App.NUM_CHILDREN));

				cl.put(App.TASK + cl.getInt(App.NUM_CHILDREN), taskData.toString());
				cl.put(App.NUM_CHILDREN, cl.getInt(App.NUM_CHILDREN) + 1);

				folder.put(App.CHECKLIST + currentChecklist, cl.toString());
			} else if (folder.getString(App.CONTENT_TYPE).equals(App.TASK)) {
				taskData.put(App.PARENT_CONTENT_TYPE, App.FOLDER);
				taskData.put(App.PARENT_ID, currentFolder);

				taskData.put(App.ID, folder.getInt(App.NUM_CHILDREN));

				folder.put(App.TASK + folder.getInt(App.NUM_CHILDREN), taskData.toString());
				folder.put(App.NUM_CHILDREN, folder.getInt(App.NUM_CHILDREN) + 1);
			}

			data.put(App.FOLDER + folder.getInt(App.ID), folder.toString());
			editor.put(App.DATA, data.toString());

			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void addChecklist(String name) {
		Log.i("Main Activity", "Added checklist " + name);

		try {
			JSONObject checklistData = new JSONObject();
			checklistData.put(App.NAME, name);
			checklistData.put(App.PARENT_ID, currentFolder);
			checklistData.put(App.PARENT_CONTENT_TYPE, App.CHECKLIST);

			JSONObject folder = new JSONObject(data.getString(App.FOLDER + currentFolder));

			if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
				checklistData.put(App.ID, folder.getInt(App.NUM_CHILDREN));
				checklistData.put(App.NUM_CHILDREN, 0);

				folder.put(App.CHECKLIST + folder.getInt(App.NUM_CHILDREN), checklistData.toString());
				folder.put(App.NUM_CHILDREN, folder.getInt(App.NUM_CHILDREN) + 1);
			} /*
			 * else
			 * 
			 * if (folder.getString(App.CONTENT_TYPE).equals(App.TASK)) {
			 * checklistData.put(App.ID, folder.getInt(App.NUM_CHILDREN));
			 * 
			 * folder.put(App.TASK + folder.getInt(App.NUM_CHILDREN),
			 * checklistData.toString()); folder.put(App.NUM_CHILDREN,
			 * folder.getInt(App.NUM_CHILDREN) + 1); }
			 */

			data.put(App.FOLDER + folder.getInt(App.ID), folder.toString());
			editor.put(App.DATA, data.toString());

			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addFolder(String name) {
		addFolder(name, App.CHECKLIST, true);
	}

	public void addFolder(String name, String contentType, boolean removable) {
		try {
			JSONObject folderData = new JSONObject();
			folderData.put(App.NAME, name);
			folderData.put(App.CONTENT_TYPE, contentType);
			folderData.put(App.ID, data.getInt(App.NUM_CHILDREN));
			folderData.put(App.REMOVABLE, removable);

			folderData.put(App.NUM_CHILDREN, 0);
			// if (contentType == App.CHECKLIST)
			// folderData.put(App.NUM_CHILDREN, 0);
			// if (contentType == App.TASK) folderData.put(App.NUM_CHILDREN, 0);

			data.put(App.FOLDER + data.getInt(App.NUM_CHILDREN), folderData.toString());
			data.put(App.NUM_CHILDREN, data.getInt(App.NUM_CHILDREN) + 1);

			editor.put(App.DATA, data.toString());

			Log.i("Main Activity", "Added folder " + name);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			updateMenu();
		}
	}

	public void checkTask(int taskId, int checklistId, int folderId, boolean isChecked) {
		Log.i("completeTask", taskId + ", " + checklistId + ", " + folderId);

		try {
			JSONObject folder = new JSONObject(data.getString(App.FOLDER + folderId));

			if (checklistId != -1 /* && folder.has(App.CHECKLIST + checklistId) */) {
				JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + checklistId));
				JSONObject task = new JSONObject(checklist.getString(App.TASK + taskId));

				task.put(App.COMPLETED, isChecked);

				checklist.put(App.TASK + taskId, task.toString());
				folder.put(App.CHECKLIST + checklistId, checklist.toString());
				data.put(App.FOLDER + folderId, folder.toString());
			} else if (folder.has(App.TASK + taskId)) {
				JSONObject task = new JSONObject(folder.getString(App.TASK + taskId));

				task.put(App.COMPLETED, isChecked);

				folder.put(App.TASK + taskId, task.toString());
				data.put(App.FOLDER + folderId, folder.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			editor.put(App.DATA, data.toString());
			updateData();

			Log.i("Compelted Task", "Completed Task");
		}
	}

	// previously known as setCurrentFolder
	private void openFolder(int i) throws JSONException {
		if (isMoving) return;

		currentChecklist = -1;

		if (currentFolder == i) return;
		currentFolder = i;
		// this should also load the correct data...

		JSONObject folder = new JSONObject(data.getString(App.FOLDER + i));

		nameTV.setText(folder.getString(App.NAME));

		folderContentType = folder.getString(App.CONTENT_TYPE);

		posInWrapper = 0;
		contentViews.clear();
		
		if(folderContentType.equals(App.TASK)) {
			contentViews.add(posInWrapper, new TaskView(this, currentFolder, currentChecklist));
		} else if(folderContentType.equals(App.CHECKLIST)) {
			contentViews.add(posInWrapper, new ChecklistView(this, currentFolder));
		}

//		if (folderContentType.equals(App.TASK)) {
//			if (contentViews.size() + 1 == posInWrapper) contentViews.remove(posInWrapper);
//
//			if (contentViews.size() - 1 > posInWrapper && contentViews.get(posInWrapper) instanceof TaskView) ((TaskView) contentViews.get(posInWrapper)).setFolderAndChecklist(currentFolder,
//					currentChecklist);
//			else contentViews.add(posInWrapper, new TaskView(this, currentFolder, currentChecklist));
//		} else if (folderContentType.equals(App.CHECKLIST)) {
//			// contentViews.add(new ChecklistView(this, currentFolder));
//			if (contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);
//			contentViews.clear();
//
//			if (contentViews.size() - 1 > posInWrapper && contentViews.get(posInWrapper) instanceof ChecklistView) ((ChecklistView) contentViews.get(posInWrapper)).setCurrentFolder(currentFolder);
//			else contentViews.add(posInWrapper, new ChecklistView(this, currentFolder));
//		}

		Log.i("Changed the current folder", folder.getString(App.NAME));

		updateData();
	}

	public void openChecklist(JSONObject checklist) {
		if (isMoving) return;

		Log.i("openChecklist", "Opened checklist");

		try {
			currentChecklist = checklist.getInt(App.ID);
			nameTV.setText(checklist.getString(App.NAME));

			scroller.startScroll(currentContentOffset, 0, -width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, 1000 / 60);

			posInWrapper++;

			/*if (contentViews.size() - 1 > posInWrapper && contentViews.get(posInWrapper) instanceof TaskView) ((TaskView) contentViews.get(posInWrapper)).setFolderAndChecklist(currentFolder,
					currentChecklist);
			else */
			if(contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);
			contentViews.add(posInWrapper, new TaskView(this, currentFolder, currentChecklist));

			backButton.setVisibility(View.VISIBLE);
			dragButton.setVisibility(View.GONE);
			updateData();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void openTask(JSONObject task) {
		if (isMoving) return;

		Log.i("openTask", "Opened task");

		try {
			currentTask = task.getInt(App.ID);
			nameTV.setText(task.getString(App.NAME));

			scroller.startScroll(currentContentOffset, 0, -width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, scrollFps);

			posInWrapper++;

			/*if (contentViews.size() - 1 > posInWrapper && contentViews.get(posInWrapper) instanceof TaskContentView) //
			((TaskContentView) contentViews.get(posInWrapper)).setFolderAndChecklistAndTask(currentFolder, currentChecklist, currentTask);
			else */
			if(contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);
			contentViews.add(posInWrapper, new TaskContentView(this, currentFolder, currentChecklist, currentTask));

			backButton.setVisibility(View.VISIBLE);
			dragButton.setVisibility(View.GONE);
			updateData();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void goBack(View v) {
		if (isMoving) return;

		// this means that it is not in anything
		if (currentChecklist == -1 && currentTask == -1) return;

		try {
			if (currentTask == -1) {
				currentChecklist = -1;

				nameTV.setText(new JSONObject(data.getString(App.FOLDER + currentFolder)).getString(App.NAME));
				// cListView.setAdapter(checklistAdapter);

			} else {
				currentTask = -1;

				String name;
				if (currentChecklist == -1) {
					name = new JSONObject(data.getString(App.FOLDER + currentFolder)).getString(App.NAME);
				} else {
					name = new JSONObject(new JSONObject(data.getString(App.FOLDER + currentFolder)).getString(App.CHECKLIST + currentChecklist)).getString(App.NAME);
				}

				nameTV.setText(name);
			}
			
			if(currentChecklist == -1 && currentTask == -1) {
				backButton.setVisibility(View.GONE);
				dragButton.setVisibility(View.VISIBLE);
			}
			
			scroller.startScroll(currentContentOffset, 0, width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, scrollFps);

			posInWrapper--;
			updateData();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getContentWidth() {
		return width;
	}

	public int getContentHeight() {
		// the height of the bar (including the border) is 1 / 12 of the height
		// (see @initBar())
		return height * 11 / 12;
	}

	public JSONObject getData() {
		return data;
	}

	private void adjustContentPosition(boolean isAnimationOngoing) {
		int offset = scroller.getCurrX();

		LayoutParams params = new LayoutParams(width, height);

		params.setMargins(offset, 0, -offset, 0);
		for (int i = 0; i < wrapper.getChildCount(); i++) {
			wrapper.getChildAt(i).setLayoutParams(params);
		}

		wrapper.invalidate();

		if (isAnimationOngoing) scrollHandler.postDelayed(scrollRunnable, 16);
		else {
			currentContentOffset = offset;
			isMoving = false;
		}

	}

	protected class SmoothInterpolator implements Interpolator {
		public float getInterpolation(float t) {
			return (float) Math.pow(t - 1, 5) + 1;
		}
	}

	protected class AnimationRunnable implements Runnable {
		public void run() {
			isMoving = true;
			boolean isAnimationOngoing = scroller.computeScrollOffset();

			adjustContentPosition(isAnimationOngoing);
		}
	}
}