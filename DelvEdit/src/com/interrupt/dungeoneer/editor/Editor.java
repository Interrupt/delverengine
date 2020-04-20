package com.interrupt.dungeoneer.editor;

public class Editor {
    public static EditorApplication app;
    public static EditorOptions options;
    public static EditorActions actions;

	public static void init() {
	    if (Editor.app != null) {
	        return;
        }

        Editor.app = new EditorApplication();
        Editor.options = EditorOptions.fromLocalFiles();
        Editor.actions = new EditorActions();
    }

    public static void dispose() {
	    EditorOptions.toLocalFiles(options);
    }
}
