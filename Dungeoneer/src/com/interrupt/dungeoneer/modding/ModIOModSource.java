package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.modding.modio.*;
import com.interrupt.dungeoneer.net.ObjectResponseListener;
import com.interrupt.dungeoneer.net.RestEndpoint;
import com.interrupt.utils.DownloadUtils;

public class ModIOModSource implements ModSource {
    private int gameID;
    private String apiKey;

    private final String rootPath = "https://api.test.mod.io/v1";
    private transient RestEndpoint api;
    private String allModsRoute;

    @Override
    public void init() {
        api = new RestEndpoint(rootPath, apiKey);
        allModsRoute = "/games/" + gameID + "/mods";
    }

    @Override
    public Array<String> getInstalledMods() {
        api.get(allModsRoute, new ObjectResponseListener<GetMods>(GetMods.class) {
            @Override
            public void handleObjectResponse(GetMods mods) {
                for (ModObject mod : mods.data) {
                    Gdx.app.log("mod.io", mod.name);

                    String url = mod.modfile.download.binary_url;
                    String filename = mod.modfile.filename;

                    DownloadUtils.downloadFile(url, ".mods//" + filename);
                }
            }
        });

        return new Array<>();
    }
}
