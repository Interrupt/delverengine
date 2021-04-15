package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.StreamUtils;
import com.interrupt.dungeoneer.game.Game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Listener for downloading a file to disk. */
public class DownloadResponseListener extends HttpResponseListenerAdapter {
    private final String filename;

    public DownloadResponseListener(String filename) {
        this.filename = filename;
    }

    @Override
    public void handleHttpResponse(Net.HttpResponse httpResponse) {
        int statusCode = httpResponse.getStatus().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            return;
        }

        InputStream input = httpResponse.getResultAsStream();
        OutputStream output = Game.getFile(filename).write(false);

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
        }
    }
}
