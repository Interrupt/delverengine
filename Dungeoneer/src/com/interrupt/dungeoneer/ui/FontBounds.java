package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class FontBounds {
    private static GlyphLayout glyphLayout = new GlyphLayout();

    public static GlyphLayout GetBounds(BitmapFont font, String text) {
        glyphLayout.setText(font, text);
        return glyphLayout;
    }
}
