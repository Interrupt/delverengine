package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.modding.modio.*;
import com.interrupt.dungeoneer.net.ObjectResponseListener;
import com.interrupt.dungeoneer.net.RestEndpoint;
import com.interrupt.utils.DownloadUtils;

public class ModIOModSource extends LocalFileSystemModSource {
    private int gameID;
    private String apiKey;

    private final String rootPath = "https://api.test.mod.io/v1";
    private transient RestEndpoint api;
    private String allModsRoute;

    @Override
    public void init() {
        api = new RestEndpoint(rootPath, apiKey);
        allModsRoute = "/games/" + gameID + "/mods";
        root = ".mods/modio/" + gameID;
        refresh();
    }

    public void refresh() {
        api.get(allModsRoute, new ObjectResponseListener<GetMods>(GetMods.class) {
            @Override
            public void handleObjectResponse(GetMods mods) {
                for (ModObject mod : mods.data) {
                    String url = mod.modfile.download.binary_url;
                    String filename = mod.modfile.filename;
                    String filepath = root + "/" + filename;

                    FileHandle f = Game.getFile(filepath);
                    if (!f.exists()) {
                        DownloadUtils.downloadFile(url, filepath);
                    }
                }
            }
        });
    }
}
