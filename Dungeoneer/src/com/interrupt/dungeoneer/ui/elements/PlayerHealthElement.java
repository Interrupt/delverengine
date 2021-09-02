package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.UiSkin;

import java.text.MessageFormat;

public class PlayerHealthElement extends Element {
    String text = "{0}/{1}";

    @Override
    public Actor createActor() {
        Label label = new Label(text, UiSkin.getSkin()) {
            private int hp;
            private int maxHp;

            @Override
            public void act(float delta) {
                super.act(delta);
                Player player = Game.instance.player;

                if (hp != player.hp || maxHp != player.maxHp) {
                    hp = player.hp;
                    maxHp = player.maxHp;
                    String formattedText = MessageFormat.format(text, hp, maxHp);
                    setText(formattedText);
                }
            }
        };
        label.setColor(Color.WHITE);

        switch (pivot) {
            case BOTTOM_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottomLeft);
                break;

            case BOTTOM_CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottom | com.badlogic.gdx.utils.Align.center);
                break;

            case BOTTOM_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottomRight);
                break;

            case CENTER_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.left | com.badlogic.gdx.utils.Align.center);
                break;

            case CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.center);
                break;

            case CENTER_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.right | com.badlogic.gdx.utils.Align.center);
                break;

            case TOP_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.topLeft);
                break;

            case TOP_CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.top | com.badlogic.gdx.utils.Align.center);
                break;

            case TOP_RIGHT:
                label.setAlignment(Align.topRight);
                break;
        }

        return label;
    }
}
