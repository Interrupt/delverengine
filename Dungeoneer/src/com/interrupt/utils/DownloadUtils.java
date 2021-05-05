package com.interrupt.utils;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.net.DownloadListener;
import com.interrupt.dungeoneer.net.DownloadResponseListener;

public final class DownloadUtils {
    private DownloadUtils() {}

    public static void downloadFile(String url, String filename, Map<String, String> header, DownloadListener listener) {
        HttpRequestBuilder builder = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.GET)
            .url(url)
            .header("User-Agent", "delverengine/" + Game.VERSION)
            .timeout(2500);

            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }

        Net.HttpRequest request = builder.build();

        Gdx.net.sendHttpRequest(request, new DownloadResponseListener(filename, listener));
    }
}
