package com.espian.flyin.library;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.espian.flyin.library.SimpleGestureFilter.SimpleGestureListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlyInMenu extends LinearLayout implements SimpleGestureListener {

	public static final int FLY_IN_WITH_ACTIVITY = 0;
	public static final int FLY_IN_OVER_ACTIVITY = 1;

	private DynamicListView listView;
	private LinearLayout mMenuHolder;
	private ViewStub mCustomStub;
	private View mCustomView;
	// private View mWrappedSearchView;
	// private boolean hasSearchView = false;
	private FlyInFragmentActivity activity;
	private Adapter adapter;
	private SimpleGestureFilter detector;

	private int flyInMenuItemHeight;
	private int expandingItemId = -1;
	private int contentOffset = 0;
	private int width;
	private int animationDuration = 300;
	private int movingItemId = -1;

	private boolean isDragging = false;
	private boolean isInOptionsMode = false;

	private OnFlyInItemClickListener callback;

	public ArrayList<FlyInMenuItem> menuItems;

	public void clearMenuItems() {
		if(menuItems != null) menuItems.clear();
	}

	public void addMenuItem(FlyInMenuItem i) {
		menuItems.add(i);
	}

	public FlyInMenu(Context context) {
		super(context);
		activity = (FlyInFragmentActivity) context;
		load();
	}

	public FlyInMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (FlyInFragmentActivity) context;
		load();
	}

	private void load() {

		if (isInEditMode()) return;

		inflateLayout();

		initUi();

		detector = new SimpleGestureFilter(getContext(), this);

	}

	private void inflateLayout() {
		try {
			LayoutInflater.from(getContext()).inflate(R.layout.fly_menu, this, true);
		} catch (Exception e) {

		}
	}

	private void initUi() {

		listView = (DynamicListView) findViewById(R.id.fly_listview);
		mCustomStub = (ViewStub) findViewById(R.id.fly_custom);
		mMenuHolder = (LinearLayout) findViewById(R.id.fly_menu_holder);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				boolean hide = true;
				if (callback != null) {
					hide = callback.onFlyInItemClick(menuItems.get(position), position);
				}

				if (hide) hideMenu();
			}

		});

		flyInMenuItemHeight = (int) activity.getResources().getDimension(R.dimen.item_height);
		listView.setFlyInFragmentActivity(activity);
	}

	public void setOnFlyInItemClickListener(OnFlyInItemClickListener callback) {
		this.callback = callback;
	}

	public void setMenuSize(int w) {
		width = w;
		LayoutParams p = (LayoutParams) mMenuHolder.getLayoutParams();
		p.width = w;
		mMenuHolder.setLayoutParams(p);
	}

	/**
	 * Enabled the SearchView at the top of the Fly-in menu, providing the SDK
	 * is API level 11 (Honeycomb) or above. The SearchView can be accessed with
	 * {@link #getSearchView()}.
	 */
	/*
	 * @SuppressLint("NewApi") public void enableSearchView() { if
	 * (Build.VERSION.SDK_INT >= 11) { SearchView s = new
	 * SearchView(getContext()); s.setIconifiedByDefault(false);
	 * s.setSubmitButtonEnabled(true); (mWrappedSearchView =
	 * s).setId(R.id.fly_searchview);
	 * mMenuHolder.removeView(findViewById(R.id.fly_searchstub));
	 * mMenuHolder.addView(mWrappedSearchView, 0); hasSearchView = true; } }
	 */

	/**
	 * Fetches the SearchView associated with the Fly-in menu. Note that calling
	 * this on an API level pre-11 (Honeycomb) will crash, so your logic must
	 * include a check on the API level if you target such platforms. The
	 * SearchView must first be initialised using {@link #enableSearchView}.
	 * 
	 * @return the SearchView instance, or null if it hasn't been initialised
	 */
	/*
	 * public SearchView getSearchView() { if (!hasSearchView) return null;
	 * return (SearchView) mWrappedSearchView; }
	 */

	/**
	 * Fetch the custom view at the bottom of the fly-in menu.
	 * 
	 * @return the custom view, or null
	 */
	public View getCustomView() {
		return mCustomView;
	}

	/**
	 * Set the custom view at the bottom of the fly-in menu, from a resource
	 * 
	 * @param rid
	 *            layout resource id of the view to inflate
	 */
	public void setCustomView(int rid) {
		mCustomStub.setLayoutResource(rid);
		mCustomView = mCustomStub.inflate();
		mCustomView.setVisibility(isVisible() ? View.VISIBLE : View.GONE);
		requestLayout();
	}

	/**
	 * Set the custom view at the bottom of the fly-in menu
	 * 
	 * @param view
	 *            the view to set
	 */
	public void setCustomView(View view) {
		mMenuHolder.removeView(mCustomStub);
		mMenuHolder.addView(mCustomView = view);
		mCustomView.setVisibility(isVisible() ? View.VISIBLE : View.GONE);
		requestLayout();
	}

	/**
	 * Set the menu items associated with the fly-in menu
	 * 
	 * @param menu
	 *            resource id of the menu to be inflated
	 */
	public void setMenuItems() {
		if (menuItems == null || menuItems.size() <= 0) menuItems = new ArrayList<FlyInMenuItem>();

		if (menuItems != null && menuItems.size() > 0) {
			if (adapter == null) {
				adapter = new Adapter();
				listView.setAdapter(adapter);
			} else adapter.notifyDataSetChanged();

			listView.setMenuItems(menuItems);
		}
		
		setColors();
	}
	
	private void setColors() {
		mMenuHolder.setBackgroundColor(getResources().getColor(activity.isDarkTheme() ? R.color.menu_background_dark : R.color.menu_background_light));
	}

	/**
	 * Sets the background of the fly-in menu
	 */
	public void setBackgroundResource(int resource) {
		mMenuHolder.setBackgroundResource(resource);
	}

	public void showMenu() {
		if (getContext().getResources().getString(R.string.is_in_master_view).equals("true")) return;
		disableOptions();
		
		// mOutsideView.setVisibility(View.VISIBLE);
		mMenuHolder.setVisibility(View.VISIBLE);
		if (mCustomView != null) {
			mCustomView.setVisibility(View.VISIBLE);
		}

		ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
		View v, x;
		v = decorView.getChildAt(0);
		x = decorView.getChildAt(1);

		Interpolator decel = AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator);

		ObjectAnimator flyIn = ObjectAnimator.ofFloat(x, "translationX", contentOffset - width, 0);
		ObjectAnimator activity = ObjectAnimator.ofFloat(v, "translationX", contentOffset, width);

		flyIn.setInterpolator(decel);
		activity.setInterpolator(decel);

		AnimatorSet showFlyIn = new AnimatorSet();
		showFlyIn.playTogether(flyIn, activity);
		showFlyIn.setDuration(animationDuration).start();

		contentOffset = width;

		isDragging = false;
	}

	public void hideMenu() {
		if (getContext().getResources().getString(R.string.is_in_master_view).equals("true")) return;
		disableOptions();
		
		mMenuHolder.setVisibility(View.VISIBLE);
		if (mCustomView != null) {
			mCustomView.setVisibility(View.VISIBLE);
		}

		ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
		View v, x;
		v = decorView.getChildAt(0);
		x = decorView.getChildAt(1);

		Interpolator decel = AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator);

		ObjectAnimator flyIn = ObjectAnimator.ofFloat(x, "translationX", contentOffset - width, -width);
		ObjectAnimator activity = ObjectAnimator.ofFloat(v, "translationX", contentOffset, 0);

		flyIn.setInterpolator(decel);
		activity.setInterpolator(decel);

		AnimatorSet showFlyIn = new AnimatorSet();
		showFlyIn.playTogether(flyIn, activity);
		showFlyIn.setDuration(animationDuration).start();

		// Hides the menu views when the animation has ended
		new Handler().postDelayed(new Runnable() {
			public void run() {
				mMenuHolder.setVisibility(View.GONE);
				if (mCustomView != null) {
					mCustomView.setVisibility(View.GONE);
				}

				isDragging = false;
			}
		}, animationDuration);

		contentOffset = 0;
	}

	public void moveMenu(int dx) {
		if (getContext().getResources().getString(R.string.is_in_master_view).equals("true")) return;
		disableOptions();
		
		if (contentOffset + dx < 0) dx = 0 - contentOffset;
		if (contentOffset + dx > width) dx = width - contentOffset;
		if (dx == 0) return;

		isDragging = true;

		// mOutsideView.setVisibility(View.VISIBLE);
		mMenuHolder.setVisibility(View.VISIBLE);
		if (mCustomView != null) {
			mCustomView.setVisibility(View.VISIBLE);
		}

		ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
		View v, x;
		v = decorView.getChildAt(0);
		x = decorView.getChildAt(1);

		ObjectAnimator flyIn = ObjectAnimator.ofFloat(x, "translationX", contentOffset - width, contentOffset - width + dx);
		ObjectAnimator activity = ObjectAnimator.ofFloat(v, "translationX", contentOffset, contentOffset + dx);
		flyIn.setInterpolator(new LinearInterpolator());
		activity.setInterpolator(new LinearInterpolator());

		AnimatorSet showFlyIn = new AnimatorSet();
		showFlyIn.playTogether(flyIn, activity);
		showFlyIn.setDuration(0).start();

		contentOffset += dx;
	}

	private void expandView(final View view) {
		if (view.getLayoutParams() != null) view.getLayoutParams().height = 1;

		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (view.getLayoutParams() != null) view.getLayoutParams().height = 1;

				Animation animation = new Animation() {
					protected void applyTransformation(float time, Transformation t) {
						if ((int) (flyInMenuItemHeight * time) != 0) {
							view.getLayoutParams().height = (int) (flyInMenuItemHeight * time);
						} else {
							view.getLayoutParams().height = 1;
						}

						view.requestLayout();
					}
				};

				animation.setDuration(animationDuration);
				view.startAnimation(animation);
			}
		}, 0);
	}

	/**
	 * Toggle the menu visibility: show it if it is hidden, and hide if it is
	 * shown.
	 */
	public void toggleMenu() {
		if (contentOffset == 0) {
			showMenu();
		} else {
			hideMenu();
		}
	}

	public void setExpandingItemId(int id) {
		expandingItemId = id;
	}

	public int getExpandingItemId() {
		return expandingItemId;
	}

	public void invalidateExpandingItemId() {
		expandingItemId = -1;
	}

	public boolean isVisible() {
		return contentOffset != 0;
	}

	public int test() {
		return contentOffset;
	}

	public int getContentOffset() {
		return contentOffset;
	}

	public ArrayList<FlyInMenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMovingItemId(int id) {
		movingItemId = id;
	}

	public DynamicListView getListView() {
		return listView;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.bShowMenu) showMenu();
		else hideMenu();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);

		ss.bShowMenu = isVisible();

		return ss;
	}

	static class SavedState extends BaseSavedState {
		boolean bShowMenu;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			bShowMenu = (in.readInt() == 1);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(bShowMenu ? 1 : 0);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private class Adapter extends BaseAdapter {

		private LayoutInflater inflater;

		public Adapter() {
			inflater = LayoutInflater.from(getContext());
		}

		public int getCount() {
			return menuItems.size();
		}

		public FlyInMenuItem getItem(int position) {
			return menuItems.get(position);
		}

		public long getItemId(int position) {
			if (position < 0 || position >= menuItems.size()) return -1;

			FlyInMenuItem item = getItem(position);
			return item.getId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// if (convertView == null || convertView instanceof TextView)
			View view = null;
			FlyInMenuItem item = menuItems.get(position);

			if(!isInOptionsMode) view = getFolderView(item);
			else view = getOptionsView(item);

			view.setId(item.getId());

			return view;
		}

		private View getFolderView(FlyInMenuItem item) {
			View view = inflater.inflate(R.layout.fly_item, null);

			TextView text = (TextView) view.findViewById(R.id.item_text);
			text.setText(item.getTitle());
			if (item.getId() == getExpandingItemId()) {
				invalidateExpandingItemId();
				expandView(view);

				if (view.getLayoutParams() != null) view.getLayoutParams().height = 1;
				else view.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1));
			}

//			if (item.getId() == movingItemId) view.setVisibility(View.INVISIBLE);

			int drawableId = R.drawable.ic_folder;
			if (App.getNumberOfTasksOverDue(item.getId(), activity.getData()) > 0) drawableId = R.drawable.ic_folder_due;
			else if (App.getNumberOfTasksCompleted(item.getId(), false, activity.getData()) == 0 && App.getNumberOfTasksCompleted(item.getId(), true, activity.getData()) > 0) drawableId = R.drawable.ic_folder_check;

			Drawable d = getContext().getResources().getDrawable(drawableId).mutate();
			d.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.item_text_color), PorterDuff.Mode.MULTIPLY));
			((ImageView) view.findViewById(R.id.icon)).setBackgroundDrawable(d);

			if (item.isOpen()) view.setBackgroundDrawable(getContext().getResources().getDrawable(activity.isDarkTheme() ? R.drawable.fly_item_background_dark_selected : R.drawable.fly_item_background_light_selected));
			else view.setBackgroundDrawable(getContext().getResources().getDrawable(activity.isDarkTheme() ? R.drawable.fly_item_background_dark : R.drawable.fly_item_background_light));

			return view;
		}
		
		private View getOptionsView(final FlyInMenuItem item) {
			final View view = inflater.inflate(R.layout.fly_options_item, null);
			
			TextView text = (TextView) view.findViewById(R.id.item_text);
			text.setText(item.getTitle());
			text.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View arg0) {
					FlyInMenu.this.toggleOptions();
					return true;
				}
			});
			
			if(item.getId() == movingItemId) view.setVisibility(View.INVISIBLE);
			
			
			Drawable d = ((ImageView) view.findViewById(R.id.remove_icon)).getBackground().mutate();
			d.setColorFilter(new PorterDuffColorFilter(getContext().getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));
			((ImageView) view.findViewById(R.id.remove_icon)).setBackgroundDrawable(d);
			((FrameLayout) view.findViewById(R.id.item_remove)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					new CollapseAnimation(view, animationDuration, (int)getResources().getDimension(R.dimen.item_height)).animate();
					
					new Handler().postDelayed(new Runnable() {
						public void run() {
							view.setVisibility(View.GONE);
							
							activity.remove(item.getId());
						}
					}, animationDuration);
				}
			});
			
			Drawable dd = ((ImageView) view.findViewById(R.id.move_icon)).getBackground().mutate();
			dd.setColorFilter(new PorterDuffColorFilter(getContext().getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));
			((ImageView) view.findViewById(R.id.move_icon)).setBackgroundDrawable(dd);
			
			view.setBackgroundDrawable(getContext().getResources().getDrawable(activity.isDarkTheme() ? R.drawable.fly_item_background_dark : R.drawable.fly_item_background_light));
			
			return view;
		}
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		detector.onTouchEvent(e);
		return super.dispatchTouchEvent(e);
	}

	public void onSwipe(int direction) {
		if (!isDragging) if (direction == SimpleGestureFilter.SWIPE_LEFT) hideMenu();
	}

	public void toggleOptions() {
		if (isInOptionsMode) disableOptions();
		else enableOptions();
	}

	public void enableOptions() {
		isInOptionsMode = true;
		setMenuItems();
	}

	public void disableOptions() {
		isInOptionsMode = false;
		setMenuItems();
	}
	
	public boolean isInOptionsMode() {
		return isInOptionsMode;
	}
}
