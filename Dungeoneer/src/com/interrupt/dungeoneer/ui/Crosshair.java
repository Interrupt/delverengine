package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class Crosshair implements IHUDRenderable {
    private Color color = new Color(1f, 1f, 1f, 0.35f);
    private float size = 18f;

    public void draw() {
        if (!shouldDrawCrosshair()) {
            return;
        }

        GameManager.renderer.uiBatch.begin();
        GameManager.renderer.uiBatch.setColor(Color.WHITE);
        // TODO: Provide option to use texture instead?
        GameManager.renderer.drawText("+", -0.5f * size, -0.65f * size, size, color);
        GameManager.renderer.uiBatch.end();
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
