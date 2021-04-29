package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.StreamUtils;
import com.interrupt.dungeoneer.game.Game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Listener for downloading a file to disk. */
public class DownloadResponseListener extends HttpResponseListenerAdapter {
    private final String filename;
    private final DownloadListener callback;

    public DownloadResponseListener(String filename, DownloadListener callback) {
        this.filename = filename;
        this.callback = callback;
    }

    @Override
    public void handleHttpResponse(Net.HttpResponse httpResponse) {
        int statusCode = httpResponse.getStatus().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            return;
        }

        InputStream input = httpResponse.getResultAsStream();
        FileHandle file = Game.getFile(filename);
        OutputStream output = file.write(false);

        try {
            Gdx.app.log("Network", "Download starting: " + filename);
            StreamUtils.copyStream(input, output);
            Gdx.app.log("Network", "Download complete: " + filename);
        }
        catch (IOException exception) {
            Gdx.app.error("Network", "Download failed.", exception);
            failed(exception);
        }
        finally {
            StreamUtils.closeQuietly(input);
            callback.completed(file);
        }
    }

    @Override
    public void failed(Throwable t) {
        callback.failed(t);
    }

    @Override
    public void cancelled() {
        callback.cancelled();
    }
}
