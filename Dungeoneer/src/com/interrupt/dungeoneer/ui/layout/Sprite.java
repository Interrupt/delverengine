package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class Sprite extends Element {
    public String spriteAtlas;
    public int tex;

    @Override
    public Actor createActor() {
        TextureAtlas atlas = TextureAtlas.getCachedRegion(spriteAtlas);
        Image image = new Image(atlas.getClippedSprite(tex));

        return image;
    }
}