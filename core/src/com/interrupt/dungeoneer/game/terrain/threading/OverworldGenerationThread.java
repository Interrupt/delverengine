package com.interrupt.dungeoneer.game.terrain.threading;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.OverworldChunk;
import com.interrupt.dungeoneer.game.OverworldLevel;

public class OverworldGenerationThread implements Runnable {
    private OverworldLevel level = null;
    private OverworldChunk chunk = null;
    private boolean tesselateOnThread = true;

    Runnable afterRunnable = new Runnable() {
        @Override
        public void run() {
            level.madeNewChunkAt(chunk.xChunk, chunk.yChunk);
            chunk.init(level);
            level.updateStaticSpatialHash();
            level.updateLightSpatialHash();

            if(tesselateOnThread)
                chunk.TesselateOnThread(level, GameManager.renderer);
            else
                chunk.TesselateOnThisThread(level, GameManager.renderer);

            // FIXME: Do we still need this?
            GameManager.renderer.UpdateDirtyWorldChunks(level);
        }
    };

    public OverworldGenerationThread() { }

    public void init(OverworldChunk chunk, OverworldLevel level, boolean tesselateOnThread) {
        this.chunk = chunk;
        this.level = level;
        this.tesselateOnThread = tesselateOnThread;
    }

    @Override
    public void run() {
        if(chunk == null || level == null) return;

        chunk.tileToLoad = level.getLocationAt(chunk.xChunk, chunk.yChunk);

        chunk.Init(level);
        chunk.decorate(level);
        chunk.runGenInfo(level);
        chunk.doLighting(level);

        // post a Runnable to the rendering thread that processes the result
        Gdx.app.postRunnable(afterRunnable);
    }
}
