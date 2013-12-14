package com.todo.code3;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

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
	private EditText inputInAddDialog;

	private Button dragButton, backButton;

	private JSONObject data;

	// the first folder should always be the inbox folder
	// this folder is selected in getDataFromSharedPreferences();
	// this may later be alterable
//	private int currentFolder = -1;
//	private int currentChecklist = -1;
//	private int currentTask = -1;
//	private String folderContentType;
	private int openObjectId = -1;
	private String openObjectType;

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
				data.put(App.NUM_TASKS, 0);
				data.put(App.NUM_CHECKLISTS, 0);
				data.put(App.NUM_FOLDERS, 0);

				addFolder("Inbox", App.FOLDER, false);

			} else {
				data = new JSONObject(d);
			}

			String[] childrenIds = data.getString(App.CHILDREN_IDS).split(",");
			for (int i = 0; i < childrenIds.length; i++) {
				if (data.has(App.FOLDER + childrenIds[i])) {
					JSONObject o = new JSONObject(data.getString(App.FOLDER + childrenIds[i]));

					if (o.getString(App.NAME).equalsIgnoreCase("Inbox")) {
						openFolder(Integer.parseInt(childrenIds[i]), o.getString(App.TYPE));
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

				// Set the views in the alert dialog
				LinearLayout l = new LinearLayout(MainActivity.this);
				Button button = new Button(MainActivity.this);
				if (button.getLayoutParams() != null) button.getLayoutParams().width = LayoutParams.FILL_PARENT;
				inputInAddDialog = new EditText(MainActivity.this);

				// Checks if voice recognition is present
				PackageManager pm = getPackageManager();
				List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

				if (activities.size() != 0 && App.isNetworkAvailable(MainActivity.this)) {
					button.setText("Press me to speak");
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							try {
								Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
								i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
								i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Add a new folder.");

								startActivityForResult(i, App.VOICE_RECOGNITION_REQUEST_CODE);
							} catch (Exception e) {
								Toast.makeText(MainActivity.this, "There was an error when trying to use the voice recongizer.", Toast.LENGTH_LONG).show();
							}
						}
					});

					l.addView(button);
				}

				l.setOrientation(LinearLayout.VERTICAL);
				l.addView(inputInAddDialog);

				alert.setView(l);
				alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = inputInAddDialog.getText().toString();
						addFolder(name, App.FOLDER);

						inputInAddDialog = null;
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

				// Set the views in the alert dialog
				LinearLayout l = new LinearLayout(MainActivity.this);
				Button button = new Button(MainActivity.this);
				if (button.getLayoutParams() != null) button.getLayoutParams().width = LayoutParams.FILL_PARENT;
				inputInAddDialog = new EditText(MainActivity.this);

				// Checks if voice recognition is present
				PackageManager pm = getPackageManager();
				List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

				if (activities.size() != 0 && App.isNetworkAvailable(MainActivity.this)) {
					button.setText("Press me to speak");
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							try {
								Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
								i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
								i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Add a new project.");

								startActivityForResult(i, App.VOICE_RECOGNITION_REQUEST_CODE);
							} catch (Exception e) {
								Toast.makeText(MainActivity.this, "There was an error when trying to use the voice recongizer.", Toast.LENGTH_LONG).show();
							}
						}
					});
					l.addView(button);
				}

				l.setOrientation(LinearLayout.VERTICAL);
				l.addView(inputInAddDialog);

				alert.setView(l);
				alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = inputInAddDialog.getText().toString();
						addFolder(name, App.PROJECT);

						inputInAddDialog = null;
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
			buttons[i].getLayoutParams().height = buttonSize;
			buttons[i].getLayoutParams().width = buttonSize;
		}

		((LinearLayout) findViewById(R.id.line1)).getLayoutParams().height = buttonSize;
		((LinearLayout) findViewById(R.id.line2)).getLayoutParams().height = buttonSize;

		((LinearLayout) findViewById(R.id.barBorder)).getLayoutParams().height = borderHeight;
		((LinearLayout) findViewById(R.id.bar)).getLayoutParams().height = barHeight;
	}

	private void updateData() {
		Log.i("Updating data...", data.toString());

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

		try {
			data.put(App.TIMESTAMP_LAST_UPDATED, (int) (System.currentTimeMillis() / 1000));
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		//When using a real XML file for the interface, remove the id "asd" from task_item
//		RelativeLayout r = (RelativeLayout) findViewById(R.id.bigWrapper);
//		View.inflate(this, R.layout.task_item, r);
//		final LinearLayout ti = (LinearLayout) findViewById(R.id.asd);
//		final int height = ti.getLayoutParams().height;
//		ti.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 1));
//		
//		Animation animation = new Animation() {
//			protected void applyTransformation(float time, Transformation t) {
//				if((int)(height * time) != 0) ti.getLayoutParams().height = (int) (height * time);
//				else ti.getLayoutParams().height = 1;
//				
//				ti.requestLayout();
//			}
//		};
//		animation.setDuration(2000);
//		ti.startAnimation(animation);
//		
//		if(true) return;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Add new task");
		alert.setMessage("What do you have to do?");

		// Set the views in the alert dialog
		LinearLayout l = new LinearLayout(this);
		Button button = new Button(this);
		if (button.getLayoutParams() != null) button.getLayoutParams().width = LayoutParams.FILL_PARENT;
		inputInAddDialog = new EditText(this);

		// Checks if voice recognition is present
		PackageManager pm = getPackageManager();
		List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0 && App.isNetworkAvailable(this)) {
			button.setText("Press me to speak");
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
						i.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you have to do?");

						startActivityForResult(i, App.VOICE_RECOGNITION_REQUEST_CODE);
					} catch (Exception e) {
						Toast.makeText(MainActivity.this, "There was an error when trying to use the voice recognizer.", Toast.LENGTH_LONG).show();
					}
				}
			});
			l.addView(button);
		}

		l.setOrientation(LinearLayout.VERTICAL);
		l.addView(inputInAddDialog);

		alert.setView(l);
		alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = inputInAddDialog.getText().toString();
				if (openObjectType.equals(App.FOLDER) || openObjectType.equals(App.CHECKLIST)) {
					addTask(name);
				} else if (openObjectType.equals(App.PROJECT)) addChecklist(name);

				inputInAddDialog = null;
			}
		});

		alert.setNegativeButton("Cancel", null);

		alert.show();
	}

	private void addTask(String name) {
		Log.i("Main Activity", "Added task " + name);

		data = App.addTask(name, openObjectId, openObjectType, data);

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

		data = App.addChecklist(name, openObjectId, App.FOLDER, data);
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
		addFolder(name, type, true);

		try {
			// This gets the id of the newly added folder
			getFlyInMenu().setExpandingItemId(data.getInt(App.NUM_FOLDERS) - 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addFolder(String name, String type, boolean removable) {
		data = App.addFolder(name, type, removable, data);
		editor.put(App.DATA, data.toString());

		updateMenu();
	}

	public void checkTask(int taskId, int parentId, String parentType, boolean isChecked) {
		data = App.checkTask(taskId, parentId, parentType, isChecked, data);

		editor.put(App.DATA, data.toString());
		updateData();
	}

	public void setTaskDescription(String desc, int taskId) {
		data = App.setTaskDescription(desc, taskId,  data);
	}

	private void openFolder(int id, String type) {
		if (isMoving) return;

		try {
			if(openObjectId == id && openObjectType.equals(type)) return;
			
			openObjectId = id;
			openObjectType = type;

			JSONObject object = new JSONObject(data.getString(App.FOLDER + openObjectId));

			setTitle(object.getString(App.NAME));

			posInWrapper = 0;
			contentViews.clear();

			if (type.equals(App.FOLDER)) {
//				if (object.getString(App.CONTENT_TYPE).equals(App.TASK)) {
					contentViews.add(posInWrapper, new TaskView(this, openObjectId, openObjectType));
//				} else if (object.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
//					contentViews.add(posInWrapper, new ChecklistView(this, openObjectId, openObjectType));
//				}
			} else if (type.equals(App.PROJECT)) {
				contentViews.add(posInWrapper, new ProjectView(this, openObjectId, openObjectType));
			}

			Log.i("Changed the current folder", object.getString(App.NAME));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		updateData();
	}

	public void open(int id, String type) {
		if (isMoving) return;

		Log.i("openTask", "Opened task");

		try {
			openObjectId = id;
			openObjectType = type;
			
			JSONObject object = new JSONObject(data.getString(type + id));
			
			setTitle(object.getString(App.NAME));

			scroller.startScroll(currentContentOffset, 0, -width, 0, scrollDuration);
			scrollHandler.postDelayed(scrollRunnable, scrollFps);

			posInWrapper++;

			if (contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);
			
			if(type.equals(App.TASK))
			contentViews.add(posInWrapper, new TaskContentView(this, openObjectId));
			else if(type.equals(App.CHECKLIST))contentViews.add(posInWrapper, new TaskView(this, openObjectId, openObjectType)); 

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

		// this means that it is not in any checklist or task
		if (openObjectType.equals(App.FOLDER) || openObjectType.equals(App.PROJECT)) return;

		try {
			JSONObject object = new JSONObject(data.getString(openObjectType + openObjectId));
	
			setTitle(new JSONObject(data.getString(object.getString(App.PARENT_CONTENT_TYPE) + object.getInt(App.PARENT_ID))).getString(App.NAME));

			openObjectId = object.getInt(App.PARENT_ID);
			openObjectType = object.getString(App.PARENT_CONTENT_TYPE);
			

			if (openObjectType.equals(App.FOLDER) || openObjectType.equals(App.PROJECT)) {
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

	public void updateChilrenOrder(String children, int parentId, String parentType) {
		data = App.updateChildrenOrder(children, parentId, parentType, data);

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

	protected class AnimationRunnable implements Runnable {
		public void run() {
			isMoving = true;
			boolean isAnimationOngoing = scroller.computeScrollOffset();

			adjustContentPosition(isAnimationOngoing);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == App.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			inputInAddDialog.setText(App.capitalizeFirstWordInSentences(results.get(0)));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public FlyInMenu getFlyInMenu() {
		return super.getFlyInMenu();
	}

	public int getMenuWidth() {
		return menuWidth;
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

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < getFlyInMenu().getMenuItems().size() - 1; i++) {
			order += getFlyInMenu().getMenuItems().get(i).getId() + ",";
		}

		order += getFlyInMenu().getMenuItems().get(getFlyInMenu().getMenuItems().size() - 1).getId();

		updateChilrenOrder(order, -1, "");
	}
}