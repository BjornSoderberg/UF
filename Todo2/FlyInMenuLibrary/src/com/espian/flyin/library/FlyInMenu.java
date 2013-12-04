package com.espian.flyin.library;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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

import com.espian.flyin.library.R;

public class FlyInMenu extends LinearLayout {

	public static final int FLY_IN_WITH_ACTIVITY = 0;
	public static final int FLY_IN_OVER_ACTIVITY = 1;

	private ListView mListView;
	private View mOutsideView;
	private LinearLayout mMenuHolder;
	private ViewStub mCustomStub;
	private View mCustomView;
	// private View mWrappedSearchView;
	// private boolean hasSearchView = false;
	private Activity mAct;

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
		mAct = (Activity) context;
		load();
	}

	public FlyInMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAct = (Activity) context;
		load();
	}

	private void load() {

		if (isInEditMode()) return;

		inflateLayout();

		initUi();

	}

	private void inflateLayout() {

		try {
			LayoutInflater.from(getContext()).inflate(R.layout.fly_menu, this, true);
		} catch (Exception e) {

		}

	}

	private void initUi() {

		mListView = (ListView) findViewById(R.id.fly_listview);
		mOutsideView = findViewById(R.id.fly_outside);
		mCustomStub = (ViewStub) findViewById(R.id.fly_custom);
		mMenuHolder = (LinearLayout) findViewById(R.id.fly_menu_holder);

		mOutsideView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideMenu();
			}
		});

		mOutsideView.setBackgroundColor(0x88ffff00);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				boolean hide = true;
				if (callback != null) {
					hide = callback.onFlyInItemClick(menuItems.get(position), position);
				}

				if (hide) hideMenu();
			}

		});
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
			mListView.setAdapter(new Adapter());

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

		ViewGroup decorView = (ViewGroup) mAct.getWindow().getDecorView();
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
		ViewGroup decorView = (ViewGroup) mAct.getWindow().getDecorView();
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

		// Makes a new thread that waits for the animation duration
		// and then hides the menu views. If this is not done,
		// older Android devices will try to render the views
		// that are no longer visible.
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(animationDuration);
					mAct.runOnUiThread(new Runnable() {
						public void run() {
							mOutsideView.setVisibility(View.GONE);
							mMenuHolder.setVisibility(View.GONE);
							if (mCustomView != null) {
								mCustomView.setVisibility(View.GONE);
							}
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();

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

		ViewGroup decorView = (ViewGroup) mAct.getWindow().getDecorView();
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

	public boolean isMenuVisible() {
		return contentOffset != 0;
	}

	public int getContentOffset() {
		return contentOffset;
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
		public Object getItem(int position) {

			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null || convertView instanceof TextView) convertView = inflater.inflate(R.layout.fly_item, null);

			ImageView icon = (ImageView) convertView.findViewById(R.id.rbm_item_icon);
			TextView text = (TextView) convertView.findViewById(R.id.rbm_item_text);
			FlyInMenuItem item = menuItems.get(position);

			text.setText(item.getTitle());
			icon.setImageResource(item.getIconId());
			convertView.setEnabled(item.isEnabled());

			return convertView;

		}

	}

	public class VisibilityHelper implements AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// if (contentOffset == 0) {
			// mOutsideView.setVisibility(View.GONE);
			// mMenuHolder.setVisibility(View.GONE);
			// if (mCustomView != null) {
			// mCustomView.setVisibility(View.GONE);
			// }
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
