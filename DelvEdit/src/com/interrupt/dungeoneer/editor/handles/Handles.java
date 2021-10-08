package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.interrupt.dungeoneer.editor.Editor;

public class Handles {
    private static Color pickColor = Color.WHITE;
    private static boolean picking = false;

    public static boolean isPicking() {
        return picking;
    }

    public static Color getPickColor() {
        return pickColor;
    }

    public static void setPickColor(Color color) {
        pickColor = color;
    }

    public static void pickingBegin() {
        picking = true;
    }

    public static void pickingEnd() {
        picking = false;
    }


    private static final Color color = new Color();
    private static Handle currentHoveredHandle = null;

    public static void pick() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Create pick buffer if needed.
        if (pickFrameBuffer == null || pickFrameBuffer.getWidth() != width || pickFrameBuffer.getHeight() != height) {
            createPickBuffer();
        }

        // Draw
        pickFrameBuffer.begin();

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);

        Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        Handles.pickingBegin();

        for (Handle handle : Handle.all) {
            if (!handle.getVisible()) continue;
            Color.rgb888ToColor(color, handle.getId());
            Handles.setPickColor(color);
            handle.draw();
        }

        Handles.pickingEnd();

        // Get mouse coords
        int pickX = Gdx.input.getX();
        int pickY = height - Gdx.input.getY();

        // Get the pixels
        Gdx.gl.glReadPixels(
            0,
            0,
            width,
            height,
            GL20.GL_RGBA,
            GL20.GL_UNSIGNED_BYTE,
            pickPixelBuffer.getPixels()
        );

        // Get the hovered pixel
        int rgba8888 = pickPixelBuffer.getPixel(pickX, pickY);
        color.set(rgba8888);

        // Get hovered Handle
        int index = Color.rgb888(color);

        Handle h = Handle.get(index);
        if (h != null) {
            Editor.app.hovered = h;
        }

        if (Editor.app.hovered instanceof Handle) {
            Handle hoveredHandle = (Handle) Editor.app.hovered;

            if (currentHoveredHandle != hoveredHandle) {
                if (currentHoveredHandle != null) {
                    currentHoveredHandle.exit();
                }

                hoveredHandle.enter();

                currentHoveredHandle = hoveredHandle;
                Editor.app.hovered = hoveredHandle;
            }
        }
        else if (currentHoveredHandle != null) {
            currentHoveredHandle.exit();
            currentHoveredHandle = null;
        }

        pickFrameBuffer.end();
    }

    private static FrameBuffer pickFrameBuffer = null;
    private static Pixmap pickPixelBuffer = null;

    private static void createPickBuffer() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        pickFrameBuffer = Editor.app.CreateFrameBuffer(
            pickFrameBuffer,
            width,
            height,
            true,
            true
        );

        if (pickPixelBuffer != null) {
            pickPixelBuffer.dispose();
        }

        pickPixelBuffer = new Pixmap(
            width,
            height,
            Pixmap.Format.RGBA8888
        );
    }
}
