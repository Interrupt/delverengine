package com.interrupt.dungeoneer.tiles;

import java.util.Random;

import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.GameData;
import com.interrupt.managers.StringManager;

public class ExitTile extends Tile {
	public ExitTile() {
		blockMotion = true;
		renderSolid = true;
		wallTex = 0;
	}
	
	public void use()
	{
		Player p = Game.instance.player;
		
		boolean hasOrb = false;
		for(int i = 0; i < p.inventory.size; i++) {
			if(p.inventory.get(i) instanceof QuestItem) {
				hasOrb = true;
			}
		}

		if(p.getCurrentTravelKey() != null) {
			Game.instance.doLevelExit(null);
		}
		else if(!hasOrb) {
			Game.ShowMessage(StringManager.get("tiles.ExitTile.cannotLeaveText"), 4, 1f);
		}
		else {
			GameApplication.ShowGameOverScreen(true);
		}
	}
}
