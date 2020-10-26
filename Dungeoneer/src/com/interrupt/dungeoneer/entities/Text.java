package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.gfx.drawables.DrawableText;
import com.interrupt.dungeoneer.ui.FontBounds;

public class Text extends DirectionalEntity {

    @EditorProperty
    public String text = "Text";

    @EditorProperty
    public Color textColor = new Color(Color.WHITE);

    @EditorProperty
    public DrawableText.TextAlignment textAlignment = DrawableText.TextAlignment.CENTER;

    @EditorProperty
    public boolean substituteControlLiterals = true;

    @Deprecated
    private String fontAtlas = "font";

    @Deprecated
    private float spacing = 1f;

    public Text() {
        this.drawable = new DrawableText(this.text);
        this.isDynamic = false;
    }

    @Override
    public void init(Level level, Level.Source source) {
        // Handle conversion of old text entities.
        if (drawable instanceof DrawableSprite) { // Under new circumstances, the drawable cannot be a DrawableSprite.
            drawable = new DrawableText();

            scale *= 5; // Scale is different now.
            textAlignment = DrawableText.TextAlignment.LEFT; // New default is center.
            attached.clear(); // The old text glyphs were attached sprites, so clear the attached entity list.

            GlyphLayout bounds = FontBounds.GetBounds(GameManager.renderer.font, text);
            Vector2 adjustment = Vector2.X.cpy().rotate(-rotation.z);

            x += adjustment.x * bounds.width / 2.0 * scale * 0.025f; // Correct for left-origin positioning.
            y += adjustment.y * bounds.width / 2.0 * scale * 0.025f;
            z += 0.4f; // Match offset to the new one of
        }
    }

    @Override
    public void onTrigger(Entity instigator, String value) {
        if (value == null || value.equals("")) {
            return;
        }

        this.text = value;
    }
}
