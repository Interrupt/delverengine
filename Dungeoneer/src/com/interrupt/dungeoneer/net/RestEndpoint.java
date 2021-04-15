package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.interrupt.dungeoneer.game.Game;

public class RestEndpoint {
    public static final String USER_AGENT_STRING = "delverengine/" + Game.VERSION;

    private final String rootPath;
    private final String apiKey;

    private final transient HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

    public RestEndpoint(String rootPath, String apiKey) {
        this.rootPath = rootPath;
        this.apiKey = apiKey;
    }

    public void get(String path, HttpResponseListener listener) {
        HttpRequest request = requestBuilder
            .newRequest()
            .method(Net.HttpMethods.GET)
            .header("User-Agent", USER_AGENT_STRING)
            .url(rootPath + path)
            .content("api_key=" + apiKey)
            .build();

        Gdx.net.sendHttpRequest(request, listener);
    }
}
