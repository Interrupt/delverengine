package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class FogSprite extends Sprite {
    public FogSprite() { tex = 1; spriteAtlas = "fog_sprites"; shader = "fog"; blendMode = BlendMode.ALPHA; color = new Color(); detailLevel = DetailLevel.HIGH; }

    boolean animate = false;

    @EditorProperty
    public Color fogColor = new Color(1f, 1f, 1f, 1.0f);

    @Override
    public void init(Level level, Level.Source source) {
        color = fogColor;
        float seed = Game.rand.nextFloat();
    }

    @Override
    public void updateLight(Level level) {
        super.updateLight(level);

        if(!fullbrite) {
            color.r *= fogColor.r;
            color.g *= fogColor.g;
            color.b *= fogColor.b;
            color.a = fogColor.a;
        }
    }

    @Override
    public void updateDrawable() {
        if(!fullbrite) {
            color.r *= fogColor.r;
            color.g *= fogColor.g;
            color.b *= fogColor.b;
            color.a = fogColor.a;
        }
        super.updateDrawable();
    }
}
