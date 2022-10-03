package com.interrupt.dungeoneer.entities;

import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.LevelInterface;

public class WarpMarker extends DirectionalEntity {
	public WarpMarker() { hidden = true; spriteAtlas = "editor"; tex = 3; isSolid = false; }

	public enum WarpType { DESTINATION, PLAYER_START }

	@EditorProperty
	public WarpType warpType = WarpType.DESTINATION;

	@Override
	public void tick(LevelInterface level, float delta) { }

	@Override
	public void init(LevelInterface level, Level.Source source) {
		if(source == Level.Source.LEVEL_START && warpType == WarpType.PLAYER_START) {
            // FIXME: Player start location
			/*level.playerStartX = (int)(x);
			level.playerStartY = (int)(y);
			level.playerStartRot = null;*/

			if(Game.instance != null && Game.instance.player != null) {
				Game.instance.player.rot = (float) Math.toRadians(getRotation().z + 90f);
			}
		}
	}
}
