package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.MapRenderer;

public class Map extends Element {
    @Override
    protected Actor createActor() {
        MapRenderer mapRenderer = GameManager.renderer.mapRenderer;
        if (mapRenderer.mapTexture == null) {
            mapRenderer.makeMapTextureForLevel(Game.instance.level);
        }

        return new Image(mapRenderer.mapTexture);
    }
}
