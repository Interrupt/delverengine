package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Canvas implements Disposable {
    private Viewport viewport;
    private Stage stage;

    public int width = 640;
    public int height = 360;

    public Array<Element> children = new Array<>();

    public Canvas() {}

    public void init() {
        if (viewport == null) {
            viewport = new FitViewport(width, height);
        }

        if (stage == null) {
            stage = new Stage(viewport);

            for (Element child : children) {
                Actor actor = child.getActor();
                stage.addActor(actor);
            }
        }
    }

    public void draw() {
        viewport.apply();
        stage.draw();
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void resize (int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void setDebug(boolean enabled) {
        stage.setDebugAll(enabled);
    }

    public boolean getDebug() {
        return stage.isDebugAll();
    }
}
