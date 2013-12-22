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
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.flyin.library.FlyInFragmentActivity;
import com.espian.flyin.library.FlyInMenu;
import com.espian.flyin.library.FlyInMenuItem;
import com.todo.code3.misc.App;
import com.todo.code3.misc.SPEditor;
import com.todo.code3.view.ContentView;
import com.todo.code3.view.ItemView;
import com.todo.code3.view.TaskContentView;
import com.todo.code3.xml.Wrapper;

public class MainActivity extends FlyInFragmentActivity {

	private SharedPreferences prefs;
	private SPEditor editor;

	private LinearLayout wrapper;
	private TextView nameTV;
	private EditText inputInAddDialog;
	private LinearLayout options;

	private Button dragButton, backButton;

	private JSONObject data;

	// the first folder should always be the inbox folder
	// this folder is selected in getDataFromSharedPreferences();
	// this may later be alterable
	// private int currentFolder = -1;
	// private int currentChecklist = -1;
	// private int currentTask = -1;
	// private String folderContentType;
	private int openObjectId = -1;

	public ArrayList<ContentView> contentViews;

	// For scrolling between a checklist and its tasks
	private Scroller scroller;
	private Runnable scrollRunnable;
	private Handler scrollHandler;
	private boolean isMoving = false;
	private int currentContentOffset = 0;
	private int posInWrapper = 0;
	private long scrollFps = 1000 / 60;

	private int width, height, menuWidth, barHeight;

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
		initBars();

		loadFlyInMenu(getMenuWidth());

		getDataFromSharedPreferences();
	}

	private void getDataFromSharedPreferences() {
		try {
			String d = prefs.getString(App.DATA, null);

			if (d == null) {
				data = new JSONObject();
				data.put(App.NUM_IDS, 0);

				addFolder("Inbox", App.FOLDER);
			} else {
				data = new JSONObject(d);
			}

			boolean hasOpened = false;

			String[] childrenIds = data.getString(App.CHILDREN_IDS).split(",");
			for (int i = 0; i < childrenIds.length; i++) {
				if (data.has(childrenIds[i])) {
					JSONObject o = new JSONObject(data.getString(childrenIds[i]));
					if (o.getString(App.NAME).equals("Inbox")) {
						openMenuItem(Integer.parseInt(childrenIds[i]));
						hasOpened = true;
						break;

					}
				}
			}

			if (!hasOpened) {
				for (int i = 0; i < childrenIds.length; i++) {
					if (data.has(childrenIds[i])) {
						JSONObject o = new JSONObject(data.getString(childrenIds[i]));
						if (/* o.getString(App.TYPE).equals(App.PROJECT) || */o.getString(App.TYPE).equals(App.FOLDER)) {
							openMenuItem(Integer.parseInt(childrenIds[i]));
							break;
						}
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

		options = (LinearLayout) findViewById(R.id.bottomBar);
		options.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(isMoving) return;
				
				if(contentViews.get(posInWrapper) instanceof ItemView) {
					ItemView i = (ItemView) contentViews.get(posInWrapper);
					if(i.isInOptionsMode()) i.removeSelectedItems();
				}
			}
		});

		initAddButtons();
	}

	private void initAddButtons() {
		// Makes the custom view
		LinearLayout customView = new LinearLayout(this);
		customView.setOrientation(LinearLayout.VERTICAL);

		Button addFolderButton = new Button(this);
		addFolderButton.setText("+  Add new folder");
		// Makes it transparent
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

		customView.addView(addFolderButton);
		getFlyInMenu().setCustomView(customView);
	}

	private void initBars() {
		barHeight = height / 12 - height / 120;
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
		((LinearLayout) findViewById(R.id.topBar)).getLayoutParams().height = barHeight;

		options.getLayoutParams().height = buttonSize;
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

			for (int i = childrenIds.length - 1; i >= 0; i--) {
				String id = childrenIds[i];
				if (data.has(id)) {
					JSONObject folder = new JSONObject(data.getString(id));
					if (folder.getString(App.TYPE).equals(App.FOLDER)) {
						// If the folder does not have a parent
						// it should be in the menu.
						if (folder.getString(App.TYPE).equals(App.FOLDER)) {
							if (folder.has(App.PARENT_ID) && folder.getInt(App.PARENT_ID) != -1) continue;
						}

						FlyInMenuItem mi = new FlyInMenuItem();
						int numChildren = (folder.getString(App.CHILDREN_IDS).length() == 0) ? 0 : folder.getString(App.CHILDREN_IDS).split(",").length;
						mi.setTitle(folder.getString(App.NAME) + " - " + numChildren + " (" + folder.getString(App.TYPE) + ")");
						mi.setId(folder.getInt(App.ID));
						mi.setType(folder.getString(App.TYPE));
						// mi.setIcon(res id);
						menu.addMenuItem(mi);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		menu.setMenuItems();
	}

	public boolean onFlyInItemClick(FlyInMenuItem item, int position) {
		try {
			JSONObject object = new JSONObject(data.getString(item.getId() + ""));

			if (object.getString(App.TYPE).equals(App.FOLDER)) openMenuItem(object.getInt(App.ID));

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
		// RelativeLayout r = (RelativeLayout) findViewById(R.id.bigWrapper);
		// FolderHierarchyViewer f = new FolderHierarchyViewer(this, data, -1,
		// 0);
		// f.setLayoutParams(new
		// RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT));
		// f.setBackgroundColor(0xffff00ff);
		// r.addView(f);
		//
		// if (true) return;

		// RelativeLayout r = (RelativeLayout) findViewById(R.id.bigWrapper);
		// final FormParent ti = new FormParent(this);
		// r.addView(ti);
		//
		// ti.addItem("Name", "Enter a task name", FormChild.TEXT_LINE);
		// ti.addItem("Other thing", "Enter another thing",
		// FormChild.TEXT_LINE);
		//
		// ti.setLayoutParams(new
		// RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 1));
		// ti.setBackgroundColor(0x33000000);
		// ti.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// ti.setBackgroundColor(0xff0000ff);
		// }
		// });
		//
		// Animation animation = new Animation() {
		// protected void applyTransformation(float time, Transformation t) {
		// if ((int) (height * time) != 0) ti.getLayoutParams().height = (int)
		// (height * time);
		// else ti.getLayoutParams().height = 1;
		//
		// ti.requestLayout();
		// }
		// };
		// animation.setDuration(500);
		// ti.startAnimation(animation);
		//
		// if (true) return;
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog alert = builder.create();
		alert.setTitle("Add new");
		alert.setMessage("Select type");

		LinearLayout l = new LinearLayout(this);

		Button task = new Button(this);
		task.setText("Task");
		task.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				askForName(App.TASK);
				alert.cancel();
			}
		});
		Button folder = new Button(this);
		folder.setText("Folder");
		folder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				askForName(App.FOLDER);
				alert.cancel();
			}
		});
		Button note = new Button(this);
		note.setText("Note");
		note.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				askForName(App.NOTE);
				alert.cancel();
			}
		});

		l.addView(task);
		l.addView(folder);
		l.addView(note);

		alert.setView(l);

		alert.show();
	}

	private void askForName(final String type) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		if (type.equals(App.TASK)) {
			builder.setTitle("Add new task");
			builder.setMessage("What do you have to do?");
		}
		if (type.equals(App.FOLDER)) {
			builder.setTitle("Add new folder");
			builder.setMessage("Name your folder!");
		}
		if (type.equals(App.NOTE)) {
			builder.setTitle("Add new note");
			builder.setMessage("What's on your mind?");
		}

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

		builder.setView(l);
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = inputInAddDialog.getText().toString();
				add(name, type);

				inputInAddDialog = null;
			}
		});

		builder.setNegativeButton("Cancel", null);

		builder.show();
	}

	private void add(String name, String type) {
		data = App.add(name, type, openObjectId, data);
		editor.put(App.DATA, data.toString());

		try {
			int id = data.getInt(App.NUM_IDS) - 1;

			if (contentViews.get(posInWrapper) instanceof ItemView) {
				((ItemView) contentViews.get(posInWrapper)).setExpandingItemId(id);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateData();
	}

	public void remove(int id) {
		data = App.remove(id, data);
		editor.put(App.DATA, data.toString());
		
		updateData();
	}
	
	public void addFolder(String name, String type) {
		data = App.add(name, type, -1, data);
		editor.put(App.DATA, data.toString());

		try {
			// This gets the id of the newly added folder
			getFlyInMenu().setExpandingItemId(data.getInt(App.NUM_IDS) - 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateMenu();
	}

	public void checkTask(int taskId, int parentId, boolean isChecked) {
		data = App.checkTask(taskId, parentId, isChecked, data);

		editor.put(App.DATA, data.toString());
		updateData();
	}

	public void setTaskDescription(String desc, int id) {
		data = App.setProperty(App.DESCRIPTION, desc, id, data);
		editor.put(App.DATA, data.toString());
		updateData();
	}

	private void openMenuItem(int id) {
		if (isMoving) return;

		try {
			if (openObjectId == id) return;

			openObjectId = id;

			JSONObject object = new JSONObject(data.getString(openObjectId + ""));

			setTitle(object.getString(App.NAME));

			posInWrapper = 0;
			contentViews.clear();

			contentViews.add(posInWrapper, new ItemView(this, openObjectId));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		updateData();
	}

	public void open(int id) {
		if (isMoving) return;
		hideOptions();

		try {
			openObjectId = id;

			JSONObject object = new JSONObject(data.getString(id + ""));

			setTitle(object.getString(App.NAME));

			scroller.startScroll(currentContentOffset, 0, -width, 0, App.ANIMATION_DURATION);
			scrollHandler.postDelayed(scrollRunnable, scrollFps);

			posInWrapper++;

			if (contentViews.size() > posInWrapper) contentViews.remove(posInWrapper);

			if (object.getString(App.TYPE).equals(App.TASK)) contentViews.add(posInWrapper, new TaskContentView(this, openObjectId));
			else contentViews.add(posInWrapper, new ItemView(this, openObjectId));

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
		hideOptions();

		try {
			JSONObject object = new JSONObject(data.getString(openObjectId + ""));

			setTitle(new JSONObject(data.getString(object.getInt(App.PARENT_ID) + "")).getString(App.NAME));

			openObjectId = object.getInt(App.PARENT_ID);
			object = new JSONObject(data.getString(openObjectId + ""));

			/*
			 * if (object.getString(App.TYPE).equals(App.PROJECT)) {
			 * backButton.setVisibility(View.GONE);
			 * dragButton.setVisibility(View.VISIBLE); } else
			 */if (object.getString(App.TYPE).equals(App.FOLDER)) {
				JSONObject o = new JSONObject(data.getString(openObjectId + ""));
				if (!o.has(App.PARENT_ID) || o.getInt(App.PARENT_ID) == -1) {
					backButton.setVisibility(View.GONE);
					dragButton.setVisibility(View.VISIBLE);
				}
			}

			scroller.startScroll(currentContentOffset, 0, width, 0, App.ANIMATION_DURATION);
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

	public void toggleOptions() {
		if (options.getVisibility() == View.GONE) showOptions();
		else hideOptions();
	}

	public void showOptions() {
		options.setVisibility(View.VISIBLE);
		options.getLayoutParams().height = 1;

		if (contentViews.get(posInWrapper) instanceof ItemView) ((ItemView)contentViews.get(posInWrapper)).enterOptionsMode();

		Animation animation = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				if ((int) (time * barHeight) != 0) options.getLayoutParams().height = (int) (time * barHeight);
				else options.getLayoutParams().height = 1;
				options.requestLayout();
			}
		};
		animation.setDuration(App.ANIMATION_DURATION);
		options.startAnimation(animation);

		// new Handler().postDelayed(new Runnable() {
		// public void run() {
		//
		// }
		// }, App.ANIMATION_DURATION);

//		options.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				hideOptions();
//			}
//		});
		
		updateData();
	}

	public void hideOptions() {
		if (contentViews.get(posInWrapper) instanceof ItemView) ((ItemView)contentViews.get(posInWrapper)).exitOptionsMode();
		
		Animation animation = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				if (barHeight - (int) (time * barHeight) != 0) options.getLayoutParams().height = barHeight - (int) (time * barHeight);
				else options.getLayoutParams().height = 1;

				options.requestLayout();
			}
		};
		animation.setDuration(App.ANIMATION_DURATION);
		options.startAnimation(animation);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				options.setVisibility(View.GONE);
			}
		}, App.ANIMATION_DURATION);
		
		updateData();
	}

	public void updateChildrenOrder(String children, int parentId) {
		data = App.updateChildrenOrder(children, parentId, data);

		editor.put(App.DATA, data.toString());
		updateData();
	}

	public void onBackPressed() {
		if(contentViews.get(posInWrapper) instanceof ItemView) {
			if(((ItemView)contentViews.get(posInWrapper)).isInOptionsMode()) {
				hideOptions();
				return;
			}
		}
		
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

		// Menu items are listed in reverse order when compared to
		// the other items. Therefore, I put the last item first.
		for (int i = getFlyInMenu().getMenuItems().size() - 1; i >= 0; i--) {
			order += getFlyInMenu().getMenuItems().get(i).getId() + ",";
		}

		// Removes the last ',' from the string
		order = order.substring(0, order.length() - 1);

		updateChildrenOrder(order, -1);
	}
}