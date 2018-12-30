package com.interrupt.dungeoneer.partitioning;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.*;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.Tile.TileSpaceType;

public class TriangleSpatialHash {
	private final int cellSize;
	private IntMap<Array<Triangle>> hash = new IntMap<Array<Triangle>>();
	
	private Array<Triangle> temp = new Array<Triangle>();
	private IntArray cellKeys = new IntArray();
	
	public TriangleSpatialHash(int cellSize)  {
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
	
	// returns all the cell keys along a ray
	private IntArray getCellsAlong(Ray ray, float distance) {
		cellKeys.clear();
		Vector3 end = CachePools.getVector3();
		ray.getEndPoint(end, distance);
		
		// go along a line collecting cells
		float dx = Math.abs(ray.origin.x - end.x);
	    float dy = Math.abs(ray.origin.z - end.z);
	    float s = (float)(0.99f/(dx>dy?dx:dy));
	    float t = 0.0f;
	    
	    while(t <= 1f) {
	        dx = (((1.0f - t)*ray.origin.x + t * end.x));
	        dy = (((1.0f - t)*ray.origin.z + t * end.z));
	        
	        int key = getKey(dx,dy);
			if(!cellKeys.contains(key)) cellKeys.add(key);
	        
        	t += s * cellSize * 0.5f;
	    }
	    
	    CachePools.freeVector3(end);
	    
	    return cellKeys;
	}
	
	// returns all the cell in a frustum
	private IntArray getCellsIn(Frustum frustum) {
		cellKeys.clear();
		
		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;
		
		boolean first = true;
		for(Vector3 point : frustum.planePoints) {
			if(first) {
				minX = (int)point.x;
				minY = (int)point.z;
				maxX = (int)point.x;
				maxY = (int)point.z;
				first = false;
			}
			else {
				if((int)point.x <= minX) minX = (int)point.x - 1;
				if((int)point.z <= minY) minY = (int)point.z - 1;
				if((int)point.x >= maxX) maxX = (int)point.x + 1;
				if((int)point.z >= maxY) maxY = (int)point.z + 1;
			}
		}
		
		for(int xx = (int)minX; xx <= (int)maxX; xx++) {
			for(int yy = (int)minY; yy <= (int)maxY; yy++) {
				int key = getKey(xx,yy);
				if(!cellKeys.contains(key)) cellKeys.add(key);
			}
		}
		
		return cellKeys;
	}
	
	private void PutTriangle(int key, Triangle e) {
		Array<Triangle> eList = hash.get(key);
		if(eList == null) {
			eList = new Array<Triangle>();
			eList.add(e);
			
			hash.put(key, eList);
		}
		else {
			if(!eList.contains(e, true))
				eList.add(e);
		}
	}
	
	public void AddTriangle(Triangle e) {
		int key = getKey(e.v1.x, e.v1.z);
		PutTriangle(key, e);
		
		key = getKey(e.v2.x, e.v2.z);
		PutTriangle(key, e);
		
		key = getKey(e.v3.x, e.v3.z);
		PutTriangle(key, e);
	}

	public void dropWorldChunk(WorldChunk chunk) {
		temp.clear();
		IntArray cells = getCellsNear(chunk.getWorldX(), chunk.getWorldY(), chunk.getRadius());

		// drop triangles in these cells
		for(int i = 0; i < cells.size; i++) {
			int cell = cells.get(i);
			Array<Triangle> inCell = hash.get(cell);
			if(inCell != null) {
				inCell.clear();
			}
		}
	}
	
	// Returns triangles found near an entity
	public Array<Triangle> getTrianglesAt(float x, float y, float colSize) {
		temp.clear();
		IntArray cells = getCellsNear(x, y, colSize);
		
		// look in all the nearby cells for triangles
		for(int i = 0; i < cells.size; i++) {
			int cell = cells.get(i);
			Array<Triangle> inCell = hash.get(cell);
			if(inCell != null) {
				for(Triangle triangle : inCell) {
					if(!temp.contains(triangle, true)) temp.add(triangle);
				}
			}
		}
		
		return temp;
	}
	
	// Returns all the triangles found along a ray
	public Array<Triangle> getTrianglesAlong(Ray ray, float length) {
		temp.clear();
		IntArray cells = getCellsAlong(ray, length);
		
		// look in all the nearby cells for triangles
		for(int i = 0; i < cells.size; i++) {
			int cell = cells.get(i);
			Array<Triangle> inCell = hash.get(cell);
			if(inCell != null) {
				for(Triangle triangle : inCell) {
					if(!temp.contains(triangle, true)) temp.add(triangle);
				}
			}
		}
		
		return temp;
	}
	
	// Returns all the triangles in a frustum
	public Array<Triangle> getTrianglesIn(Frustum frustum) {
		temp.clear();
		IntArray cells = getCellsIn(frustum);
		
		// look in all the nearby cells for triangles
		for(int i = 0; i < cells.size; i++) {
			int cell = cells.get(i);
			Array<Triangle> inCell = hash.get(cell);
			if(inCell != null) {
				for(Triangle triangle : inCell) {
					if(!temp.contains(triangle, true)) temp.add(triangle);
				}
			}
		}
		
		return temp;
	}
	
	public void Clear() {
		hash.clear();
	}
	
	public void Flush() {
		for(Array<Triangle> val : hash.values()) {
			val.clear();
		}
	}

    public Array<Triangle> getAllTriangles() {
        temp.clear();

        for(Array<Triangle> val : hash.values()) {
            temp.addAll(val);
        }

        return temp;
    }
}
