package com.interrupt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.net.DownloadListener;
import com.interrupt.dungeoneer.net.DownloadResponseListener;

public final class DownloadUtils {
    public static void downloadFile(String url, String filename, DownloadListener listener) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest request = builder
            .newRequest()
            .method(Net.HttpMethods.GET)
            .url(url)
            .header("User-Agent", "delverengine/" + Game.VERSION)
            .timeout(2500)
            .build();

        Gdx.net.sendHttpRequest(request, new DownloadResponseListener(filename, listener));
    }
}
