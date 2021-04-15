package com.interrupt.dungeoneer.net;

import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.net.HttpStatus;
import com.interrupt.utils.JsonUtil;

/** Listener for deserializing a JSON response. */
public class ObjectResponseListener<T> extends HttpResponseListenerAdapter {
    private Class<T> type;

    public ObjectResponseListener(Class<T> type) {
        this.type = type;
    }
    @Override
    public void handleHttpResponse(HttpResponse httpResponse) {
        int statusCode = httpResponse.getStatus().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            return;
        }

        String response = httpResponse.getResultAsString();
        T object = JsonUtil.fromJson(type, response);

        if (object != null) handleObjectResponse(object);
    }

    public void handleObjectResponse(T object) {}
}
