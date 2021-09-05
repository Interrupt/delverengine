package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;

public class Group extends Element {
    public enum Overflow {
        VISIBLE,
        HIDDEN
    }

    public int width;
    public int height;
    public Array<Element> children = new Array<>();
    public Overflow overflow = Overflow.VISIBLE;

    @Override
    protected Actor createActor() {
        WidgetGroup group = new WidgetGroup() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (overflow == Overflow.VISIBLE) {
                    drawOverflowVisible(batch, parentAlpha);
                }
                else {
                    drawOverflowHidden(batch, parentAlpha);
                }
            }

            private void drawOverflowVisible(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
            }

            private final Rectangle scissors = new Rectangle();
            private final Rectangle area = new Rectangle(getX(), getY(), getWidth(), getHeight());

            private void drawOverflowHidden(Batch batch, float parentAlpha) {
                area.set(getX(), getY(), getWidth(), getHeight());

                ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), area, scissors);
                if (ScissorStack.pushScissors(scissors)) {
                    super.draw(batch, parentAlpha);
                    ScissorStack.popScissors();
                }

                batch.flush();
            }
        };
        group.setWidth(width);
        group.setHeight(height);

        for (Element child : children) {
            child.init();
            child.setCanvas(canvas);
            Actor actor = child.getActor();
            positionActor(actor, child);
            group.addActor(actor);
        }

        return group;
    }

    public void positionActor(Actor actor, Element element) {
        Vector2 origin = new Vector2();
        Vector2 position = new Vector2(element.x, element.y);
        Vector2 offset = new Vector2();

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
                break;

            case TOP_LEFT:
                origin.y = height;
                break;

            case TOP_CENTER:
                origin.x = width / 2;
                origin.y = height;
                break;

            case TOP_RIGHT:
                origin.x += width;
                origin.y += height;
                break;
        }

        position.add(origin).add(offset);
        actor.setPosition(position.x, position.y);
    }
}
