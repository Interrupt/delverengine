package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
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

        // Apply a function across all of these vertices to set the height modifier
        // These steps are flattened to avoid cache misses, hopefully
        Array<Vector3> vertices = selection.getVertexLocations();
        for(int i = 0; i < vertices.size; i++) {
            Vector3 vert = vertices.get(i);

            // Offset the floor and ceiling noise by different amounts
            int noiseOffset = 400;
            if(isCeiling)
                noiseOffset += 75;

            // Perlin noise based randomness
            float noiseAmt = (float)perlinNoise.getHeight(
                vert.x * 0.05f + noiseOffset + selection.x,
                vert.y * 0.05f + noiseOffset + selection.y);

            noiseAmt /= perlinNoise.getAmplitude();
            noiseAmt += 1f;
            noiseAmt *= 0.5f;

            if(noiseAmt < 0)
                noiseAmt = 0;

            vert.z = noiseAmt * -dragOffset.y;
        }

        applyVertexHeightModifiers(selection, vertices, isCeiling, !isCeiling);
        packTileHeights(selection, isCeiling);

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
