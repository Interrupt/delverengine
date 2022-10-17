package com.interrupt.dungeoneer.editor.modes;

import com.interrupt.dungeoneer.editor.EditorApplication;

public class ArchMode extends CarveMode {
    public ArchMode(EditorApplication inEditor) {
        super(inEditor);
        canCarve = false;
        canExtrude = false;
    }
}
