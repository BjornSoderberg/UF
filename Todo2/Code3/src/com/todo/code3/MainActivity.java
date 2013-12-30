package com.todo.code3;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.flyin.library.FlyInFragmentActivity;
import com.espian.flyin.library.FlyInMenu;
import com.espian.flyin.library.FlyInMenuItem;
import com.todo.code3.animation.ChangeSizeAnimation;
import com.todo.code3.animation.CollapseAnimation;
import com.todo.code3.animation.ExpandAnimation;
import com.todo.code3.dialog.AddItemDialog;
import com.todo.code3.dialog.Dialog;
import com.todo.code3.dialog.TextLineDialog;
import com.todo.code3.misc.App;
import com.todo.code3.misc.SPEditor;
import com.todo.code3.view.ContentView;
import com.todo.code3.view.ItemView;
import com.todo.code3.view.TaskContentView;
import com.todo.code3.xml.OptionsBar;
import com.todo.code3.xml.Wrapper;

public class MainActivity extends FlyInFragmentActivity {

	private SharedPreferences prefs;
	private SPEditor editor;

	private LinearLayout wrapper;
	private TextView nameTV;
	private EditText focusDummy, nameET;
	private OptionsBar options;
	private Button saveButton;
	private FrameLayout dragButton, backButton;

	private JSONObject data;

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

	private int width, height;

	private Dialog dialogBoxForOnActivityResult;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		scroller = new Scroller(this, AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));
		scrollRunnable = new AnimationRunnable();
		scrollHandler = new Handler();

		DisplayMetrics dm = getResources().getDisplayMetrics();
		width = dm.widthPixels;
		height = dm.heightPixels - App.getStatusBarHeight(getResources());

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

				addMenuItem("Inbox", App.FOLDER);
			} else {
				data = new JSONObject(d);
			}

			boolean hasOpened = false;

			String[] childrenIds;
			if (data.has(App.CHILDREN_IDS)) childrenIds = data.getString(App.CHILDREN_IDS).split(",");
			else childrenIds = new String[0];

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

			// Opens the folder at the top of the menu
			// The order is reversed since the order of the menu is reversed
			if (!hasOpened) {
				for (int i = childrenIds.length - 1; i >= 0; i--) {
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
		nameET = (EditText) findViewById(R.id.nameET);
		focusDummy = (EditText) findViewById(R.id.focusDummy);

		dragButton = (FrameLayout) findViewById(R.id.dragButton);
		backButton = (FrameLayout) findViewById(R.id.backButton);

		options = (OptionsBar) findViewById(R.id.bottomBar);
		options.setMainActivity(this);

		saveButton = (Button) findViewById(R.id.saveButton);

		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				endEditTitle(true);
			}
		});

		// This is the view that encapsulates the title
		((RelativeLayout) findViewById(R.id.nameTouchArea)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startEditTitle();
			}
		});

		nameET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) App.showKeyboard(MainActivity.this);
				else {
					App.hideKeyboard(MainActivity.this, focusDummy);
					endEditTitle(true);
				}
			}
		});

		focusDummy.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) App.hideKeyboard(MainActivity.this, focusDummy);
			}
		});

		initAddButton();
	}

	private void initAddButton() {
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
				TextLineDialog b = new TextLineDialog(MainActivity.this, "Add new folder", null, true, "Add", "Cancel") {
					protected void onResult(Object result) {
						super.onResult(result);

						if (result instanceof String) addMenuItem((String) result, App.FOLDER);
					}
				};

				b.show();
			}
		});

		customView.addView(addFolderButton);
		getFlyInMenu().setCustomView(customView);
	}

	private void initBars() {
		// Together the button height and the border
		// height are as high as the bar
		int barContentHeight = getBarHeight() * 9 / 10;
		int borderHeight = getBarHeight() * 1 / 10;

		FrameLayout[] buttons = { dragButton, backButton, (FrameLayout) findViewById(R.id.addButton) };

		for (int i = 0; i < buttons.length; i++) {
			buttons[i].getLayoutParams().height = barContentHeight;
			buttons[i].getLayoutParams().width = barContentHeight;

			((ImageView) buttons[i].findViewById(R.id.icon)).getLayoutParams().height = (int) (barContentHeight * 0.8);
			((ImageView) buttons[i].findViewById(R.id.icon)).getLayoutParams().width = (int) (barContentHeight * 0.8);
		}

		((LinearLayout) findViewById(R.id.line1)).getLayoutParams().height = barContentHeight;
		((LinearLayout) findViewById(R.id.line2)).getLayoutParams().height = barContentHeight;

		((LinearLayout) findViewById(R.id.barBorder)).getLayoutParams().height = borderHeight;
		((LinearLayout) findViewById(R.id.topBar)).getLayoutParams().height = barContentHeight;

		options.getLayoutParams().height = getBarHeight();
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

	public void toggleMenu() {
		if (!isMoving) getFlyInMenu().toggleMenu();
	}

	public void showMenu() {
		if (!isMoving) getFlyInMenu().showMenu();

		hideOptions();
		if (isEditingTitle()) endEditTitle(false);
		App.hideKeyboard(this, focusDummy);
	}

	public void hideMenu() {
		if (!isMoving) getFlyInMenu().hideMenu();
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

		AddItemDialog i = new AddItemDialog(this, "Add new", "Select type", null, "Cancel") {
			public void onResult(String name, String type) {
				super.onResult(name, type);
				add(name, type);
			}
		};

		i.show();
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

	public void addMenuItem(String name, String type) {
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

	public void remove(int id) {
		remove(id, true);
	}

	public void remove(int id, boolean update) {
		data = App.remove(id, data);
		editor.put(App.DATA, data.toString());

		if (update) updateData();
	}

	public void move(int id, int parentId) {
		move(id, parentId, true);
	}

	public void move(int id, int parentId, boolean update) {
		data = App.move(id, parentId, data);
		editor.put(App.DATA, data.toString());

		if (update) updateData();
	}

	public void removeWithChildren(int id) {
		removeWithChildrenLoop(id);

		updateData();
	}

	public void removeWithChildrenLoop(int id) {
		try {
			JSONObject object = new JSONObject(data.getString(id + ""));

			if (object.has(App.CHILDREN_IDS) && !object.getString(App.CHILDREN_IDS).equals("")) {
				String[] childrenIds = object.getString(App.CHILDREN_IDS).split(",");

				for (String string : childrenIds) {
					removeWithChildrenLoop(Integer.parseInt(string));
				}
			} else remove(id, false);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		data = App.remove(id, data);
		editor.put(App.DATA, data.toString());
	}

	public void prioritize(int id, boolean prioritized) {
		data = App.setProperty(App.PRIORITIZED, prioritized, id, data);
		editor.put(App.DATA, data.toString());

		updateData();
	}

	public void groupItemsInNewFolder(String newFolderName, int[] itemIds) {
		add(newFolderName, App.FOLDER);

		if (itemIds.length == 0) return;

		try {
			JSONObject newFolder = new JSONObject(data.getString((data.getInt(App.NUM_IDS) - 1) + ""));

			// Gets the old folder from the first item in the itemIds array's
			// parent id
			JSONObject child = new JSONObject(data.getString(itemIds[0] + ""));
			JSONObject oldFolder = new JSONObject(data.getString(child.getInt(App.PARENT_ID) + ""));

			String newChildrenString = "";
			for (int id : itemIds) {
				// builds the new children string
				newChildrenString += id + ",";

				// removes the children ids from the old parent's string
				if (data.has(id + "")) {
					data = App.setProperty(App.PARENT_ID, newFolder.getInt(App.ID), id, data);
				}
			}

			newChildrenString = newChildrenString.substring(0, newChildrenString.length() - 1);
			data = App.setProperty(App.CHILDREN_IDS, newChildrenString, newFolder.getInt(App.ID), data);

			String oldChildrenString = App.removeFromChildrenString(oldFolder, itemIds);
			data = App.setProperty(App.CHILDREN_IDS, oldChildrenString, oldFolder.getInt(App.ID), data);

			editor.put(App.DATA, data.toString());

			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public void setNewName(String name, int id) {
		data = App.setProperty(App.NAME, name, id, data);
		editor.put(App.DATA, data.toString());
		updateData();
	}

	private void openMenuItem(int id) {
		if (isMoving) return;
		hideOptions();
		App.hideKeyboard(this, focusDummy);

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
		if (isEditingTitle()) endEditTitle(false);
		App.hideKeyboard(this, focusDummy);

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
	}

	public void goBack() {
		if (isMoving) return;

		hideOptions();
		if (isEditingTitle()) endEditTitle(false);
		App.hideKeyboard(this, focusDummy);

		try {
			JSONObject object = new JSONObject(data.getString(openObjectId + ""));

			setTitle(new JSONObject(data.getString(object.getInt(App.PARENT_ID) + "")).getString(App.NAME));

			openObjectId = object.getInt(App.PARENT_ID);
			object = new JSONObject(data.getString(openObjectId + ""));

			if (object.getString(App.TYPE).equals(App.FOLDER)) {
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
	}

	private void startEditTitle() {
		nameTV.setVisibility(View.GONE);
		nameET.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.VISIBLE);

		String name = nameTV.getText().toString();

		// Gets the real name of the open object (just in case)
		try {
			JSONObject o = new JSONObject(data.getString(openObjectId + ""));
			if (o.has(App.NAME)) name = o.getString(App.NAME);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		nameET.setText(name);
		nameET.requestFocus();
	}

	private void endEditTitle(boolean save) {
		nameTV.setVisibility(View.VISIBLE);
		nameET.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);

		focusDummy.requestFocus();

		if (save) {
			setNewName(nameET.getText().toString(), openObjectId);
			nameTV.setText(nameET.getText().toString());
		}
	}

	private boolean isEditingTitle() {
		return nameET.getVisibility() == View.VISIBLE;
	}

	public void toggleOptions() {
		if (options.getVisibility() == View.GONE) showOptions();
		else hideOptions();
	}

	public void showOptions() {
		if (isEditingTitle()) endEditTitle(false);

		options.setVisibility(View.VISIBLE);
		options.getLayoutParams().height = 1;

		if (contentViews.get(posInWrapper) instanceof ItemView) ((ItemView) contentViews.get(posInWrapper)).enterOptionsMode();

		new ExpandAnimation(options, App.ANIMATION_DURATION, getBarHeight()).animate();

		options.clearOptionsItems();
		options.addOptionsItem(App.OPTIONS_REMOVE);
		options.addOptionsItem(App.OPTIONS_GROUP_ITEMS);
		options.addOptionsItem(App.OPTIONS_SELECT_ALL);
		options.addOptionsItem(App.OPTIONS_MOVE);

		updateData();
	}

	public void hideOptions() {
		if (0 <= posInWrapper && posInWrapper < contentViews.size()) //
		if (contentViews.get(posInWrapper) instanceof ItemView) ((ItemView) contentViews.get(posInWrapper)).exitOptionsMode();

		// Makes the hide animation only if the view is not already hidden
		if (options.getVisibility() != View.GONE) {
			new CollapseAnimation(options, App.ANIMATION_DURATION, getBarHeight()).animate();

			new Handler().postDelayed(new Runnable() {
				public void run() {
					options.setVisibility(View.GONE);

					// Waits for the hide animation to finish before
					// updating the data (and the views)
					updateData();
				}
			}, App.ANIMATION_DURATION);
		} else updateData();
	}

	public void updateChildrenOrder(String children, int parentId) {
		data = App.updateChildrenOrder(children, parentId, data);

		editor.put(App.DATA, data.toString());
		updateData();
	}

	public void onBackPressed() {
		if (isMoving) return;

		if (getFlyInMenu().isMenuVisible()) {
			hideMenu();
			return;
		}

		if (isEditingTitle()) {
			endEditTitle(true);
			return;
		}

		if (contentViews.get(posInWrapper) instanceof ItemView) {
			if (((ItemView) contentViews.get(posInWrapper)).isInOptionsMode()) {
				hideOptions();
				return;
			}
		}

		if (posInWrapper != 0) {
			goBack();
			return;
		}

		super.onBackPressed();
	}

	protected class AnimationRunnable implements Runnable {
		public void run() {
			isMoving = true;
			boolean isAnimationOngoing = scroller.computeScrollOffset();

			adjustContentPosition(isAnimationOngoing);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (dialogBoxForOnActivityResult != null) dialogBoxForOnActivityResult.onActivityResult(requestCode, resultCode, data);

		super.onActivityResult(requestCode, resultCode, data);
	}

	public FlyInMenu getFlyInMenu() {
		return super.getFlyInMenu();
	}

	public int getMenuWidth() {
		if (width * 0.8 > App.dpToPx(300, getResources()) && App.dpToPx(300, getResources()) < width) return App.dpToPx(300, getResources());
		else return (int) (width * 0.8);
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

	public FrameLayout getDragButton() {
		return dragButton;
	}

	public ContentView getOpenContentView() {
		return contentViews.get(posInWrapper);
	}

	public int getBarHeight() {
		if (getResources().getDimension(R.dimen.item_height) < height * 1 / 12) return height * 1 / 12;
		else return (int) getResources().getDimension(R.dimen.item_height);
	}

	public int getContentWidth() {
		return width;
	}

	public int getContentHeight() {
		return height - getBarHeight();
	}

	public JSONObject getData() {
		return data;
	}

	public void startVoiceRecognition(Dialog b, String message) {
		dialogBoxForOnActivityResult = b;

		try {
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			if (message != null) i.putExtra(RecognizerIntent.EXTRA_PROMPT, message);

			startActivityForResult(i, App.VOICE_RECOGNITION_REQUEST_CODE);
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, "There was an error when trying to use the voice recongizer.", Toast.LENGTH_LONG).show();
		}
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

	protected void onStart() {
		super.onResume();
		App.hideKeyboard(this, focusDummy);
	}
}