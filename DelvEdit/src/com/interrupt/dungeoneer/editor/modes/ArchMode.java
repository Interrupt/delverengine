package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;

public class ArchMode extends CarveMode {
    public ArchMode() {
        super(EditorModes.ARCH);
        canCarve = false;
        canExtrude = false;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = controlPointType == ControlPoint.ControlPointType.ceiling;
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // Get the arch heights
            float widthMod = (info.x + 0.5f - selection.x) / (float)selection.width;
            float heightMod = (info.y + 0.5f - selection.y) / (float)selection.height;

            widthMod = (float)Math.sin(widthMod * Math.PI);
            heightMod = (float)Math.sin(heightMod * Math.PI);

            // Pick our arch direction based on selection differences
            boolean xMode = selection.width > selection.height;
            float pickedArchMod = xMode ? widthMod : heightMod;

            if (isCeiling) {
                t.ceilHeight -= dragOffset.y * pickedArchMod;
            } else {
                t.floorHeight -= dragOffset.y * pickedArchMod;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }
}
