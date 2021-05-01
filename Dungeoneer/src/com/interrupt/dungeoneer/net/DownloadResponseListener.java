package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.StreamUtils;
import com.interrupt.dungeoneer.game.Game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Listener for downloading a file to disk. */
public class DownloadResponseListener implements HttpResponseListener {
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

        long bytesTotal = Long.parseLong(httpResponse.getHeader("Content-Length"));
        byte[] buffer = new byte[8192];
        int bytesRead = -1;
        long bytesReceived = 0;

        try {
            Gdx.app.log("Network", "Download starting: " + filename);

            while ((bytesRead = input.read(buffer, 0, buffer.length)) != -1) {
                output.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;

                callback.progress(new DownloadProgress(bytesReceived, bytesTotal));
            }

            Gdx.app.log("Network", "Download complete: " + filename);
            callback.completed(file);
        }
        catch (IOException exception) {
            Gdx.app.error("Network", "Download failed.", exception);
            failed(exception);
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
