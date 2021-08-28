package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class Crosshair extends Element {
    public String imagePath = "ui/crosshair.png";
    public float imageScale = 0.4f;

    @Override
    public Actor createActor() {
        FileHandle file = Game.getInternal(imagePath);
        if (!file.exists()) {
            return null;
        }

        Image image = new Image(new Texture(file)) {
            @Override
            public void act(float delta) {
                this.setVisible(shouldDrawCrosshair());
            }
        };

        image.setScale(imageScale);
        image.setColor(1f, 1f, 1f, 0.35f);

        return image;
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
