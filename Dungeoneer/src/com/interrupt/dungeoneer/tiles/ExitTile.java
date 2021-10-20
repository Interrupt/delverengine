package com.interrupt.dungeoneer.tiles;

import java.text.MessageFormat;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.ReadableKeys;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.dungeoneer.interfaces.LookAt;
import com.interrupt.dungeoneer.interfaces.LookAtInfoModifier;
import com.interrupt.managers.StringManager;

public class ExitTile extends Tile implements LookAt {
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
			Game.message2.add(StringManager.get("tiles.ExitTile.cannotLeaveText"), 4f);
		}
		else {
			GameApplication.ShowGameOverScreen(true);
		}
	}

    @Override
    public void getLookAtInfo(LookAtInfoModifier modifier) {
        String useText = ReadableKeys.keyNames.get(Actions.keyBindings.get(Action.USE));
        if(Game.isMobile) useText = StringManager.get("entities.Player.mobileUseText");

        String title = MessageFormat.format(StringManager.get("entities.Player.exitDungeonText"), useText);

        modifier.modify(title, null, Color.WHITE);
    }
}
