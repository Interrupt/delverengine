package com.interrupt.dungeoneer.editor;

import com.interrupt.dungeoneer.editor.selection.EditorSelection;

/** Main API entry point providing references to subsystems. */
public class Editor {
    public static EditorApplication app;
    public static EditorOptions options;
    public static EditorSelection selection;

    /** Initialize subsystems. */
	public static void init() {
	    if (Editor.app != null) {
	        return;
        }

        Editor.app = new EditorApplication();
        Editor.options = EditorOptions.fromLocalFiles();
        Editor.selection = new EditorSelection();
    }

    public static void dispose() {
	    Editor.options.dispose();
	    Editor.app.dispose();
    }
}
