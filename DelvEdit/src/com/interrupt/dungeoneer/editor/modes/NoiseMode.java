package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.tiles.Tile;
import com.noise.PerlinNoise;

public class NoiseMode extends CarveMode {
    public NoiseMode() {
        super(EditorModes.NOISE);
        canCarve = true;
        carveAutomatically = false;
        canExtrude = false;
        usePlanePicking = false;
        useCollisionTrianglePicking = true;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    PerlinNoise perlinNoise = new PerlinNoise(1, 1, 0.5, 1, 8);

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = controlPointType == ControlPoint.ControlPointType.ceiling;
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // Offset the floor and ceiling noise by different amounts
            int noiseOffset = 400;
            if(isCeiling)
                noiseOffset += 75;

            // Perlin noise based randomness
            float noiseAmt = (float)perlinNoise.getHeight(
                info.x * 0.1f + noiseOffset + selection.x,
                info.y * 0.1f + noiseOffset + selection.y);

            noiseAmt /= perlinNoise.getAmplitude();
            noiseAmt += 1f;
            noiseAmt *= 0.5f;

            if(noiseAmt < 0)
                noiseAmt = 0;

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

    @Override
    protected boolean canCarveTile(TileSelection selection, TileSelectionInfo info) {
        // Perlin noise based randomness
        float noiseAmt = (float)perlinNoise.getHeight(
            info.x * 0.1f + 400 + selection.x,
            info.y * 0.1f + 400 + selection.y);

        noiseAmt /= perlinNoise.getAmplitude();
        noiseAmt += 1f;
        noiseAmt *= 0.5f;

        // Only allow carving / deleting of high perlin noise areas
        if(noiseAmt > 0.7f)
            return true;

        return false;
    }
}
