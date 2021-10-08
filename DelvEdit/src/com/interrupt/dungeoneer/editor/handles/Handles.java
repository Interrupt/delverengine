package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public class Handles {
    public abstract class Handle extends InputAdapter {
        private final int id;

        private boolean hovered;
        private boolean selected;
        private boolean visible = false;


        public Handle() {
            id = handleIds++;
            handles.add(new WeakReference<>(this));
        }

        public int getId() {
            return id;
        }

        public boolean getHovered() {
            return hovered;
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        public boolean getSelected() {
            return this.selected;
        }

        public void setSelected(boolean selected) {
            if (this.selected != selected) {
                if (!selected) deselect();
                else select();
            }

            this.selected = selected;
        }

        public boolean getVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public void draw() {
            setVisible(true);
        };

        /** Called when cursor is moved over handle. */
        public void enter() {
            setHovered(true);
        }

        /** Called when cursor is moved out of handle. */
        public void exit() {
            setHovered(false);
        }

        /** Called when handle becomes selected. */
        public void select() {}

        /** Called when handle become deselected. */
        public void deselect() {}

        /** Called when handle is manipulated. */
        public void change() {}

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != 0) {
                setHovered(false);
            }

            setSelected(getHovered());

            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            setSelected(false);
            return false;
        }
    }
    private static int handleIds = 1;

    public static int getNewId() {
        return handleIds++;
    }

    private static final Array<WeakReference<Handle>> handles = new Array<>();

    /** Gets the handle object for the given id. */
    public static Handle get(int id) {
        for (WeakReference<Handle> ref : handles) {
            Handle handle = ref.get();
            if (handle == null) continue;
            if (handle.getId() == id) return handle;
        }

        return null;
    }

    public static Array<Handle> result = new Array<>();
    public static Iterable<Handle> all = new Iterable<Handle>() {
        @Override
        public Iterator<Handle> iterator() {
            result.clear();

            for (Iterator<WeakReference<Handle>> it = handles.iterator(); it.hasNext(); ) {
                WeakReference<Handle> ref = it.next();
                Handle handle = ref.get();

                // Clean up list if needed.
                if (handle == null) {
                    it.remove();
                    continue;
                }

                result.add(handle);
            }

            return result.iterator();
        }
    };

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

        pickingBegin();

        for (Handle handle : all) {
            if (!handle.getVisible()) continue;
            Color.rgb888ToColor(color, handle.getId());
            setPickColor(color);
            handle.draw();
        }

        pickingEnd();

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

        Handle h = get(index);
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

    static {
        createPickBuffer();
    }

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

        batch = null;
    }

    public static void reset() {
        for (Handle h : Handles.all) {
            h.setVisible(false);
        }
    }

    private static SpriteBatch batch;

    /** Draws pickFrameBuffer to screen. Useful for debugging purposes. It is
     * also helpful to set handleIds to 0xFF000 so they are easier to see. */
    public static void drawVisualization() {
        if (batch == null) {
            batch = new SpriteBatch();
        }

        batch.begin();
        batch.draw(
            pickFrameBuffer.getColorBufferTexture(),
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            0,
            0,
            1,
            1
        );
        batch.end();
    }
}
