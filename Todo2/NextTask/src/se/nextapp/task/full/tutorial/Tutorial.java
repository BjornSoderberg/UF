package se.nextapp.task.full.tutorial;

import org.json.JSONException;
import org.json.JSONObject;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.view.ItemView;
import se.nextapp.task.full.view.TaskView;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Tutorial {

	public static int itemToBeClicked = -1;
	public static int prioritizedId = -1;
	public static int selectedOptionsItem = -1;
	public static boolean wentBackOnce = false;

	public static void positionItems(final TextView text, final ImageView img, final TutorialState e, final MainActivity a) {
		text.setVisibility(View.GONE);
		img.setVisibility(View.GONE);

		a.setColors();
		a.getOpenContentView().setColors();

		new Handler().postDelayed(new Runnable() {
			public void run() {
				a.setColors();
				a.getOpenContentView().setColors();

				final RelativeLayout.LayoutParams textParams = ((RelativeLayout.LayoutParams) text.getLayoutParams());
				final RelativeLayout.LayoutParams imgParams = ((RelativeLayout.LayoutParams) img.getLayoutParams());

				textParams.setMargins(10, 10, 10, 10);

				if (e == TutorialState.INTRO) {
					textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					img.setVisibility(View.GONE);

					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ADD_TASK) {
					textParams.addRule(RelativeLayout.BELOW, img.getId());
					textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

					imgParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					imgParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

					text.setVisibility(View.VISIBLE);
					img.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ENTER_TASK) {
					textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					try {
						for (int id = a.getData().getInt(App.NUM_IDS) - 1; id >= 0; id--) {
							if (a.getOpenContentView() instanceof ItemView) {
								ItemView i = (ItemView) a.getOpenContentView();
								View v = i.getViewById(id);
								if (v != null && new JSONObject(a.getData().getString(id + "")).getString(App.TYPE).equals(App.TASK)) {
									// v.setBackgroundColor(0xff883399);
									itemToBeClicked = id;

									int[] imgCoords = new int[2];
									img.getLocationInWindow(imgCoords);
									imgCoords[1] -= App.getStatusBarHeight(a.getResources());

									int[] vCoords = new int[2];
									v.getLocationInWindow(vCoords);
									vCoords[1] -= App.getStatusBarHeight(a.getResources());

									// Sets x and y coordinates
									// (View.setX/Y()
									// is not available until API 11)
									int x = (vCoords[0] + a.getContentWidth()) / 2;
									if (a.getContentWidth() - x < imgParams.width) x = a.getContentWidth() - imgParams.width;
									imgParams.leftMargin = x;
									imgParams.topMargin = vCoords[1];

									if (a.getContentHeight() - imgParams.height - imgParams.topMargin > text.getLineCount() * text.getTextSize()) {
										textParams.addRule(RelativeLayout.BELOW, img.getId());
									} else {
										textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
									}

									a.updateData();
									break;
								}
							}
						}
					} catch (JSONException ex) {
						ex.printStackTrace();
					}

					img.setVisibility(View.VISIBLE);
					text.setVisibility(View.VISIBLE);

				} else if (e == TutorialState.INTRODUCE_TASK) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.SET_DUE_DATE) {
					if (a.getOpenContentView() instanceof TaskView) {
						TaskView t = (TaskView) a.getOpenContentView();
						View v = t.getDueDateView();
						v.setBackgroundColor(0xff883377);
					}

					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.SET_REMINDER) {
					if(a.getOpenContentView() instanceof TaskView) {
						((TaskView) a.getOpenContentView()).setColors();
					}
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.CHANGE_NAME) {
					a.getNameTV().setBackgroundColor(0xff8899dd);

					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.GO_BACK) {
					a.getBackButton().setBackgroundColor(0xffffffff);

					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.CHECK_TASK) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.PRIORITIZE_TASK) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ADD_FOLDER) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ENTER_FOLDER) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.INTRODUCE_FOLDER) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ADD_NOTE) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ENTER_NOTE) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.INTRODUCE_NOTE) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.CHANGE_DESCRIPTION) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.GO_BACK2) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ENABLE_OPTIONS) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.CHANGE_ORDER) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.REMOVE_ITEMS) {
					text.setVisibility(View.VISIBLE);
					selectedOptionsItem = App.OPTIONS_REMOVE;
					a.getOptionsBar().updateItems();
				} else if (e == TutorialState.GROUP_ITEMS) {
					text.setVisibility(View.VISIBLE);
					selectedOptionsItem = App.OPTIONS_GROUP_ITEMS;
					a.getOptionsBar().updateItems();
				} else if (e == TutorialState.SELECT_ALL) {
					text.setVisibility(View.VISIBLE);
					selectedOptionsItem = App.OPTIONS_SELECT_ALL;
					a.getOptionsBar().updateItems();
				} else if (e == TutorialState.MOVE_ITEMS) {
					text.setVisibility(View.VISIBLE);
					selectedOptionsItem = App.OPTIONS_MOVE;
					a.getOptionsBar().updateItems();
				} else if (e == TutorialState.DISABLE_OPTIONS) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.SHOW_MENU) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.ENABLE_MENU_OPTIONS) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.OPEN_SETTINGS) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.CHANGE_THEME) {
					text.setVisibility(View.VISIBLE);
				} else if (e == TutorialState.OUTRO) {
					text.setVisibility(View.VISIBLE);
				} else {
					a.endTutorial();
				}
			}
		}, App.ANIMATION_DURATION * 2);
	}

	public static TutorialState getNextTutorial(boolean madeAction, TutorialState current, MainActivity a) {
		// madeAction = true; // Change later
		if (current == TutorialState.END) return TutorialState.INTRO;
		if (current == TutorialState.INTRO) return TutorialState.ADD_TASK;
		if (current == TutorialState.ADD_TASK && madeAction) return TutorialState.ENTER_TASK;
		if (current == TutorialState.ENTER_TASK && madeAction) return TutorialState.INTRODUCE_TASK;
		if (current == TutorialState.INTRODUCE_TASK) return TutorialState.SET_DUE_DATE;
		if (current == TutorialState.SET_DUE_DATE && madeAction) return TutorialState.SET_REMINDER;
		if (current == TutorialState.SET_REMINDER && madeAction) return TutorialState.CHANGE_NAME;
		if (current == TutorialState.CHANGE_NAME && madeAction) return TutorialState.GO_BACK;
		if (current == TutorialState.GO_BACK && madeAction) return TutorialState.CHECK_TASK;
		if (current == TutorialState.CHECK_TASK && madeAction) return TutorialState.PRIORITIZE_TASK;
		if (current == TutorialState.PRIORITIZE_TASK && madeAction) return TutorialState.ADD_FOLDER;

		if (current == TutorialState.ADD_FOLDER && madeAction) return TutorialState.ENTER_FOLDER;
		if (current == TutorialState.ENTER_FOLDER && madeAction) return TutorialState.INTRODUCE_FOLDER;
		if (current == TutorialState.INTRODUCE_FOLDER) return TutorialState.ADD_NOTE;
		if (current == TutorialState.ADD_NOTE && madeAction) return TutorialState.ENTER_NOTE;
		if (current == TutorialState.ENTER_NOTE && madeAction) return TutorialState.INTRODUCE_NOTE;
		if (current == TutorialState.INTRODUCE_NOTE) return TutorialState.CHANGE_DESCRIPTION;
		if (current == TutorialState.CHANGE_DESCRIPTION && madeAction) return TutorialState.GO_BACK2;
		if (current == TutorialState.GO_BACK2 && madeAction) return TutorialState.ENABLE_OPTIONS;

		if (current == TutorialState.ENABLE_OPTIONS && madeAction) return TutorialState.CHANGE_ORDER;
		if (current == TutorialState.CHANGE_ORDER && madeAction) return TutorialState.REMOVE_ITEMS;
		if (current == TutorialState.REMOVE_ITEMS) return TutorialState.GROUP_ITEMS;
		if (current == TutorialState.GROUP_ITEMS) return TutorialState.SELECT_ALL;
		if (current == TutorialState.SELECT_ALL) return TutorialState.MOVE_ITEMS;
		if (current == TutorialState.MOVE_ITEMS) return TutorialState.DISABLE_OPTIONS;
		if (current == TutorialState.DISABLE_OPTIONS && madeAction) {
			if (!a.isInMasterView()) return TutorialState.SHOW_MENU;
			else return TutorialState.ENABLE_MENU_OPTIONS;
		}

		if (current == TutorialState.SHOW_MENU && madeAction) return TutorialState.ENABLE_MENU_OPTIONS;
		if (current == TutorialState.ENABLE_MENU_OPTIONS && madeAction) return TutorialState.OPEN_SETTINGS;
		if (current == TutorialState.OPEN_SETTINGS && madeAction) return TutorialState.CHANGE_THEME;
		if (current == TutorialState.CHANGE_THEME && madeAction) return TutorialState.OUTRO;
		if (current == TutorialState.OUTRO) return TutorialState.END;

		return current;
	}

	public static TutorialState getPrevTutorial(boolean performAction, TutorialState current, MainActivity a) {
		if (current == TutorialState.INTRO) return TutorialState.END;
		if (current == TutorialState.ADD_TASK) return TutorialState.INTRO;
		if (current == TutorialState.ENTER_TASK) return TutorialState.ADD_TASK;
		if (current == TutorialState.INTRODUCE_TASK) {
			if (performAction) a.goBack();
			return TutorialState.ENTER_TASK;
		}
		if (current == TutorialState.SET_DUE_DATE) return TutorialState.INTRODUCE_TASK;
		if (current == TutorialState.SET_REMINDER) return TutorialState.SET_DUE_DATE;
		if (current == TutorialState.CHANGE_NAME) return TutorialState.SET_REMINDER;
		if (current == TutorialState.GO_BACK) return TutorialState.CHANGE_NAME;
		if (current == TutorialState.CHECK_TASK) {
			if (performAction) a.open(a.getLastOpenObjectId());
			return TutorialState.GO_BACK;
		}
		if (current == TutorialState.PRIORITIZE_TASK) return TutorialState.CHECK_TASK;

		if (current == TutorialState.ADD_FOLDER) return TutorialState.PRIORITIZE_TASK;
		if (current == TutorialState.ENTER_FOLDER) return TutorialState.ADD_FOLDER;
		if (current == TutorialState.INTRODUCE_FOLDER) {
			if (performAction) a.goBack();
			return TutorialState.ENTER_FOLDER;
		}
		if (current == TutorialState.ADD_NOTE) return TutorialState.INTRODUCE_FOLDER;
		if (current == TutorialState.ENTER_NOTE) return TutorialState.ADD_NOTE;
		if (current == TutorialState.INTRODUCE_NOTE) {
			if (performAction) a.goBack();
			return TutorialState.ENTER_NOTE;
		}
		if (current == TutorialState.CHANGE_DESCRIPTION) return TutorialState.INTRODUCE_NOTE;
		if (current == TutorialState.GO_BACK2) return TutorialState.CHANGE_DESCRIPTION;
		if (current == TutorialState.ENABLE_OPTIONS) {
			if (performAction) a.open(a.getLastOpenObjectId());
			return TutorialState.GO_BACK2;
		}
		if (current == TutorialState.CHANGE_ORDER) {
			if (performAction) a.disableOptions();
			return TutorialState.ENABLE_OPTIONS;
		}
		if (current == TutorialState.REMOVE_ITEMS) return TutorialState.CHANGE_ORDER;
		if (current == TutorialState.GROUP_ITEMS) return TutorialState.REMOVE_ITEMS;
		if (current == TutorialState.SELECT_ALL) return TutorialState.GROUP_ITEMS;
		if (current == TutorialState.MOVE_ITEMS) return TutorialState.SELECT_ALL;
		if (current == TutorialState.DISABLE_OPTIONS) {
			if (performAction) a.enableOptions();
			return TutorialState.MOVE_ITEMS;
		}
		if (current == TutorialState.DISABLE_OPTIONS) {
			if (performAction) a.enableOptions();
			return TutorialState.MOVE_ITEMS;
		}
		if (current == TutorialState.SHOW_MENU) {
			if (performAction) a.hideMenu();
			return TutorialState.DISABLE_OPTIONS;
		}
		if (current == TutorialState.ENABLE_MENU_OPTIONS) {
			if (a.isInMasterView()) return TutorialState.DISABLE_OPTIONS;
			if (performAction) a.getFlyInMenu().disableOptions();
			return TutorialState.SHOW_MENU;
		}
		if (current == TutorialState.OPEN_SETTINGS) return TutorialState.ENABLE_MENU_OPTIONS;
		if (current == TutorialState.CHANGE_THEME) return TutorialState.OPEN_SETTINGS;
		if (current == TutorialState.OUTRO) return TutorialState.CHANGE_THEME;

		return TutorialState.END;
	}
	

	
	public static String getStringFromTutorialState(TutorialState state) {
		if(state == TutorialState.INTRO) return "intro";
		if(state == TutorialState.ADD_TASK) return "addTask";
		if(state == TutorialState.INTRODUCE_TASK) return "introduceTask";
		if(state == TutorialState.ENTER_TASK) return "enterTask";
		if(state == TutorialState.SET_DUE_DATE) return "setDueDate";
		if(state == TutorialState.SET_REMINDER) return "setReminder";
		if(state == TutorialState.CHANGE_NAME) return "changeName";
		if(state == TutorialState.GO_BACK) return "goBack";
		if(state == TutorialState.CHECK_TASK) return "checkTask";
		if(state == TutorialState.PRIORITIZE_TASK) return "prioritizeTask";
		if(state == TutorialState.ADD_FOLDER) return "addFolder";
		if(state == TutorialState.ENTER_FOLDER) return "enterFolder";
		if(state == TutorialState.INTRODUCE_FOLDER) return "introduceFolder";
		if(state == TutorialState.ADD_NOTE) return "addNote";
		if(state == TutorialState.ENTER_NOTE) return "enterNote";
		if(state == TutorialState.INTRODUCE_NOTE) return "introduceNote";
		if(state == TutorialState.CHANGE_DESCRIPTION) return "changeDescription";
		if(state == TutorialState.GO_BACK2) return "goBack2";
		if(state == TutorialState.ENABLE_OPTIONS) return "enableOptions";
		if(state == TutorialState.CHANGE_ORDER) return "changeOrder";
		if(state == TutorialState.REMOVE_ITEMS) return "removeItems";
		if(state == TutorialState.GROUP_ITEMS) return "groupItems";
		if(state == TutorialState.SELECT_ALL) return "selectAll";
		if(state == TutorialState.MOVE_ITEMS) return "moveItems";
		if(state == TutorialState.DISABLE_OPTIONS) return "disableOptions";
		if(state == TutorialState.SHOW_MENU) return "showMenu";
		if(state == TutorialState.ENABLE_MENU_OPTIONS) return "enableMenuOptions";
		if(state == TutorialState.OPEN_SETTINGS) return "openSettings";
		if(state == TutorialState.CHANGE_THEME) return "changeTheme";
		if(state == TutorialState.OUTRO) return "outro";
		
		return "";
	}
	
	public static TutorialState getTutorialStateFromString(String string) {
		if(string.equals("intro")) return TutorialState.INTRO;
		if(string.equals("addTask")) return TutorialState.ADD_TASK;
		if(string.equals("enterTask")) return TutorialState.ENTER_TASK;
		if(string.equals("introduceTask")) return TutorialState.INTRODUCE_TASK;
		if(string.equals("setDueDate")) return TutorialState.SET_DUE_DATE;
		if(string.equals("setReminder")) return TutorialState.SET_REMINDER;
		if(string.equals("changeName")) return TutorialState.CHANGE_NAME;
		if(string.equals("goBack")) return TutorialState.GO_BACK;
		if(string.equals("checkTask")) return TutorialState.CHECK_TASK;
		if(string.equals("prioritizeTask")) return TutorialState.PRIORITIZE_TASK;
		if(string.equals("addFolder")) return TutorialState.ADD_FOLDER;
		if(string.equals("enterFolder")) return TutorialState.ENTER_FOLDER;
		if(string.equals("introduceFolder")) return TutorialState.INTRODUCE_FOLDER;
		if(string.equals("addNote")) return TutorialState.ADD_NOTE;
		if(string.equals("enterNote")) return TutorialState.ENTER_NOTE;
		if(string.equals("introduceNote")) return TutorialState.INTRODUCE_NOTE;
		if(string.equals("changeDescription")) return TutorialState.CHANGE_DESCRIPTION;
		if(string.equals("goBack2")) return TutorialState.GO_BACK2;
		if(string.equals("enableOptions")) return TutorialState.ENABLE_OPTIONS;
		if(string.equals("changeOrder")) return TutorialState.CHANGE_ORDER;
		if(string.equals("removeItems")) return TutorialState.REMOVE_ITEMS;
		if(string.equals("groupItems")) return TutorialState.GROUP_ITEMS;
		if(string.equals("selectAll")) return TutorialState.SELECT_ALL;
		if(string.equals("moveItems")) return TutorialState.MOVE_ITEMS;
		if(string.equals("disableOptions")) return TutorialState.DISABLE_OPTIONS;
		if(string.equals("showMenu")) return TutorialState.SHOW_MENU;
		if(string.equals("enableMenuOptions")) return TutorialState.ENABLE_MENU_OPTIONS;
		if(string.equals("openSettings")) return TutorialState.OPEN_SETTINGS;
		if(string.equals("changeTheme")) return TutorialState.CHANGE_THEME;
		if(string.equals("outro")) return TutorialState.OUTRO;
		
		return TutorialState.END;
	}
}
