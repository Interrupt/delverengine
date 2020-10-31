package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class EditorWindow extends Window {
    public EditorWindow(String title, Skin skin) {
        super(title, skin);

        TextButton closeWindowButton = new TextButton("Close", this.getSkin());
        closeWindowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleCloseWindow();
            }
        });

        getTitleTable().padLeft(6f).padRight(6f).add(closeWindowButton);
    }

    /** Override this to implement custom guards. Returning false will abort closing the window. */
    protected boolean onCloseWindow() {
        return true;
    }

    /** Override this to implement custom guards. Returning false will abort expanding the window. */
    protected boolean onExpandWindow() {
        return true;
    }

    /** Override this to implement custom guards. Returning false will abort shrinking the window. */
    protected boolean onShrinkWindow() {
        return true;
    }

    /** Override this to implement custom guards. Returning false will abort dragging the window. */
    protected boolean onDragWindow() {
        return true;
    }

    /** Override this to implement custom guards. Returning false will abort resizing the window. */
    protected boolean onResizeWindow() {
        return true;
    }

    private void handleCloseWindow() {
        if (this.onCloseWindow()) {
            hide();
        }
    }

    private void hide() {}
}
