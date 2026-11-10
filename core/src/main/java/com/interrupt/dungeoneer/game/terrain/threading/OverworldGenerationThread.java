package com.interrupt.dungeoneer.game.terrain.threading;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.OverworldChunk;
import com.interrupt.dungeoneer.game.OverworldLevel;

public class OverworldGenerationThread implements Runnable {
    private OverworldLevel level = null;
    private OverworldChunk chunk = null;

    Runnable afterRunnable = new Runnable() {
        @Override
        public void run() {
            level.madeNewChunkAt(chunk.xChunk, chunk.yChunk);
            chunk.init(level);
            level.updateStaticSpatialHash();
            level.updateLightSpatialHash();

            // generation runs on the main thread (entity model loading is GL-bound,
            // issue #2), so tessellate on this thread; the mesh build is posted to
            // the render thread inside TesselationThreadRunnable.
            chunk.TesselateOnThisThread(level, GameManager.renderer);

            // FIXME: Do we still need this?
            GameManager.renderer.UpdateDirtyWorldChunks(level);
        }
    };

    public OverworldGenerationThread() { }

    public void init(OverworldChunk chunk, OverworldLevel level) {
        this.chunk = chunk;
        this.level = level;
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
