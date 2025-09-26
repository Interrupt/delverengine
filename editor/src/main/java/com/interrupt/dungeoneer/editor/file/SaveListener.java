package com.interrupt.dungeoneer.editor.file;

public interface SaveListener {
    void onSave();
    void onCancel();
    void onDontSave();
}
