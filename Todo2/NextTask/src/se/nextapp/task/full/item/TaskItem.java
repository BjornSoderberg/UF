package se.nextapp.task.full.item;

public class TaskItem extends ContentItem {

	protected boolean isCompleted = false;
	protected long timestampChecked = -1;
	protected long dueDate = -1;
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public long getTimestampCompleted() {
		return timestampChecked;
	}
	
	public long getDueDate() {
		return dueDate;
	}
	
	public TaskItem completed(boolean b) {
		isCompleted = b;
		return this;
	}
	
	public TaskItem setTimestampChecked(int i) {
		timestampChecked = i;
		return this;
	}
	
	public TaskItem setDueDate(long l) {
		dueDate = l;
		return this;
	}
}
