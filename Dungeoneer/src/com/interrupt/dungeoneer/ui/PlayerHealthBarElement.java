package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;

public class PlayerHealthBarElement extends Element {
    public String image;

    @Override
    public Actor createActor() {
        FileHandle file = Game.getInternal(image);
        if (!file.exists()) {
            return null;
        }

        Texture texture = new Texture(file);
        TextureRegion region = new TextureRegion(texture);
        Image image = new Image(region) {
            int value = -1;
            @Override
            public void act(float delta) {
                super.act(delta);

                Player player = Game.instance.player;
                if (value == player.hp) return;
                value = player.hp;

                TextureRegionDrawable regionDrawable = (TextureRegionDrawable)getDrawable();
                TextureRegion r = regionDrawable.getRegion();

                float normalizedHealth = player.hp / (float)player.maxHp;
                r.setU2(normalizedHealth);
                setWidth((int)(texture.getWidth() * normalizedHealth));
            }
        };

        return image;
    }
}
