package com.interrupt.dungeoneer.partitioning;

import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.FrustumShapeBuilder;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.entities.Light;
import com.interrupt.dungeoneer.entities.SpotLight;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.GlRenderer;

public class LightSpatialHash {
	private final int cellSize;
	private IntMap<Array<Light>> hash = new IntMap<Array<Light>>();

	private Array<Light> temp = new Array<Light>();
	private IntArray cellKeys = new IntArray();

	public LightSpatialHash(int cellSize)  {
		this.cellSize = cellSize;
	}

	public int getKey(float x, float y) {
		return ((int)Math.floor(x) / cellSize) + (int)((int)Math.floor(y) / cellSize) * 3000;
	}

	// returns all the cell keys near a point
	private IntArray getCellsNear(float x, float y, float colSize) {
		cellKeys.clear();

		int minX = (int)Math.floor(x - colSize) / cellSize;
		int maxX = (int)Math.floor(x + colSize) / cellSize;
		int minY = (int)Math.floor(y - colSize) / cellSize;
		int maxY = (int)Math.floor(y + colSize) / cellSize;

		for(int xx = minX; xx <= maxX; xx++) {
			for(int yy = minY; yy <= maxY; yy++) {
				int key = getKey(xx,yy);
				if(!cellKeys.contains(key)) cellKeys.add(key);
			}
		}

		return cellKeys;
	}

    // returns all the cell keys in a frustrum
    private static BoundingBox t_calcFrustumBox = new BoundingBox();
    private IntArray getCellsInFrustrum(Frustum frustum) {
        cellKeys.clear();

        // Put this light in the spatial hash based on the bounds of the frustum
        BoundingBox box = t_calcFrustumBox;
        box.set(frustum.planePoints);

        int minX = (int)Math.floor(box.min.x) / cellSize;
        int maxX = (int)Math.floor(box.max.x) / cellSize;
        int minY = (int)Math.floor(box.min.y) / cellSize;
        int maxY = (int)Math.floor(box.max.y) / cellSize;

        for(int xx = minX; xx <= maxX; xx++) {
            for(int yy = minY; yy <= maxY; yy++) {
                int key = getKey(xx,yy);
                if(!cellKeys.contains(key)) cellKeys.add(key);
            }
        }

        return cellKeys;
    }

	private synchronized void PutLight(int key, Light e) {
		Array<Light> eList = hash.get(key);
		if(eList == null) {
			eList = new Array<Light>();
			eList.add(e);

			hash.put(key, eList);
		}
		else {
			if(!eList.contains(e, true))
				eList.add(e);
		}
	}

	public synchronized void AddLight(Light e) {
		IntArray near = cellKeys;
        if(e instanceof SpotLight) {
            near = getCellsInFrustrum(((SpotLight)e).getFrustum());
        }
        else {
            near = getCellsNear(e.x, e.y, e.range);
        }

		for(int i = 0; i < near.size; i++) {
			PutLight(near.get(i), e);
		}
	}

	// Returns lights found near an entity
	public Array<Light> getLightsAt(float x, float y) {
		int cell = getKey(x,y);
		Array<Light> inCell = hash.get(cell);
		if(inCell != null) {
			temp.clear();
			for(Light light : inCell) {
				if(!temp.contains(light, true)) temp.add(light);
			}
			return temp;
		}

		return null;
	}

	public synchronized Array<Light> getThreadSafeLightsAt(float x, float y, Array<Light> outLights) {
		int cell = getKey(x,y);
		Array<Light> inCell = hash.get(cell);
		if(inCell != null) {
			for(Light light : inCell) {
				if(!outLights.contains(light, true)) outLights.add(light);
			}
			return outLights;
		}
		return null;
	}

	public void Clear() {
		hash.clear();
	}

	public synchronized void Flush() {
		for(Array<Light> val : hash.values()) {
			val.clear();
		}
	}
}
