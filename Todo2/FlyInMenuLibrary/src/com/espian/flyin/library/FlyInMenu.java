package com.espian.flyin.library;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlyInMenu extends LinearLayout {

	public static final int FLY_IN_WITH_ACTIVITY = 0;
	public static final int FLY_IN_OVER_ACTIVITY = 1;

	private ListView listView;
	private LinearLayout mMenuHolder;
	private ViewStub mCustomStub;
	private View mCustomView;
	// private View mWrappedSearchView;
	// private boolean hasSearchView = false;
	private FlyInFragmentActivity activity;

	private Adapter adapter;

	private final int EXPAND_ANIMATION_DURATION = 300;
	private int flyInMenuItemHeight;
	private int expandingItemId = -1;

	private boolean hasDynamicListView = false;

	private int contentOffset = 0;
	private int width;
	private int animationDuration = 300;

	private OnFlyInItemClickListener callback;

	public ArrayList<FlyInMenuItem> menuItems;

	public void clearMenuItems() {
		menuItems.clear();
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

	}

	private void inflateLayout() {
		hasDynamicListView = activity.getSDKVersion() >= 11;

		Log.i("asassad", "a" + activity.getSDKVersion());

		try {
			if (hasDynamicListView) LayoutInflater.from(getContext()).inflate(R.layout.fly_menu_dynamic, this, true);
			else LayoutInflater.from(getContext()).inflate(R.layout.fly_menu, this, true);
		} catch (Exception e) {

		}

	}

	private void initUi() {

		listView = (ListView) findViewById(R.id.fly_listview);
		mCustomStub = (ViewStub) findViewById(R.id.fly_custom);
		mMenuHolder = (LinearLayout) findViewById(R.id.fly_menu_holder);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				boolean hide = true;
				if (callback != null) {
					hide = callback.onFlyInItemClick(menuItems.get(position), position);
				}

				if (hide) hideMenu();
			}

		});

		flyInMenuItemHeight = (int) activity.getResources().getDimension(R.dimen.item_height);
		if (hasDynamicListView) ((DynamicListView) listView).setFlyInFragmentActivity(activity);
	}

	public void setOnFlyInItemClickListener(OnFlyInItemClickListener callback) {
		this.callback = callback;
	}

	public void setMenuWidth(int w) {
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
		mCustomView.setVisibility(isMenuVisible() ? View.VISIBLE : View.GONE);
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
		mCustomView.setVisibility(isMenuVisible() ? View.VISIBLE : View.GONE);
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

			if (hasDynamicListView) ((DynamicListView) listView).setMenuItems(menuItems);
		}
	}

	/**
	 * Sets the background of the fly-in menu
	 */
	public void setBackgroundResource(int resource) {
		mMenuHolder.setBackgroundResource(resource);
	}

	public void showMenu() {
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
	}

	public void hideMenu() {
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
		showFlyIn.addListener(new VisibilityHelper());
		showFlyIn.setDuration(animationDuration).start();

		// Hides the menu views when the animation has ended
		new Handler().postDelayed(new Runnable() {
			public void run() {
				mMenuHolder.setVisibility(View.GONE);
				if (mCustomView != null) {
					mCustomView.setVisibility(View.GONE);
				}
			}
		}, animationDuration);

		contentOffset = 0;
	}

	public void moveMenu(int dx) {
		if (contentOffset + dx < 0) dx = 0 - contentOffset;
		if (contentOffset + dx > width) dx = width - contentOffset;
		if (dx == 0) return;

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
		showFlyIn.addListener(new VisibilityHelper());
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

				animation.setDuration(EXPAND_ANIMATION_DURATION);
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

	public boolean isMenuVisible() {
		return contentOffset != 0;
	}

	public int getContentOffset() {
		return contentOffset;
	}

	public ArrayList<FlyInMenuItem> getMenuItems() {
		return menuItems;
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

		ss.bShowMenu = isMenuVisible();

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

		@Override
		public int getCount() {
			return menuItems.size();
		}

		@Override
		public FlyInMenuItem getItem(int position) {
			return menuItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			if (position < 0 || position >= menuItems.size()) return -1;

			FlyInMenuItem item = getItem(position);
			return item.getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView instanceof TextView) convertView = inflater.inflate(R.layout.fly_item, null);

			ImageView icon = (ImageView) convertView.findViewById(R.id.rbm_item_icon);
			TextView text = (TextView) convertView.findViewById(R.id.rbm_item_text);
			FlyInMenuItem item = menuItems.get(position);

			text.setText(item.getTitle());
			icon.setImageResource(item.getIconId());

			if (item.getId() == getExpandingItemId()) {
				invalidateExpandingItemId();
				expandView(convertView);

				if (convertView.getLayoutParams() != null) convertView.getLayoutParams().height = 1;
				else convertView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1));
			}

			convertView.setId(item.getId());

			return convertView;

		}

	}

	public class VisibilityHelper implements AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// mOutsideView.setVisibility(View.GONE);
			// mMenuHolder.setVisibility(View.GONE);
			// if (mCustomView != null) {
			// mCustomView.setVisibility(View.GONE);
			// }
		}

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

	}
}
