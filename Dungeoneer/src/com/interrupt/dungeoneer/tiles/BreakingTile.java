package com.interrupt.dungeoneer.tiles;

import com.interrupt.dungeoneer.game.Game;

public class BreakingTile extends Tile {
	public BreakingTile() {
		blockMotion = true;
		renderSolid = true;
		wallTex = 0;
	}
	
	public void use()
	{
		renderSolid = false;
		blockMotion = false;
	}
}
