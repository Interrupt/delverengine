package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.files.FileHandle;

public interface DownloadListener {
    void completed(FileHandle file);

    void failed(Throwable t);

    void cancelled();
}
