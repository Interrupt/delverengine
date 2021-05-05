package com.interrupt.dungeoneer.modding;

import java.util.HashMap;
import java.util.UUID;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.net.DownloadListenerAdapter;
import com.interrupt.utils.DownloadUtils;
import com.interrupt.utils.JsonUtil;
import com.interrupt.utils.ZipUtils;

public class GitHubRepoModSource extends LocalFileSystemModSource {
    private String username;
    private String repository;
    private boolean hasMultipleMods;

    private transient String downloadsPath;
    private transient String modsPath;

    @Override
    public void init() {
        if (root == null || root.isEmpty()) {
            root = ".mods/github/";
        }

        if (!root.endsWith("/"))
            root += "/";

        downloadsPath = root + ".downloads/";
        modsPath = root + "mods/";

        refresh();
    }

    @Override
    protected FileHandle getRootHandle() {
        return Game.getFile(modsPath);
    }

    public void refresh() {
        HashMap<String, String> header = new HashMap<>();
        header.put("Accept", "application/vnd.github.v3+json");

        String url = "https://api.github.com/repos/" + this.username + "/" + this.repository + "/zipball";
        String filename = this.username + "-" + this.repository + ".zip";
        String filepath = downloadsPath + filename;

        FileHandle file = Game.getFile(filepath);
        if (!file.exists()) {
            DownloadUtils.downloadFile(url, filepath, header, new DownloadListenerAdapter() {
                @Override
                public void completed(FileHandle file) {
                    // Extract contents to temporary path.
                    FileHandle path = Game.getFile(downloadsPath + "/" + username + "-" + repository + "/");
                    ZipUtils.extract(file, path);

                    // Move into actual repository root.
                    FileHandle[] innerRoot = path.list();
                    path = innerRoot[0];

                    if (hasMultipleMods) {
                        for (FileHandle root : path.list()) {
                            if (!root.isDirectory())
                                continue;

                            FileHandle modInfoPath = root.child("modInfo.json");
                            if (!modInfoPath.exists()) {
                                ModInfo modInfo = new ModInfo();
                                modInfo.id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                                modInfo.name = root.nameWithoutExtension();

                                JsonUtil.toJson(modInfo, modInfoPath);
                            }

                            FileHandle destination = new FileHandle(modsPath + "/" + username + "-" + repository + "-" + root.nameWithoutExtension() + "/");
                            destination.mkdirs();
                            for (FileHandle item : root.list()) {
                                item.copyTo(destination);
                            }
                        }
                    } else {
                        FileHandle modInfoPath = path.child("modInfo.json");
                        if (!modInfoPath.exists()) {
                            ModInfo modInfo = new ModInfo();
                            modInfo.id = 0;
                            modInfo.name = username + "-" + repository;

                            JsonUtil.toJson(modInfo, modInfoPath);
                        }

                        FileHandle destination = new FileHandle(modsPath + "/" + username + "-" + repository + "/");
                        destination.mkdirs();
                        for (FileHandle item : path.list()) {
                            item.copyTo(destination);
                        }
                    }
                }
            });
        }
    }
}
