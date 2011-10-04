package com.salas.gameoflife;

import java.util.ArrayList;
import java.util.Enumeration;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.EntityBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.bitstorm.gameoflife.GameOfLifeGrid;
import org.bitstorm.gameoflife.LifeShape;
import org.bitstorm.gameoflife.ShapeCollection;
import org.bitstorm.gameoflife.ShapeException;

import android.util.Log;

public class GOLState {

	public static int GRIDHSPACE = 101;
	public static int GRIDVSPACE = 85;

	public static int BACKTILEW = 99 ;//101;// 101;
	public static int BACKTILEH = 83;//85;// 101;

	private float actualMinX, actualMaxX, actualMinY, actualMaxY;
	private int cellColumns, cellRows;

	private GameOfLifeGrid grid;
	int camWidth, camHeight, rowsWidth, colsHeight;
	private int generation = 0;
	ArrayList<Entity> entityList;

	// Background
	ArrayList<Entity> bckrdEntityList;

	public GOLState(int cellColumns, int cellRows) {
		this.cellColumns = cellColumns;
		this.cellRows = cellRows;
		grid = new GameOfLifeGrid(cellColumns, cellRows);
		try {
			setShape(ShapeCollection.getShapeByName("Exploder"));
		} catch (Exception e) {
			Log.e("GOL", "Shape Error in GOLState Constructor");
		}
		entityList = new ArrayList<Entity>();
		bckrdEntityList = new ArrayList<Entity>();
		
// We want to learn the real-world max and mins of all the sprites we put there. Initialize the variables.
		actualMinX = 9999999; actualMinY = actualMinX;
		actualMaxX = -actualMinX; actualMaxY = actualMaxX;
	}

	public void initScene(Scene s, TextureRegion charTexture) {
		s.detachChildren();
		entityList.clear();
		final Entity lifeforms = new Entity(0, 0);
		for (int row = 0; row < cellRows; row++) {
			for (int col = 0; col < cellColumns; col++) {
				float yPos = row * GRIDVSPACE;
				float xPos = col * GRIDHSPACE;
//				Entity r = makeLifeform(xPos, yPos, (row + 1) * 0.2f + col
//						* 0.05f, 0, 0);
				Entity r = makeLifeform(xPos, yPos, charTexture);
				lifeforms.attachChild(r);
				entityList.add(row * cellColumns + col, r);
			}
		}
		lifeforms.setZIndex(1);
		s.attachChild(lifeforms);
	}

	public void initBackground(Scene scene, TextureRegion backTexture, TextureRegion dirtTexture) {
		bckrdEntityList.clear();
		final Entity backTilesEntity = new Entity(0, 0);
		for (int row = 0; row < cellRows; row++) {
			for (int col = 0; col < cellColumns; col++) {
				float yPos = row * GRIDVSPACE;
				float xPos = col * GRIDHSPACE;
				Entity b;
				if (isCorner(row, col, cellRows-1, cellColumns-1)) {
					b = makeBackCell(xPos, yPos, dirtTexture);
				} else {
					b = makeBackCell(xPos, yPos, backTexture);
				}
				backTilesEntity.attachChild(b);
				bckrdEntityList.add(row * cellColumns + col, b);
			}
		}
		backTilesEntity.setZIndex(0);
		scene.attachChild(backTilesEntity);
	}
	
	private boolean isCorner(int x, int y, int maxX, int maxY) {
		if (x == 0 && y == 0) return true;
		if (x == 0 && y == maxY) return true;
		if (x == maxX && y == 0) return true;
		if (x == maxX && y == maxY) return true;
		return false;
	}

	private Entity makeLifeform(final float pX, final float pY,
			TextureRegion charTexture) {			
		float wid = charTexture.getWidth(); 
		float ht = charTexture.getHeight();
		float xpos = pX;// - (wid/2.0f); 
		float ypos = pY;// - (ht/2.0f);
		final Sprite coloredRect = new Sprite(xpos, ypos, charTexture);
		coloredRect.setVisible(false);
		Log.v("GOL","Lifeform: x="+xpos+",y="+ypos+", wid="+wid+", ht="+ht);
		recordSceneMinMax(xpos, wid, ypos, ht);
		return coloredRect;
	}

	private Entity makeBackCell(final float px, final float py, TextureRegion backTexture) {
//		float wid = BACKTILEW; 
//		float ht = BACKTILEH;
		
		float wid = backTexture.getWidth(); 
		float ht = backTexture.getHeight();
		float xpos = px;// - (wid/2.0f); 
		float ypos = py;// - (ht/2.0f); 

		final Sprite backTile  = new Sprite(xpos, ypos, backTexture);
//		final Rectangle backTile = new Rectangle(xpos, ypos, wid, ht);		
//		backTile.setColor(1, 1, 1, 0.25f);
		backTile.setVisible(true);
		Log.v("GOL","Backtile: x="+xpos+",y="+ypos+", wid="+wid+", ht="+ht);
		recordSceneMinMax(xpos, wid, ypos, ht);
		return backTile;
	}

	private void recordSceneMinMax(float xpos, float wid, float ypos, float ht) {
		if (xpos < actualMinX) actualMinX = xpos;
		if ((xpos + wid) > actualMaxX) actualMaxX = xpos + wid;
		if (ypos < actualMinY) actualMinY = ypos;
		if ((ypos + ht) > actualMaxY) actualMaxY = ypos + ht;
	}
	
	public float getActualMaxX() {
		Log.d("GOL", "Actual Max X="+actualMaxX);
		return actualMaxX;
	}

	public float getActualMinX() {
		Log.d("GOL", "Actual Min X="+actualMinX);
		return actualMinX;
	}
	public float getActualMaxY() {
		Log.d("GOL", "Actual Max Y="+actualMaxY);
		return actualMaxY;
	}
	public float getActualMinY() {
		Log.d("GOL", "Actual Min Y="+actualMinY);
		return actualMinY;
	}

	public void render(Scene s) {
		for (int row = 0; row < cellRows; row++) {
			for (int col = 0; col < cellColumns; col++) {
				entityList.get(row * cellColumns + col).setVisible(
						grid.getCell(row, col));
			}
		}
	}

	public void pulse() {
		grid.next();
		generation++;
	}

	// Shape management

	void setShape(LifeShape shape) throws ShapeException {
		int xOffset;
		int yOffset;

		// get shape properties
		// shapeGrid = shape.getShape();
		int shapeWidth = shape.getWidth();
		int shapeHeight = shape.getHeight();
		int gridWidth = grid.getWidth();
		int gridHeight = grid.getHeight();

		if (shapeWidth > gridWidth || shapeHeight > gridHeight)
			throw new ShapeException("Shape doesn't fit on canvas (grid: "
					+ gridWidth + "x" + gridHeight + ", shape: " + shapeWidth
					+ "x" + shapeHeight + ")"); // shape doesn't fit on canvas

		// center the shape
//		xOffset = 0;//(gridWidth - shapeWidth) / 2;
//		yOffset = 0;//(gridHeight - shapeHeight) / 2;
		xOffset = (gridWidth - shapeWidth) / 2;
		yOffset = (gridHeight - shapeHeight) / 2;
		grid.clear();

		// draw shape
		Enumeration<int[]> cells = shape.getCells();
		while (cells.hasMoreElements()) {
			int[] cell = cells.nextElement();
			grid.setCell(xOffset + cell[0], yOffset + cell[1], true);
		}

	}

	public int getCurrGeneration() {
		return generation;
	}

	public float getCenterX() {
		return actualMinX + (actualMaxX-actualMinX)/2;
	}

	public float getCenterY() {
		return actualMinY + (actualMaxY-actualMinY)/2;
	}

}
