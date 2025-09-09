package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.overlays.Overlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class OverlayWrapperScreen extends BaseScreen {

    OverlayManager manager;

    public OverlayWrapperScreen(Overlay overlay) {

        if(OverlayManager.instance == null) {
            OverlayManager.instance = new OverlayManager();
        }

        tickGamepadManager = false;

        manager = OverlayManager.instance;
        manager.push(overlay);
    }

    @Override
    protected void draw(float delta) {
        super.draw(delta);
        manager.draw(delta);
    }

    @Override
    protected void tick(float delta) {
        super.tick(delta);

        if(manager.current() == null) {
            GameApplication.SetScreen(new MainMenuScreen());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        manager.resize(width, height);
    }
}
