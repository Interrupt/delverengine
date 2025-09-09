package com.interrupt.dungeoneer.partitioning;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;

public class SpatialHash {
	private final int cellSize;
	private IntMap<Array<Entity>> hash = new IntMap<Array<Entity>>();

	private Array<Entity> temp = new Array<Entity>();

	public SpatialHash(int cellSize)  {
		this.cellSize = cellSize;
	}

	public int getKey(float x, float y) {
		return ((int)Math.floor(x) / cellSize) + (int)((int)Math.floor(y) / cellSize) * 3000;
	}

	private void PutEntity(int key, Entity e) {
		Array<Entity> eList = hash.get(key);
		if(eList == null) {
			eList = new Array<Entity>();
			eList.add(e);

			hash.put(key, eList);
		}
		else {
			if(!eList.contains(e, true))
				eList.add(e);
		}
	}

	public void AddEntity(Entity e) {
		int startX = (int)(e.x / cellSize - e.collision.x);
		int endX = (int)(e.x / cellSize + e.collision.x);
		int startY = (int)(e.y / cellSize - e.collision.y);
		int endY = (int)(e.y / cellSize + e.collision.y);

		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				int key = getKey(x, y);
				PutEntity(key, e);
			}
		}
	}

	public Array<Entity> getEntitiesAt(float x, float y, float colSize) {
		int startX = (int)(x / cellSize - colSize);
		int endX = (int)(x / cellSize + colSize);
		int startY = (int)(y / cellSize - colSize);
		int endY = (int)(y / cellSize + colSize);

		temp.clear();

        // Guard from super high values causing an infinite loop here
		if(startX == Integer.MAX_VALUE || startX == Integer.MIN_VALUE)
			return temp;
		if(startY == Integer.MAX_VALUE || startY == Integer.MIN_VALUE)
			return temp;

		for(int xi = startX; xi <= endX; xi++) {
			for(int yi = startY; yi <= endY; yi++) {
				int key = getKey(xi, yi);
				Array<Entity> entitiesHere = hash.get(key);
				if(entitiesHere != null) {
					for(Entity entity : entitiesHere) {
						if(!temp.contains(entity, true)) // make sure this entity only is returned once
							temp.add(entity);
					}
				}
			}
		}

		return temp;
	}

	public void Clear() {
		hash.clear();
	}

	public void Flush() {
		for(Array<Entity> val : hash.values()) {
			val.clear();
		}
	}
}
