package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
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
                child.init();
                child.setCanvas(this);
                stage.addActor(child.getActor());
            }
        }

        layout();
    }

    private void reinit() {
        stage.dispose();
        stage = null;
        viewport = null;
        init();
    }

    private void layout() {
        for (Element child : children) {
            positionActor(child.getActor(), child);
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
        Vector2 origin = new Vector2();
        Vector2 position = new Vector2(element.x, element.y);
        Vector2 offset = new Vector2();

        int x = element.x;
        int y = element.y;

        Align pivot = element.pivot;
        if (pivot.equals(Align.UNSET)) {
            pivot = element.anchor;
        }

        switch (pivot) {
            case BOTTOM_LEFT:
                break;

            case BOTTOM_CENTER:
                offset.x = -(int)(actor.getWidth() / 2);
                break;

            case BOTTOM_RIGHT:
                offset.x = -(int)(actor.getWidth());
                break;

            case CENTER_LEFT:
                offset.y = -(int)(actor.getHeight() / 2);
                break;

            case CENTER:
                offset.x = -(int)(actor.getWidth() / 2);
                offset.y = -(int)(actor.getHeight() / 2);
                break;

            case CENTER_RIGHT:
                offset.x = -(int)(actor.getWidth());
                offset.y = -(int)(actor.getHeight() / 2);
                break;

            case TOP_LEFT:
                offset.y = -(int)(actor.getHeight());
                break;

            case TOP_CENTER:
                offset.x = -(int)(actor.getWidth() / 2);
                offset.y = -(int)(actor.getHeight());
                break;

            case TOP_RIGHT:
                offset.x = -(int)(actor.getWidth());
                offset.y = -(int)(actor.getHeight());
                break;
        }

        switch (element.anchor) {
            case BOTTOM_LEFT:
                break;

            case BOTTOM_CENTER:
                origin.x = width / 2;
                break;

            case BOTTOM_RIGHT:
                origin.x = width;
                position.x *= -1;
                break;

            case CENTER_LEFT:
                origin.y = height / 2;
                break;

            case CENTER:
                origin.x = width / 2;
                origin.y = height / 2;
                break;

            case CENTER_RIGHT:
                origin.x = width;
                origin.y = height / 2;
                position.x *= -1;
                break;

            case TOP_LEFT:
                origin.y = height;
                position.y *= -1;
                break;

            case TOP_CENTER:
                origin.x = width / 2;
                origin.y = height;
                position.y *= -1;
                break;

            case TOP_RIGHT:
                origin.x += width;
                origin.y += height;
                position.x *= -1;
                position.y *= -1;
                break;
        }

        position.add(origin).add(offset);
        actor.setPosition(position.x, position.y);
    }
}
