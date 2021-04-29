package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.modding.modio.*;
import com.interrupt.dungeoneer.net.DownloadListenerAdapter;
import com.interrupt.dungeoneer.net.ObjectResponseListener;
import com.interrupt.dungeoneer.net.RestEndpoint;
import com.interrupt.utils.DownloadUtils;
import com.interrupt.utils.ZipUtils;

public class ModIOModSource extends LocalFileSystemModSource {
    private int gameID;
    private String apiKey;

    private final String rootPath = "https://api.test.mod.io/v1";
    private transient RestEndpoint api;
    private transient String allModsRoute;
    private transient String downloadsPath;
    private transient String contentPath;

    @Override
    public void init() {
        api = new RestEndpoint(rootPath, apiKey);
        allModsRoute = "/games/" + gameID + "/mods";

        if (root == null || root.isEmpty()) {
            root = ".mods/modio/";
        }

        if (!root.endsWith("/")) root += "/";

        downloadsPath = root + ".downloads/";
        contentPath = root + "content/";

        refresh();
    }

    @Override
    protected FileHandle getRootHandle() {
        return Game.getFile(contentPath);
    }

    public void refresh() {
        api.get(allModsRoute, new ObjectResponseListener<GetMods>(GetMods.class) {
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
                                    FileHandle path = Game.getFile(contentPath + "/" + mod.id + "/");
                                    ZipUtils.extract(file, path);
                                }
                            }
                        );
                    }
                }
            }
        });
    }
}
