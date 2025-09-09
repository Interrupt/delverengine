package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.files.FileHandle;

public class FileListItem {
    public FileHandle file;

    final String name;

    public FileListItem(FileHandle file) {
        this(file.name(), file);
    }

    public FileListItem(FileHandle file, boolean isDirectory) {
        this(file.name(), file, isDirectory);
    }

    public FileListItem(String name, FileHandle file){
        if(file.isDirectory()){
            name += "/";
        }
        this.name = name;
        this.file = file;
    }

    public FileListItem(String name, FileHandle file, boolean isDirectory){
        if(isDirectory){
            name += "/";
        }
        this.name = name;
        this.file = file;
    }

    @Override
    public String toString() {
        return name;
    }
}
