package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class SpriteElement extends Element {
    public String spriteAtlas;
    public int tex;

    @Override
    public Actor createActor() {
        TextureAtlas atlas = TextureAtlas.getCachedRegion(spriteAtlas);
        Image image = new Image(atlas.getClippedSprite(tex));
        image.setPosition(x, y);

        return image;
    }
}
