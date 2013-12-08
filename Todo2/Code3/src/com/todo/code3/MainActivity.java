package com.todo.code3;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
import com.todo.code3.view.ProjectView;
import com.todo.code3.view.TaskContentView;
import com.todo.code3.view.TaskView;
import com.todo.code3.xml.Wrapper;

public class MainActivity extends FlyInFragmentActivity {

	private SharedPreferences prefs;
	private SPEditor editor;

	private LinearLayout wrapper;
	private TextView nameTV;

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

	private int width, height, menuWidth;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this makes the app go fullscreen
		// this solved the issue about the wrapper being to big
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// scroller = new Scroller(this, new SmoothInterpolator());
		scroller = new Scroller(this, AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));
		scrollRunnable = new AnimationRunnable();
		scrollHandler = new Handler();

		DisplayMetrics dm = getResources().getDisplayMetrics();
		width = dm.widthPixels;
		height = dm.heightPixels - App.getStatusBarHeight(getResources());
		menuWidth = (int) (width * 0.8);

		Log.i("asdasdsa", height + ", " + dm.heightPixels);

		setContentView(R.layout.wrapper);

		prefs = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = new SPEditor(prefs);

		contentViews = new ArrayList<ContentView>();

		initXML();
		initBar();

		loadFlyInMenu(getMenuWidth());

		getDataFromSharedPreferences();
	}

	private void getDataFromSharedPreferences() {
		try {
			String d = prefs.getString(App.DATA, null);

			if (d == null) {
				data = new JSONObject();
				data.put(App.CONTENT_TYPE, App.FOLDER);
				data.put(App.NUM_TASKS, 0);
				data.put(App.NUM_CHECKLISTS, 0);
				data.put(App.NUM_FOLDERS, 0);

				addFolder("Inbox", App.TASK, App.FOLDER, false);

			} else {
				data = new JSONObject(d);
			}

			for (int i = 0; i < data.getInt(App.NUM_FOLDERS); i++) {
				if (data.has(App.FOLDER + i)) {
					JSONObject o = new JSONObject(data.getString(App.FOLDER + i));

					if (o.getString(App.NAME).equalsIgnoreCase("Inbox")) {
						openFolder(i, o.getString(App.TYPE));
						break;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateData();
	}

	private void initXML() {
		// Gives the wrapper (which makes swiping the menu open) possible
		((Wrapper) findViewById(R.id.bigWrapper)).setActivity(this);

		wrapper = (LinearLayout) findViewById(R.id.wrapper);

		nameTV = (TextView) findViewById(R.id.name);

		dragButton = (Button) findViewById(R.id.dragButton);
		backButton = (Button) findViewById(R.id.backButton);

		initAddButtons();
	}

	private void initAddButtons() {
		// Makes the custom view
		LinearLayout customView = new LinearLayout(this);
		customView.setOrientation(LinearLayout.VERTICAL);

		Button addFolderButton = new Button(this);
		addFolderButton.setText("+  Add new folder");
		addFolderButton.setBackgroundColor(0);
		addFolderButton.setTextColor(getResources().getColor(com.espian.flyin.library.R.color.rbm_item_text_color));
		addFolderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
				alert.setTitle("Add new folder");

				// Set edit text to get user input
				final EditText input = new EditText(MainActivity.this);
				alert.setView(input);
				alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = input.getText().toString();
						addFolder(name, App.FOLDER);
					}
				});

				alert.setNegativeButton("Cancel", null);

				alert.show();
			}
		});

		Button addProjectButton = new Button(this);
		addProjectButton.setText("+  Add new project");
		addProjectButton.setBackgroundColor(0);
		addProjectButton.setTextColor(0xff888888);
		addProjectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
				alert.setTitle("Add new project");

				// Set edit text to get user input
				final EditText input = new EditText(MainActivity.this);
				alert.setView(input);
				alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = input.getText().toString();
						addFolder(name, App.PROJECT);
					}
				});

				alert.setNegativeButton("Cancel", null);

				alert.show();
			}
		});

		customView.addView(addFolderButton);
		customView.addView(addProjectButton);
		getFlyInMenu().setCustomView(customView);
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
		// Log.i("Updating data...", data.toString());

		// removes the view that are not next
		// to the right of the view the user sees
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
			String[] childrenIds;

			if (data.has(App.CHILDREN_IDS)) childrenIds = data.getString(App.CHILDREN_IDS).split(",");
			else childrenIds = new String[0];

			for (int i = 0; i < childrenIds.length; i++) {
				String id = childrenIds[i];
				if (data.has(App.FOLDER + id)) {
					JSONObject folder = new JSONObject(data.getString(App.FOLDER + id));
					FlyInMenuItem mi = new FlyInMenuItem();
					int numChildren = (folder.getString(App.CHILDREN_IDS).length() == 0) ? 0 : folder.getString(App.CHILDREN_IDS).split(",").length;
					mi.setTitle(folder.getString(App.NAME) + " - " + numChildren + " (" + folder.getString(App.TYPE) + ")");
					mi.setId(folder.getInt(App.ID));
					mi.setType(folder.getString(App.TYPE));
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
			JSONObject object = new JSONObject(data.getString(App.FOLDER + item.getId()));

			openFolder(object.getInt(App.ID), object.getString((App.TYPE)));
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void toggleMenu(View v) {
		if (!isMoving) getFlyInMenu().toggleMenu();
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

		data = App.addTask(name, currentChecklist, currentFolder, data);

		// this should be the id of the new task
		int id = -1;
		try {
			id = data.getInt(App.NUM_TASKS) - 1;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (contentViews.get(posInWrapper) instanceof TaskView) {
			((TaskView) contentViews.get(posInWrapper)).setExpandingItemId(id);
		}

		editor.put(App.DATA, data.toString());
		updateData();
	}

	private void addChecklist(String name) {
		Log.i("Main Activity", "Added checklist " + name);

		data = App.addChecklist(name, currentFolder, data);
		editor.put(App.DATA, data.toString());

		int id = -1;
		try {
			id = data.getInt(App.NUM_CHECKLISTS) - 1;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (contentViews.get(posInWrapper) instanceof ChecklistView) {
			((ChecklistView) contentViews.get(posInWrapper)).setExpandingItemId(id);
		} else if (contentViews.get(posInWrapper) instanceof ProjectView) {
			((ProjectView) contentViews.get(posInWrapper)).setExpandingItemId(id);
		}

		updateData();
	}

	public void addFolder(String name, String type) {
		addFolder(name, App.CHECKLIST, type, true);
	}

	public void addFolder(String name, String contentType, String type, boolean removable) {
		data = App.addFolder(name, contentType, type, removable, data);
		editor.put(App.DATA, data.toString());

		updateMenu();
	}

	public void checkTask(int taskId, int checklistId, int folderId, boolean isChecked) {
		Log.i("completeTask", taskId + ", " + checklistId + ", " + folderId);

		data = App.checkTask(taskId, checklistId, folderId, isChecked, data);

		editor.put(App.DATA, data.toString());
		updateData();
	}

	public void setTaskDescription(String desc, int taskId, int checklistId, int folderId) {
		data = App.setTaskDescription(desc, taskId, checklistId, folderId, data);
	}

	// previously known as setCurrentFolder
	// this function also opens projects
	private void openFolder(int id, String type) {
		if (isMoving) return;

		try {
			currentChecklist = -1;

			if (currentFolder == id) return;
			currentFolder = id;
			// this should also load the correct data...

			JSONObject object = new JSONObject(data.getString(App.FOLDER + id));
			// if (type.equals(App.FOLDER)) object = new
			// JSONObject(data.getString(App.FOLDER + id));
			// else if (type.equals(App.PROJECT)) object = new
			// JSONObject(data.getString(App.PROJECT + id));

			setTitle(object.getString(App.NAME));

			folderContentType = object.getString(App.CONTENT_TYPE);

			posInWrapper = 0;
			contentViews.clear();

			if (type.equals(App.FOLDER)) {
				if (folderContentType.equals(App.TASK)) {
					contentViews.add(posInWrapper, new TaskView(this, currentFolder, currentChecklist));
				} else if (folderContentType.equals(App.CHECKLIST)) {
					contentViews.add(posInWrapper, new ChecklistView(this, currentFolder));
				}
			} else if (type.equals(App.PROJECT)) {
				contentViews.add(posInWrapper, new ProjectView(this, currentFolder));
			}

			Log.i("Changed the current folder", object.getString(App.NAME));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		updateData();
	}

	public void openChecklist(JSONObject checklist) {
		if (isMoving) return;

		Log.i("openChecklist", "Opened checklist");

		try {
			currentChecklist = checklist.getInt(App.ID);
			setTitle(checklist.getString(App.NAME));

			scroller.startScroll(currentContentOffset, 0, -width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, 1000 / 60);

			posInWrapper++;

			/*
			 * if (contentViews.size() - 1 > posInWrapper &&
			 * contentViews.get(posInWrapper) instanceof TaskView) ((TaskView)
			 * contentViews
			 * .get(posInWrapper)).setFolderAndChecklist(currentFolder,
			 * currentChecklist); else
			 */
			if (contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);
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
			setTitle(task.getString(App.NAME));

			scroller.startScroll(currentContentOffset, 0, -width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, scrollFps);

			posInWrapper++;

			if (contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);
			contentViews.add(posInWrapper, new TaskContentView(this, currentFolder, currentChecklist, currentTask));

			backButton.setVisibility(View.VISIBLE);
			dragButton.setVisibility(View.GONE);
			updateData();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void goBack(View v) {
		goBack();

		// hides the keyboard if it is open
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public void goBack() {
		if (isMoving) return;

		Log.i("asdasdas", posInWrapper + ", ");

		// this means that it is not in any checklist or task
		if (currentChecklist == -1 && currentTask == -1) return;

		try {
			if (currentTask == -1) {
				currentChecklist = -1;

				setTitle(new JSONObject(data.getString(App.FOLDER + currentFolder)).getString(App.NAME));

			} else {
				currentTask = -1;

				String name;
				if (currentChecklist == -1) {
					name = new JSONObject(data.getString(App.FOLDER + currentFolder)).getString(App.NAME);
				} else {
					name = new JSONObject(new JSONObject(data.getString(App.FOLDER + currentFolder)).getString(App.CHECKLIST + currentChecklist)).getString(App.NAME);
				}

				setTitle(name);
			}

			if (currentChecklist == -1 && currentTask == -1) {
				backButton.setVisibility(View.GONE);
				dragButton.setVisibility(View.VISIBLE);
			}

			scroller.startScroll(currentContentOffset, 0, width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, scrollFps);

			contentViews.get(posInWrapper).leave();
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

	private void setTitle(String name) {
		Log.i(nameTV.getWidth() + "", width + "");

		nameTV.setText(name);

		Rect bounds = new Rect();
		Paint paint = nameTV.getPaint();
		paint.getTextBounds(name, 0, name.length(), bounds);
		int w = bounds.width();

		// The add button has the same width as the drag button
		// The *0.8 is added to get some margins
		while (w >= (width - dragButton.getWidth() * 2) * 0.8) {
			bounds = new Rect();
			paint = nameTV.getPaint();
			paint.getTextBounds(name, 0, name.length(), bounds);

			w = bounds.width();

			name = name.substring(0, name.length() - 1);
			nameTV.setText(name + "...");
		}
	}

	public void updateChilrenOrder(String children, int checklistId, int folderId) {
		data = App.updateChildrenOrder(children, checklistId, folderId, data);

		editor.put(App.DATA, data.toString());
		updateData();
	}

	public void onBackPressed() {
		if (posInWrapper == 0) {
			if (getFlyInMenu().isMenuVisible()) getFlyInMenu().hideMenu();
			else super.onBackPressed();
		} else {
			goBack();
		}
	}

	public FlyInMenu getFlyInMenu() {
		return super.getFlyInMenu();
	}

	public int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	public int getMenuWidth() {
		return menuWidth;
	}

	protected class AnimationRunnable implements Runnable {
		public void run() {
			isMoving = true;
			boolean isAnimationOngoing = scroller.computeScrollOffset();

			adjustContentPosition(isAnimationOngoing);
		}
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void isMoving(boolean b) {
		isMoving = b;
	}

	public int getPosInWrapper() {
		return posInWrapper;
	}

	public Button getDragButton() {
		return dragButton;
	}
}