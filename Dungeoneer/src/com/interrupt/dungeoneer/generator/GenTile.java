package com.interrupt.dungeoneer.generator;

public class GenTile {
	public enum TileTypes { beginning, start, corner, hall, intersection, tri_intersection, end, finish };
	public TileTypes type;
	public int rot = 0;
	public int exits = 0;
	
	public boolean exitLeft = false;
	public boolean exitTop = false;
	public boolean exitRight = false;
	public boolean exitBottom = false;

	public GenTile(TileTypes type, int rot, int exits) {
		this.type = type;
		this.rot = rot;
		this.exits = exits;
		
		if(type == TileTypes.start || type == TileTypes.end || type == TileTypes.beginning || type == TileTypes.finish) {
			exitLeft = true;
		}
		else if (type == TileTypes.hall) {
			exitLeft = true;
			exitRight = true;
		}
		else if(type == TileTypes.intersection) {
			exitLeft = true;
			exitRight = true;
			exitTop = true;
			exitBottom = true;
		}
		else if (type == TileTypes.corner) {
			exitRight = true;
			exitBottom = true;
		}
		else if(type == TileTypes.tri_intersection) {
			exitLeft = true;
			exitBottom = true;
			exitRight = true;
		}
		
		rotate(rot);
	}

	public void rotate(int times) {
		for(int i = 0; i < times; i++) {
			boolean swapTop = exitTop;
			boolean swapRight = exitRight;
			boolean swapBottom = exitBottom;
			boolean swapLeft = exitLeft;

			exitTop = swapLeft;
			exitRight = swapTop;
			exitBottom = swapRight;
			exitLeft = swapBottom;
		}
	}
	
	public boolean canConnect(GenTile otherTile, int dir) {
		if(dir == 0 && exitLeft && otherTile.exitRight)
			return true;
		if(dir == 1 && exitTop && otherTile.exitBottom)
			return true;
		if(dir == 2 && exitRight && otherTile.exitLeft)
			return true;
		if(dir == 3 && exitBottom && otherTile.exitTop)
			return true;
		
		return false;
	}
}
