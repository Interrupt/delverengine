package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.files.FileHandle;

public class DownloadListenerAdapter implements DownloadListener {
    @Override
    public void completed(FileHandle file) {}

    @Override
    public void failed(Throwable t) {}

    @Override
    public void cancelled() {}
}
