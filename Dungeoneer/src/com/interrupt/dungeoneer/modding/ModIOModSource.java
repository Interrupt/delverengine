package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.modding.modio.*;
import com.interrupt.dungeoneer.net.DownloadListenerAdapter;
import com.interrupt.dungeoneer.net.ObjectResponseListener;
import com.interrupt.utils.DownloadUtils;
import com.interrupt.utils.JsonUtil;
import com.interrupt.utils.ZipUtils;

public class ModIOModSource extends LocalFileSystemModSource {
    private int gameID;
    private String apiKey;

    private transient String downloadsPath;
    private transient String modsPath;

    @Override
    public void init() {
        if (root == null || root.isEmpty()) {
            root = ".mods/modio/";
        }

        if (!root.endsWith("/")) root += "/";

        downloadsPath = root + ".downloads/";
        modsPath = root + "mods/";

        refresh();
    }

    @Override
    protected FileHandle getRootHandle() {
        return Game.getFile(modsPath);
    }

    public void refresh() {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        HttpRequest request = requestBuilder
            .newRequest()
            .method(Net.HttpMethods.GET)
            .header("User-Agent", "delverengine/" + Game.VERSION)
            .url("https://api.test.mod.io/v1/games/" + gameID + "/mods")
            .content("api_key=" + apiKey)
            .build();

        Gdx.net.sendHttpRequest(request, new ObjectResponseListener<GetMods>(GetMods.class) {
            @Override
            public void handleObjectResponse(GetMods mods) {
                for (ModObject mod : mods.data) {
                    String url = mod.modfile.download.binary_url;
                    String filename = mod.modfile.filename;
                    String filepath = downloadsPath + filename;

                    FileHandle file = Game.getFile(filepath);
                    if (!file.exists()) {
                        DownloadUtils.downloadFile(
                            url,
                            filepath,
                            new DownloadListenerAdapter() {
                                @Override
                                public void completed(FileHandle file) {
                                    FileHandle path = Game.getFile(modsPath + "/" + mod.id + "/");
                                    ZipUtils.extract(file, path);

                                    FileHandle modInfoPath = path.child("modInfo.json");
                                    if (!modInfoPath.exists()) {
                                        ModInfo modInfo = new ModInfo();
                                        modInfo.id = mod.id;
                                        modInfo.name = mod.name;

                                        JsonUtil.toJson(modInfo, modInfoPath);
                                    }
                                }
                            }
                        );
                    }
                }
            }
        });
    }
}
