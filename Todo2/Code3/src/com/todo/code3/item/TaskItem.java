package com.todo.code3.item;

public class TaskItem extends ContentItem {

	protected boolean isChecklistChild;
	protected boolean isCompleted = false;
	protected long timestampChecked = -1;
	
	public boolean isChecklistChild() {
		return isChecklistChild;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public long getTimestampCompleted() {
		return timestampChecked;
	}
	
	public TaskItem isChecklistChild(boolean b) {
		isChecklistChild = b;
		return this;
	}
	
	public TaskItem completed(boolean b) {
		isCompleted = b;
		return this;
	}
	
	public TaskItem setTimestampChecked(int i) {
		timestampChecked = i;
		return this;
	}
}
