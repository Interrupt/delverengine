package com.interrupt.dungeoneer.editor;

import com.interrupt.dungeoneer.editor.selection.EditorSelection;

public class Editor {
    public static EditorApplication app;
    public static EditorOptions options;
    public static EditorActions actions;
    public static EditorSelection selection;

	public static void init() {
	    if (Editor.app != null) {
	        return;
        }

        Editor.app = new EditorApplication();
        Editor.options = EditorOptions.fromLocalFiles();
        Editor.actions = new EditorActions();
        Editor.selection = new EditorSelection();
    }

    public static void dispose() {
	    EditorOptions.toLocalFiles(options);
    }
}
