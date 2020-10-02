package com.interrupt.dungeoneer.gfx.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Text;

public class DrawableText extends Drawable {

    public String text = "";
    public Vector3 parentPosition = new Vector3();
    public Vector3 parentRotation = new Vector3();
    public Entity.EditorState editorState = Entity.EditorState.hovered;
    public Color pickingColor = Color.BLACK.cpy();
    public Color color = Color.WHITE.cpy();
    public float alignmentOffset = 0.5f;

    public DrawableText() {
    }

    public DrawableText(String text) {
        this.text = text;
    }

    public void update(Entity e) {
        scale = e.scale;
        blendMode = e.blendMode;
        fullbrite = e.fullbrite;

        parentPosition.set(e.x, e.y, e.z);
        parentRotation.set(e.getRotation());

        editorState = e.editorState;

        if (e instanceof Text) {
            Text parentText = (Text)e;

            color.set(parentText.textColor);

            text = parentText.text;

            if (parentText.substituteControlLiterals) {
                text = text.replace("\\n", "\n"); // Hack in support for newlines in the editor.
            }

            switch (parentText.textAlignment) {
                case LEFT:
                    alignmentOffset = 0.0f; break;
                case CENTER:
                    alignmentOffset = 0.5f; break;
                case RIGHT:
                    alignmentOffset = 1.0f; break;
            }
        }
    }

    public enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
