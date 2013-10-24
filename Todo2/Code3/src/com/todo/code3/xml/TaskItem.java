package com.todo.code3.xml;

public class TaskItem extends ContentItem {

	protected boolean isChecklistChild;
	protected boolean isCompleted;
	protected int checklistId = -1;
	
	public boolean isChecklistChild() {
		return isChecklistChild;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public int getChecklistId() {
		return checklistId;
	}
	
	public TaskItem isChecklistChild(boolean b) {
		isChecklistChild = b;
		return this;
	}
	
	public TaskItem setChecklistId(int i) {
		checklistId = i;
		isChecklistChild(true);
		return this;
	}
	
	public TaskItem completed(boolean b) {
		isCompleted = b;
		return this;
	}
}
