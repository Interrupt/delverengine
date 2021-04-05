package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.utils.Array;

public class GitHubRepoModSource implements ModSource {
    /** The URL of the GitHub repository to search for mod content. */
    private String url;

    public GitHubRepoModSource() {}

    public GitHubRepoModSource(String url) {
        this.url = url;
    }

    @Override
    public Array<String> getInstalledMods() {
        return new Array<>();
    }
}
