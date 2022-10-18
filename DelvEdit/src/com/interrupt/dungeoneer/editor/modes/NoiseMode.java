package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;
import com.noise.PerlinNoise;

public class NoiseMode extends CarveMode {
    public NoiseMode() {
        super(EditorModes.NOISE);
        canCarve = false;
        canExtrude = false;
    }

    PerlinNoise perlinNoise = new PerlinNoise(1, 1, 0.5, 9, 8);

    @Override
    public void adjustTileHeights(Vector3 dragStart, Vector3 dragOffset, boolean isCeiling) {
        for (TileSelectionInfo info : tileSelection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // Perlin noise based randomness
            float noiseAmt = (float)perlinNoise.getHeight(
                info.x * 0.1f + 400 + tileSelection.x,
                info.y * 0.1f + 400 + tileSelection.y);

            noiseAmt = Math.abs(noiseAmt) * 0.1f;

            if (isCeiling) {
                t.ceilHeight -= dragOffset.y * noiseAmt;
            } else {
                t.floorHeight -= dragOffset.y * noiseAmt;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }
}
