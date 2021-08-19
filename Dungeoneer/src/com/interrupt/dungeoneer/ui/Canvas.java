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

    private int minWidth = 640;
    private int minHeight = 360;

    private int width = minWidth;
    private int height = minHeight;

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
        int displayWidth = Gdx.graphics.getWidth();
        int displayHeight = Gdx.graphics.getHeight();
        int maxPixelScale = Math.min(displayWidth / minWidth, displayHeight / minHeight);

        int pixelScale = 1;
        for (int i = maxPixelScale; i > 1; i--) {
            if (displayWidth / i >= minWidth && displayHeight / i >= minHeight) {
                pixelScale = i;
                break;
            }
        }

        width = displayWidth / pixelScale;
        height = displayHeight / pixelScale;

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
            case BOTTOM_LEFT:
                break;

            case BOTTOM_CENTER:
                x -= actor.getWidth() / 2;
                x += width / 2;
                break;

            case BOTTOM_RIGHT:
                x = width - x;
                x -= actor.getWidth();
                break;

            case CENTER_LEFT:
                y -= actor.getHeight() / 2;
                y += height / 2;
                break;

            case CENTER:
                x -= actor.getWidth() / 2;
                x += width / 2;

                y -= actor.getHeight() / 2;
                y += height / 2;
                break;

            case CENTER_RIGHT:
                x = width - x;
                x -= actor.getWidth();

                y -= actor.getHeight() / 2;
                y += height / 2;
                break;

            case TOP_LEFT:
                y = height - y;
                y -= actor.getHeight();
                break;

            case TOP_CENTER:
                x -= actor.getWidth() / 2;
                x += width / 2;

                y = height - y;
                y -= actor.getHeight();
                break;

            case TOP_RIGHT:
                x = width - x;
                x -= actor.getWidth();

                y = height - y;
                y -= actor.getHeight();
                break;

        }
        actor.setPosition(x, y);
    }
}
