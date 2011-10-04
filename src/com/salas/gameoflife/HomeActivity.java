package com.salas.gameoflife;

import java.lang.annotation.Target;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.ITexture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 11:54:51 - 03.04.2010
 */
public class HomeActivity extends CommonActivity implements
		IOnSceneTouchListener, IScrollDetectorListener,
		IPinchZoomDetectorListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final float PULSE_SECOND = 0.25f;
	private static final int CELL_COLUMNS = 40;
	private static final int CELL_ROWS = 40;
	
	private static final float INITIAL_ZOOM = 0.5f;
	private static final int CELL_WID_TARGET = 101 / 4;
	private static final int CELL_HT_TARGET = 171 / 4;
	private static final int HUD_TOP_HEIGHT = 50;
	private static final int HUD_RIGHT_WIDTH = 100;

	// ===========================================================
	// Fields
	// ===========================================================

	private int screenWidth;
	private int screenHeight;
	private ZoomCamera camera;
	private Engine engine;
	private GOLState state;
	private SurfaceScrollDetector scrollDetector;
	private PinchZoomDetector pinchDetector;
	private float startZoomfactor;
	private Font font;
	private ITexture fontTexture;
	private ChangeableText hudText;
	private HUD hud;
	private BitmapTextureAtlas grassTextureAtlas;
	private TextureRegion grassTexture;
	private BitmapTextureAtlas charTextureAtlas;
	private TextureRegion charTexture;
	private BitmapTextureAtlas dirtTextureAtlas;
	private TextureRegion dirtTexture;

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
	public Engine onLoadEngine() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		Log.d("GOL", "screen wid="+screenWidth+", ht="+screenHeight);
		
//		int cellsColumns = screenWidth / CELL_WID_TARGET;
//		int cellsRows = screenHeight / CELL_HT_TARGET;
		
		int cellsColumns = CELL_COLUMNS;
		int cellsRows = CELL_ROWS; 

		camera = new ZoomCamera(0, 0, screenWidth, screenHeight);
		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(
						screenWidth, screenHeight), camera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		engine = new Engine(engineOptions);
		checkEnableMultiTouch(engine);
		state = new GOLState(cellsColumns, cellsRows);
		return (engine);
	}

	@Override
	public void onLoadResources() {
		// Fonts
		fontTexture = new BitmapTextureAtlas(256, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		font = new Font(fontTexture, Typeface.create(Typeface.DEFAULT,
				Typeface.BOLD), 20, true, Color.WHITE);
		engine.getTextureManager().loadTexture(fontTexture);
		getFontManager().loadFont(font);

		// Background tiles
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("tiles/");
		grassTextureAtlas = new BitmapTextureAtlas(128, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		grassTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				grassTextureAtlas, this, "Grass Block.png", 0, 0);
		charTextureAtlas = new BitmapTextureAtlas(128, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		charTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				charTextureAtlas, this, "Tree Short.png", 0, 0);
		dirtTextureAtlas = new BitmapTextureAtlas(128, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		dirtTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				dirtTextureAtlas, this, "Dirt Block.png", 0, 0);
		this.mEngine.getTextureManager().loadTextures(grassTextureAtlas,
				charTextureAtlas, dirtTextureAtlas);

		Log.v("GOL", "Load Resources");
	}

	@SuppressWarnings("unused")
	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setOnAreaTouchTraversalFrontToBack();
		scrollDetector = new SurfaceScrollDetector(this);
//		if (MultiTouch.isSupportedByAndroidVersion()) {
		if (false) {
			try {
				pinchDetector = new PinchZoomDetector(this);
			} catch (final MultiTouchException e) {
				pinchDetector = null;
			}
		} else {
			pinchDetector = null;
		}

		scene.setOnSceneTouchListener(this);
		scene.setTouchAreaBindingEnabled(true);

		state.initScene(scene, charTexture);
		state.initBackground(scene, grassTexture, dirtTexture);
		scene.sortChildren();
		camera.setBounds(state.getActualMinX(), state.getActualMaxX() + HUD_RIGHT_WIDTH + 101, state.getActualMinY()-HUD_TOP_HEIGHT, state.getActualMaxY());
		camera.setZoomFactor(INITIAL_ZOOM);
//		camera.setCenter(camera.getCenterX()/camera.getZoomFactor(),camera.getCenterY()/camera.getZoomFactor());
		camera.setCenter(state.getCenterX() + HUD_RIGHT_WIDTH, state.getCenterY() - HUD_TOP_HEIGHT);
		Log.d("GOL", "Camera bounds: "+state.getActualMinX()+", "+state.getActualMaxX()+", "+state.getActualMinY()+", "+state.getActualMaxY());
		camera.setBoundsEnabled(true);
		hudCreate();
		scene.registerUpdateHandler(new TimerHandler(PULSE_SECOND, true,
				new ITimerCallback() {
					@Override
					public void onTimePassed(final TimerHandler timerHandler) {
						state.pulse();
						state.render(scene);
						hudText.setText("gen: "
								+ state.getCurrGeneration()
								+ "   cam: ("
								+ String.format("%3.0f", camera.getCenterX())
								+ ","
								+ String.format("%3.0f", camera.getCenterY())
										+ ")  zoom: " + camera.getZoomFactor());

					}
				}));

		return scene;
	}

	/**
	 * 
	 */
	private void hudCreate() {
		hud = new HUD();
		Entity hudTopBar = new Rectangle(0, 0, screenWidth, HUD_TOP_HEIGHT);
		hudTopBar.setColor(0.2f, 0.0f, 0.0f);
		Entity hudRightBar = new Rectangle(screenWidth - HUD_RIGHT_WIDTH, HUD_TOP_HEIGHT, HUD_RIGHT_WIDTH, screenHeight - HUD_TOP_HEIGHT); 
		hudRightBar.setColor(0.0f, 0.2f, 0.0f);
		hudText = new ChangeableText(0, 10, font, "", 60);
		hudTopBar.attachChild(hudText);
		hud.attachChild(hudTopBar);
		hud.attachChild(hudRightBar);
		camera.setHUD(hud);
	}

	@Override
	public void onLoadComplete() {
		Log.v("GOL", "Load Complete");
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector,
			final TouchEvent pTouchEvent, final float pDistanceX,
			final float pDistanceY) {
		final float zoomFactor = camera.getZoomFactor();
		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent) {
		startZoomfactor = camera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		camera.setZoomFactor(startZoomfactor * pZoomFactor);
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		camera.setZoomFactor(startZoomfactor * pZoomFactor);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent) {
		if (pinchDetector != null) {
			pinchDetector.onTouchEvent(pSceneTouchEvent);

			if (pinchDetector.isZooming()) {
				scrollDetector.setEnabled(false);
			} else {
				if (pSceneTouchEvent.isActionDown()) {
					scrollDetector.setEnabled(true);
				}
				scrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.scrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
