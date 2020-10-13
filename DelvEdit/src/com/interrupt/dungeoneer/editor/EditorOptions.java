package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/** Editor options container class. */
public class EditorOptions {
    public Array<String> recentlyOpenedFiles;

    public EditorOptions() {
        recentlyOpenedFiles = new Array<String>();
    }

    public static EditorOptions fromLocalFiles() {
        String path = "/save/editor.txt";
	    Json json = new Json();
	    FileHandle file = Gdx.files.local(path);

	    if (!file.exists()) {
	        file.writeString(json.toJson(new EditorOptions()), false);
        }

	    return json.fromJson(EditorOptions.class, file);
    }

    public static void toLocalFiles(EditorOptions instance) {
        String path = "/save/editor.txt";
        Json json = new Json();
        FileHandle file = Gdx.files.local(path);
        file.writeString(json.toJson(instance), false);
    }

    public void save() {
        EditorOptions.toLocalFiles(this);
    }

    public void dispose() {
        Editor.options.save();
    }
}
