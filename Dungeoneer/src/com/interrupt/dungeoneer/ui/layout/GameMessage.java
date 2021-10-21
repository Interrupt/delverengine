package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.UiSkin;

public class GameMessage extends Element {
    @Override
    protected Actor createActor() {
        VerticalGroup group = new VerticalGroup();

        group.addActor(createMessageLabel());
        group.addActor(createPromptLabel());
        group.addActor(createAttributesLabel());

        group.debug();
        group.columnAlign(Align.center);
        group.space(4f);

        return group;
    }

    private Label createMessageLabel() {
        Label label = new Label("MESSAGE", UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);

                if (Game.message2.hasActiveMessage()) {
                    setText(Game.message2.getMessage());
                    setColor(Color.WHITE);
                    setVisible(true);
                }
                else {
                    setText("");
                    setVisible(false);
                }
            }
        };

        label.debug();

        return label;
    }

    private Label createPromptLabel() {
        Label label = new Label("TITLE", UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);

                if (!Game.instance.shouldShowUseMessage()) {
                    setText("");
                    setVisible(false);
                    return;
                }

                setText(Game.instance.player.lookedAtInfo.getPrompt());
                setColor(Game.instance.player.lookedAtInfo.getColor());
                setVisible(true);
            }
        };

        label.debug();

        return label;
    }

    private Label createAttributesLabel() {
        Label label = new Label("ATTRIBUTES", UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);

                if (!Game.instance.shouldShowUseMessage()) {
                    setText("");
                    setVisible(false);
                    return;
                }

                setText(Game.instance.player.lookedAtInfo.getAdditionalInfo());
                setVisible(true);
            }
        };

        label.debug();
        label.setAlignment(Align.center);

        return label;
    }
}
