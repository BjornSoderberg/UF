package se.nextapp.task.full.tutorial;

import se.nextapp.task.full.R;

public class TutorialItem {
	private int string, drawable;
	
	public TutorialItem(int drawable, int string) {
		this.string = string;
		this.drawable = drawable;
	}
	
	public int getStringResourceId() {
		return string;
	}
	
	public int getDrawableResourceId() {
		return drawable;
	}
	
	public static TutorialItem getTutorialItem(TutorialState state) {
		if(state == TutorialState.INTRO) return new TutorialItem(R.drawable.gif, R.string.tutorial_intro);
		if(state == TutorialState.ADD_TASK) return new TutorialItem(R.drawable.gif, R.string.tutorial_add_task);
		if(state == TutorialState.INTRODUCE_TASK) return new TutorialItem(R.drawable.gif, R.string.tutorial_introduce_task);
		if(state == TutorialState.ENTER_TASK) return new TutorialItem(R.drawable.gif, R.string.tutorial_enter_task);
		if(state == TutorialState.SET_DUE_DATE) return new TutorialItem(R.drawable.gif, R.string.tutorial_set_due_date);
		if(state == TutorialState.SET_REMINDER) return new TutorialItem(R.drawable.gif, R.string.tutorial_set_reminder);
		if(state == TutorialState.CHANGE_NAME) return new TutorialItem(R.drawable.gif, R.string.tutorial_change_name);
		if(state == TutorialState.GO_BACK) return new TutorialItem(R.drawable.gif, R.string.tutorial_go_back);
		if(state == TutorialState.CHECK_TASK) return new TutorialItem(R.drawable.gif, R.string.tutorial_check_task);
		if(state == TutorialState.PRIORITIZE_TASK) return new TutorialItem(R.drawable.gif, R.string.tutorial_prioritize_task);
		if(state == TutorialState.ADD_FOLDER) return new TutorialItem(R.drawable.gif, R.string.tutorial_add_folder);
		if(state == TutorialState.ENTER_FOLDER) return new TutorialItem(R.drawable.gif, R.string.tutorial_enter_folder);
		if(state == TutorialState.INTRODUCE_FOLDER) return new TutorialItem(R.drawable.gif, R.string.tutorial_introduce_folder);
		if(state == TutorialState.ADD_NOTE) return new TutorialItem(R.drawable.gif, R.string.tutorial_add_note);
		if(state == TutorialState.ENTER_NOTE) return new TutorialItem(R.drawable.gif, R.string.tutorial_enter_note);
		if(state == TutorialState.INTRODUCE_NOTE) return new TutorialItem(R.drawable.gif, R.string.tutorial_introduce_note);
		if(state == TutorialState.CHANGE_DESCRIPTION) return new TutorialItem(R.drawable.gif, R.string.tutorial_change_description);
		if(state == TutorialState.GO_BACK2) return new TutorialItem(R.drawable.gif, R.string.tutorial_go_back2);
		if(state == TutorialState.ENABLE_OPTIONS) return new TutorialItem(R.drawable.gif, R.string.tutorial_enable_options);
		if(state == TutorialState.CHANGE_ORDER) return new TutorialItem(R.drawable.gif, R.string.tutorial_change_order);
		if(state == TutorialState.REMOVE_ITEMS) return new TutorialItem(R.drawable.gif, R.string.tutorial_remove_items);
		if(state == TutorialState.GROUP_ITEMS) return new TutorialItem(R.drawable.gif, R.string.tutorial_group_items);
		if(state == TutorialState.SELECT_ALL) return new TutorialItem(R.drawable.gif, R.string.tutorial_select_all);
		if(state == TutorialState.MOVE_ITEMS) return new TutorialItem(R.drawable.gif, R.string.tutorial_move_items);
		if(state == TutorialState.DISABLE_OPTIONS) return new TutorialItem(R.drawable.gif, R.string.tutorial_disable_options);
		if(state == TutorialState.SHOW_MENU) return new TutorialItem(R.drawable.gif, R.string.tutorial_show_menu);
		if(state == TutorialState.ENABLE_MENU_OPTIONS) return new TutorialItem(R.drawable.gif, R.string.tutorial_enable_menu_options);
		if(state == TutorialState.OPEN_SETTINGS) return new TutorialItem(R.drawable.gif, R.string.tutorial_open_settings);
		if(state == TutorialState.CHANGE_THEME) return new TutorialItem(R.drawable.gif, R.string.tutorial_change_theme);
		if(state == TutorialState.OUTRO) return new TutorialItem(R.drawable.gif, R.string.tutorial_outro);
		if(state == TutorialState.END) return new TutorialItem(R.drawable.gif, R.string.oops_something_went_wrong);
		
		return new TutorialItem(R.drawable.gif,R.string.oops_something_went_wrong);
	}
}
