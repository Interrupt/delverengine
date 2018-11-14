package com.interrupt.dungeoneer.entities.areas;

import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.tiles.Tile;

public class SpawnBlockerArea extends Area {
    public SpawnBlockerArea() { hidden = true; spriteAtlas = "editor"; tex = 11; isStatic = true; isDynamic = false; }

    @Override
    public void init(Level level, Level.Source source) {
        for (float tx = x - collision.x; tx < x + collision.x; tx++) {
            for (float ty = y - collision.y; ty < y + collision.y; ty++) {
                Tile t = level.getTileOrNull((int) tx, (int) ty);
                if (t != null) t.canTeleportHere = false;
            }
        }
    }
}
