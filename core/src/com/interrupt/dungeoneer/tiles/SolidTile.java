package com.interrupt.dungeoneer.tiles;

import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.game.Game;

public class SolidTile extends Tile {
	public SolidTile() {
		blockMotion = true;
		renderSolid = true;
		
		wallTex = 0;
	}
}
