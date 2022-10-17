package com.interrupt.dungeoneer.editor.modes;

import com.interrupt.dungeoneer.editor.EditorApplication;

public class EntityPickedMode extends EditorMode {
    public EntityPickedMode(EditorApplication inEditor) {
        super(inEditor);
    }

    @Override
    public void reset() {
        // Switch back to the carve mode when done here
        editor.setCurrentEditorMode(EditorModes.CARVE);
    }
}
