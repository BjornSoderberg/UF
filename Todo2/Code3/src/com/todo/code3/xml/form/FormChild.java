package com.todo.code3.xml.form;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.todo.code3.R;

public class FormChild extends LinearLayout {

	public static final int ANIMATION_DURATION = 500;
	public static final int TEXT_LINE = 0;

	private RelativeLayout title, content;
	private FormParent parent;

	private int height = 100;
	private int formType;

	private String name;
	private String hint;

	private boolean isMoving = false;

	public FormChild(Context context, String title, String hint, int type, FormParent parent) {
		super(context);
		name = title;
		formType = type;
		this.parent = parent;
		this.hint = hint;

		init();
	}

	public FormChild(Context context, AttributeSet attr, String title, String hint, int type, FormParent parent) {
		super(context, attr);
		name = title;
		formType = type;
		this.parent = parent;
		this.hint = hint;

		init();
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(0xff585858);
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		initTitle();

		if (formType == TEXT_LINE) initTextLine();
		content.setVisibility(View.GONE);

		addView(title);
		addView(content);
	}

	private void initTitle() {
		View v = View.inflate(getContext(), R.layout.form_title, null);
		((TextView) v.findViewById(R.id.title)).setText(name);

		title = (RelativeLayout) v;
		title.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleContent();
			}
		});
	}

	private void initTextLine() {
		View v = View.inflate(getContext(), R.layout.form_text_line, null);
		((Button) v.findViewById(R.id.button)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				next();
			}
		});
		content = (RelativeLayout) v;
	}

	public void next() {
		setAnswerInTitle();
		parent.next();
	}

	private void setAnswerInTitle() {
		String answer = "";
		
		if(formType == TEXT_LINE) {
			answer = ((EditText)content.findViewById(R.id.editText)).getText().toString();
		}
		
		((TextView) title.findViewById(R.id.answer)).setText(answer);
		((TextView) title.findViewById(R.id.answer)).setVisibility(View.VISIBLE);
	}

	public boolean isContentVisible() {
		return content.getVisibility() == View.VISIBLE;
	}

	public void toggleContent() {
		if (isMoving) return;

		if (isContentVisible()) hideContent();
		else {
			showContent();
			parent.hideOther(this);
		}
	}

	public void showContent() {
		if (isMoving) return;

		AnimationListener al = new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				isMoving = false;
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
				isMoving = true;
				content.setVisibility(View.VISIBLE);
			}
		};

		Animation animation = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				if ((int) (height * time) != 0) content.getLayoutParams().height = (int) (height * time);
				else content.getLayoutParams().height = 1;

				content.requestLayout();
			}
		};

		content.setBackgroundColor(0xffff0000);

		animation.setAnimationListener(al);
		animation.setDuration(ANIMATION_DURATION);
		startAnimation(animation);
	}

	public void hideContent() {
		if (isMoving) return;

		AnimationListener al = new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				isMoving = false;
				content.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
				isMoving = true;
			}
		};

		Animation animation = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				content.getLayoutParams().height = (int) (height - height * time);

				requestLayout();
			}
		};

		animation.setAnimationListener(al);
		animation.setDuration(ANIMATION_DURATION);
		startAnimation(animation);
	}
}
