package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Canvas implements Disposable {
    private Viewport viewport;
    private Stage stage;

    private final int minWidth = 640;
    private final int minHeight = 360;

    public int width = minWidth;
    public int height = minHeight;

    public Array<Element> children = new Array<>();

    public Canvas() {}

    public void init() {
        if (viewport == null) {
            calculateScaling();
            viewport = new FitViewport(width, height);
        }

        if (stage == null) {
            stage = new Stage(viewport);

            for (Element child : children) {
                Actor actor = child.getActor();
                positionActor(actor, child);
                stage.addActor(actor);
            }
        }
    }

    private void reinit() {
        viewport = null;
        stage.dispose();
        stage = null;
        init();
    }

    public void draw() {
        viewport.apply();
        stage.draw();
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void resize (int width, int height) {
        calculateScaling();
        viewport.setWorldSize(width, height);
        viewport.update(width, height, true);
        reinit();
    }

    private void calculateScaling() {
        int scale = 1;
        int displayWidth = Gdx.graphics.getWidth();
        int displayHeight = Gdx.graphics.getHeight();

        for (int i = 4; i > 1; i--) {
            if (displayWidth / i >= minWidth && displayHeight / i >= minHeight) {
                scale = i;
                break;
            }
        }

        width = displayWidth / scale;
        height = displayHeight / scale;

        if (width < minWidth || height < minHeight) {
            width = minWidth;
            height = minHeight;
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void positionActor(Actor actor, Element element) {
        int x = element.x;
        int y = element.y;

        switch (element.anchor) {
            case bottom_left:
                break;

            case bottom_center:
                x -= actor.getWidth() / 2;
                x += width / 2;
                break;

            case bottom_right:
                x -= actor.getWidth();
                x += width;
                break;

            case center_left:
                y -= actor.getHeight() / 2;
                y += height / 2;
                break;

            case center:
                x -= actor.getWidth() / 2;
                x += width / 2;

                y -= actor.getHeight() / 2;
                y += height / 2;
                break;

            case center_right:
                x -= actor.getWidth();
                x += width;

                y -= actor.getHeight() / 2;
                y += height / 2;
                break;

            case top_left:
                y -= actor.getHeight();
                y += height;
                break;

            case top_center:
                x -= actor.getWidth() / 2;
                x += width / 2;

                y -= actor.getHeight();
                y += height;
                break;

            case top_right:
                x -= actor.getWidth();
                x += width;

                y -= actor.getHeight();
                y += height;
                break;

        }
        actor.setPosition(x, y);
    }
}
