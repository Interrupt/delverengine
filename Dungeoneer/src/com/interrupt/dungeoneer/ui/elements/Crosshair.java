package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.ui.UiSkin;

public class Crosshair extends Element {
    public String text = "+";
    public Color color = Color.WHITE;

    public Crosshair() {
        anchor = com.interrupt.dungeoneer.ui.elements.Align.CENTER;
    }

    @Override
    public Actor createActor() {
        Label label = new Label(text, UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                this.setVisible(shouldDrawCrosshair());
            }
        };

        label.setPosition(x, y);
        label.setColor(color.r, color.g, color.b, 1f);
        label.setAlignment(Align.center);

        return label;
    }

    private boolean shouldDrawCrosshair() {
        // TODO: Provide a proper boolean for this on a state manager?
        if (GameManager.renderer.cutsceneCamera != null && GameManager.renderer.cutsceneCamera.isActive) {
            return false;
        }

        // TODO: Provide a proper boolean for this on a state manager?
        if (OverlayManager.instance.current() != null && OverlayManager.instance.current().catchInput) {
            return false;
        }

        if (Options.instance.hideUI) {
            return false;
        }

        if (Options.instance.alwaysShowCrosshair) {
            return true;
        }

        Item heldItem = Game.instance.player.GetHeldItem();
        if (heldItem == null) {
            return false;
        }

        return heldItem.showCrosshair;
    }
}
