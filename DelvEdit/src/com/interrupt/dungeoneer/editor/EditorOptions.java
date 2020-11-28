package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.utils.JsonUtil;

/** Editor options container class. */
public class EditorOptions {
    public Array<String> recentlyOpenedFiles;

    public EditorOptions() {
        recentlyOpenedFiles = new Array<>();
    }

    public static EditorOptions fromLocalFiles() {
	    FileHandle file = Game.getFile(getEditorOptionsFilePath());

        return JsonUtil.fromJson(EditorOptions.class, file, ()-> {
            EditorOptions eo = new EditorOptions();
            JsonUtil.toJson(eo, file);
            return eo;
        });
    }

    public static void toLocalFiles(EditorOptions instance) {
        FileHandle file = Game.getFile(getEditorOptionsFilePath());
        JsonUtil.toJson(instance, file);
    }

    public void save() {
        EditorOptions.toLocalFiles(this);
    }

    public void dispose() {
        Editor.options.save();
    }

    public static String getEditorOptionsFilePath() {
        return "save/editor.txt";
    }

    public void removeRecentlyOpenedFile(String path) {
        recentlyOpenedFiles.removeValue(path, false);
    }

    public void addRecentlyOpenedFile(String path) {
        removeRecentlyOpenedFile(path);
        recentlyOpenedFiles.insert(0, path);
    }
}
