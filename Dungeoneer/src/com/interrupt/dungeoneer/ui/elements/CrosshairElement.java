package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class CrosshairElement extends Element {
    public Color color = Color.WHITE;
    public String imagePath = "ui/crosshair.png";

    private Image image;

    @Override
    public Actor createActor() {
        FileHandle file = Game.getInternal(imagePath);
        if (!file.exists()) {
            return null;
        }

        image = new Image(new Texture(file)) {
            @Override
            public void act(float delta) {
                boolean shouldDraw = shouldDrawCrosshair();
                this.setVisible(shouldDraw);

                if (!shouldDraw) return;

                Item heldItem = Game.instance.player.GetHeldItem();

                String path = imagePath;
                if (heldItem.crosshairImagePath != null && !heldItem.crosshairImagePath.isEmpty()) {
                    path = heldItem.crosshairImagePath;
                }

                FileHandle file = Game.getInternal(path);
                if (!file.exists()) {
                    return;
                }

                image.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(file))));
            }
        };

        image.setColor(color.r, color.g, color.b, 0.35f);

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
