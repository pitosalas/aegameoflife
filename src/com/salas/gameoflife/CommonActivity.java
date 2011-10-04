package com.salas.gameoflife;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * (c) 2010 Nicolas Gramlich 
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 22:10:28 - 11.04.2010
 */
public abstract class CommonActivity extends BaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_TRACE = Menu.FIRST;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(Menu.NONE, MENU_TRACE, Menu.NONE, "Start Method Tracing");
		return super.onCreateOptionsMenu(pMenu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		pMenu.findItem(MENU_TRACE).setTitle(this.mEngine.isMethodTracing() ? "Stop Method Tracing" : "Start Method Tracing");
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onMenuItemSelected(final int pFeatureId, final MenuItem pItem) {
		switch(pItem.getItemId()) {
			case MENU_TRACE:
				if(this.mEngine.isMethodTracing()) {
					this.mEngine.stopMethodTracing();
				} else {
					this.mEngine.startMethodTracing("AndEngine_" + System.currentTimeMillis() + ".trace");
				}
				return true;
			default:
				return super.onMenuItemSelected(pFeatureId, pItem);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	public boolean checkEnableMultiTouch(Engine engine) {
		try {
			if(MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
				return true;
			} else {
				Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
				return false;
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
			return false;
		}

	}
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
