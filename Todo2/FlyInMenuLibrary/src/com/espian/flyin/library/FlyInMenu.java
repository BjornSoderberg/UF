package com.espian.flyin.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

public class FlyInMenu extends LinearLayout {

	public static final int FLY_IN_WITH_ACTIVITY = 0;
	public static final int FLY_IN_OVER_ACTIVITY = 1;

	private ListView mListView;
	private View mOutsideView;
	private LinearLayout mMenuHolder;
	private ViewStub mCustomStub;
	private View mCustomView;
	private View mWrappedSearchView;
	private boolean hasSearchView = false;
	private int flyType;
	private int animStartFlyType;
	private Activity mAct;

	private OnFlyInItemClickListener callback;

	private static ArrayList<FlyInMenuItem> menuItems;

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

	public void setType(int type) {
		flyType = type;
	}

	private void load() {

		if (isInEditMode())
			return;

		inflateLayout();

		initUi();

	}

	private void inflateLayout() {

		try {
			LayoutInflater.from(getContext()).inflate(R.layout.fly_menu, this,
					true);
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

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {

				boolean hide = true;
				if (callback != null) {
					hide = callback.onFlyInItemClick(menuItems.get(position),
							position);
				}

				if (hide)
					hideMenu();
			}

		});

	}

	public void setOnFlyInItemClickListener(OnFlyInItemClickListener callback) {
		this.callback = callback;
	}

	/**
	 * Enabled the SearchView at the top of the Fly-in menu, providing the SDK
	 * is API level 11 (Honeycomb) or above. The SearchView can be accessed with
	 * {@link #getSearchView()}.
	 */
	public void enableSearchView() {
		if (Build.VERSION.SDK_INT >= 11) {
			SearchView s = new SearchView(getContext());
			s.setIconifiedByDefault(false);
			s.setSubmitButtonEnabled(true);
			(mWrappedSearchView = s).setId(R.id.fly_searchview);
			mMenuHolder.removeView(findViewById(R.id.fly_searchstub));
			mMenuHolder.addView(mWrappedSearchView, 0);
			hasSearchView = true;
		}
	}

	/**
	 * Fetches the SearchView associated with the Fly-in menu. Note that calling
	 * this on an API level pre-11 (Honeycomb) will crash, so your logic must
	 * include a check on the API level if you target such platforms. The
	 * SearchView must first be initialised using {@link #enableSearchView}.
	 *
	 * @return the SearchView instance, or null if it hasn't been initialised
	 */
	public SearchView getSearchView() {
		if (!hasSearchView)
			return null;
		return (SearchView) mWrappedSearchView;
	}

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
	 * @param rid layout resource id of the view to inflate
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
	 * @param view the view to set
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
	 * @param menu resource id of the menu to be inflated
	 */
	public void setMenuItems(int menu) {
		setMenuItems(menu, -1);
	}

	public void setMenuItems(int menu, int resourceId) {

		parseXml(menu);
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

	private int getInteger(int id) {
		return getResources().getInteger(id);
	}

	public void showMenu() {

		if (flyType > 1)
			flyType = 0;
		mOutsideView.setVisibility(View.VISIBLE);
		mMenuHolder.setVisibility(View.VISIBLE);
		if (mCustomView != null) {
            mCustomView.setVisibility(View.VISIBLE);
        }

		ViewGroup decorView = (ViewGroup) mAct.getWindow().getDecorView();
		View v, x;
		v = decorView.getChildAt(0);
		x = decorView.getChildAt(1);

		Interpolator decel = AnimationUtils.loadInterpolator(getContext(),
				android.R.anim.decelerate_interpolator);

		ObjectAnimator flyIn = ObjectAnimator.ofFloat(x, "translationX",
				getResources().getDimension(R.dimen.rbm_menu_width) * -1, 0);
		ObjectAnimator activity = ObjectAnimator.ofFloat(v, "translationX", 0,
				getResources().getDimension(R.dimen.rbm_menu_width));
		flyIn.setInterpolator(decel);
		activity.setInterpolator(decel);

		animStartFlyType = flyType;
		if (flyType == FLY_IN_WITH_ACTIVITY) {

			AnimatorSet showFlyIn = new AnimatorSet();
			showFlyIn.playTogether(flyIn, activity);
			showFlyIn.setDuration(300).start();

		} else {

			flyIn.setInterpolator(decel);
			flyIn.setDuration(300).start();

		}

	}

	public void hideMenu() {

		ViewGroup decorView = (ViewGroup) mAct.getWindow().getDecorView();
		View v, x;
		v = decorView.getChildAt(0);
		x = decorView.getChildAt(1);

		Interpolator decel = AnimationUtils.loadInterpolator(getContext(),
				android.R.anim.decelerate_interpolator);

		ObjectAnimator flyIn = ObjectAnimator.ofFloat(x, "translationX", 0,
				getResources().getDimension(R.dimen.rbm_menu_width) * -1);
		ObjectAnimator activity = ObjectAnimator.ofFloat(v, "translationX",
				getResources().getDimension(R.dimen.rbm_menu_width), 0);
		flyIn.setInterpolator(decel);
		activity.setInterpolator(decel);

		if (animStartFlyType == FLY_IN_WITH_ACTIVITY) {

			AnimatorSet showFlyIn = new AnimatorSet();
			showFlyIn.playTogether(flyIn, activity);
			showFlyIn.addListener(new VisibilityHelper());
			showFlyIn.setDuration(300).start();

		} else {

			flyIn.setInterpolator(decel);
			flyIn.addListener(new VisibilityHelper());
			flyIn.setDuration(300).start();

		}

	}

	/**
	 * Toggle the menu visibility: show it if it is hidden, and hide if it is
	 * shown.
	 */
	public void toggleMenu() {

		if (mOutsideView.getVisibility() == View.GONE) {
			showMenu();
		} else {
			hideMenu();
		}
	}

	private void parseXml(int menu) {

		menuItems = new ArrayList<FlyInMenuItem>();

		try {
			XmlResourceParser xpp = getResources().getXml(menu);

			xpp.next();
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_TAG) {

					String elemName = xpp.getName();

					if (elemName.equals("item")) {

						String textId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"title");
						String iconId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"icon");
						String resId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"id");
						String enabled = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"enabled");

						FlyInMenuItem item = new FlyInMenuItem();
						item.setItemId(Integer.valueOf(resId.replace("@", "")));
						item.setTitle(resourceIdToString(textId));
						item.setIcon(Integer.valueOf(iconId.replace("@", "")));
						// item.setEnabled(Boolean.parseBoolean(enabled));

						menuItems.add(item);

					}

				}

				eventType = xpp.next();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String resourceIdToString(String text) {

		if (!text.contains("@")) {
			return text;
		} else {

			String id = text.replace("@", "");
			int rid = Integer.valueOf(id);
			return getResources().getString(rid);

		}

	}

	public boolean isMenuVisible() {
		return mOutsideView.getVisibility() == View.VISIBLE;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.bShowMenu)
			showMenu();
		else
			hideMenu();
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

			if (convertView == null || convertView instanceof TextView)
				convertView = inflater.inflate(R.layout.fly_item, null);

			ImageView icon = (ImageView) convertView
					.findViewById(R.id.rbm_item_icon);
			TextView text = (TextView) convertView
					.findViewById(R.id.rbm_item_text);
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

			mOutsideView.setVisibility(View.GONE);
			mMenuHolder.setVisibility(View.GONE);
			if (mCustomView != null) {
                mCustomView.setVisibility(View.GONE);
            }
		}

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

	}

}
