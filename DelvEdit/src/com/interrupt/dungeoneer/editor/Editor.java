package com.interrupt.dungeoneer.editor;

public class Editor {
    public static EditorApplication app;
    public static EditorOptions options;
    public static EditorActions actions;

	public Editor() {
		Editor.app = new EditorApplication();
		Editor.options = EditorOptions.fromLocalFiles();
		Editor.actions = new EditorActions();
	}

    public static void dispose() {
	    EditorOptions.toLocalFiles(options);
    }
}
