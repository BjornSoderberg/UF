package com.example.code3;

import lib.flyinmenu.FlyInFragmentActivity;
import lib.flyinmenu.FlyInMenu;
import lib.flyinmenu.FlyInMenuItem;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FlyInFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		loadFlyInMenu(0);
		Button b = new Button(this);
		b.setText("Click me!");
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Button b2 = new Button(MainActivity.this);
				b2.setText("Click me!");
				
				FlyInMenuItem item = new FlyInMenuItem();
				item.setTitle("hello");
				
				getFlyInMenu().addMenuItem(item);
				
				getFlyInMenu().test(b2);
				
				loadFlyInMenu(1);
			}

		});
		getFlyInMenu().setCustomView(b);
		
		
		getFlyInMenu().enableSearchView();
	}

	@Override
	public boolean onFlyInItemClick(FlyInMenuItem menuItem, int position) {
		Toast.makeText(this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
		if (position == 2)
			setFlyInType(FlyInMenu.FLY_IN_OVER_ACTIVITY);
		else if (position == 0)
			setFlyInType(FlyInMenu.FLY_IN_WITH_ACTIVITY);
		return position != 1;
	}

	public void showBackup(View v) {
		getFlyInMenu().toggleMenu();
	}

}