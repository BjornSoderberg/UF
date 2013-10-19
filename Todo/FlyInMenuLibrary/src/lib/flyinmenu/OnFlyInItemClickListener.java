package lib.flyinmenu;

public interface OnFlyInItemClickListener {
	/**
	 * Callback for when a {@link FlyInMenuItem} is clicked.
	 *
	 * @param flyInMenuItem the item which was clicked
	 * @param position      the position of the clicked item
	 * @return true to hide the FlyIn menu, false otherwise
	 */
	boolean onFlyInItemClick(FlyInMenuItem flyInMenuItem, int position);
}
