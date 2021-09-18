package com.interrupt.dungeoneer.ui.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;
import com.interrupt.dungeoneer.ui.UiSkin;

public class PlayerStatusEffects extends Element {
    HashMap<StatusEffect, Label> children = new HashMap<>();

    @Override
    protected Actor createActor() {
        VerticalGroup group = new VerticalGroup() {
            @Override
            public void act(float delta) {
                super.act(delta);

                Array<StatusEffect> statusEffects = Game.instance.player.statusEffects;

                // Create new status effect lables
                if (statusEffects != null) {
                    for (int i = 0; i < statusEffects.size; i++) {
                        final StatusEffect statusEffect = statusEffects.get(i);

                        if (children.get(statusEffect) != null) {
                            continue;
                        }

                        Label label = createLabel(statusEffect);
                        addActor(label);
                        children.put(statusEffect, label);
                    }
                }

                // Remove old status effect labels
                if (children.size() > 0) {
                    Iterator<Entry<StatusEffect, Label>> iterator = children.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Entry<StatusEffect, Label> child = iterator.next();

                        if (statusEffects == null || !statusEffects.contains(child.getKey(), true)) {
                            child.getValue().remove();
                            iterator.remove();
                        }
                    }
                }

                pack();
            }
        };

        // group.setDebug(true, true);

        return group;
    }

    private Label createLabel(final StatusEffect effect) {
        Label label = new Label(getLabelText(effect), UiSkin.getSkin()) {
            StatusEffect statusEffect = effect;

            @Override
            public void act(float delta) {
                super.act(delta);

                if (statusEffect == null) {
                    setText("");
                    return;
                }

                setText(getLabelText(effect));
            }
        };

        label.setColor(Color.WHITE);

        return label;
    }

    private String getLabelText(StatusEffect statusEffect) {
        String message = statusEffect.name;

        if (statusEffect.timer < 10000) {
            int secondsRemaining = (int) (statusEffect.timer / 100f + 1);

            // Make sure we do not "jump" from "X EFFECT" to "EFFECT" in a frame
            //  when timer reaches 0.
            secondsRemaining = Math.max(1, secondsRemaining);

            if (secondsRemaining > 0) {
                message = secondsRemaining + " " + statusEffect.name;
            }
        }

        return message;
    }
}
