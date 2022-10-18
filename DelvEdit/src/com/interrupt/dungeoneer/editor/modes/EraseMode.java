package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.tiles.Tile;

public class EraseMode extends DrawMode {
    public EraseMode() {
        super(EditorModes.ERASE);
    }

    @Override
    protected void applyTiles() {
        Level level = Editor.app.level;

        // Could be erasing more than one tile, so loop through the selection
        boolean didErase = false;
        for (TileSelectionInfo info : tileSelection) {
            // Only do something if this tile is empty
            Tile existing = level.getTileOrNull(info.x, info.y);
            if(existing == null || existing.blockMotion)
                continue;

            // Check to see if there is empty space around this tile
            Tile n = level.getTile(info.x, info.y - 1);
            Tile s = level.getTile(info.x, info.y + 1);
            Tile e = level.getTile(info.x - 1, info.y);
            Tile w = level.getTile(info.x + 1, info.y);

            // If there is no empty space, we can just set this tile to null
            if(n.blockMotion && s.blockMotion && e.blockMotion && w.blockMotion) {
                level.setTile(info.x, info.y, null);
            }
            else {
                Tile t = Tile.NewSolidTile();
                t.wallTex = (byte)Editor.app.pickedWallTexture;
                t.wallTexAtlas = Editor.app.pickedWallTextureAtlas;
                level.setTile(info.x, info.y, t);
            }

            didErase = true;
        }

        if(!didErase)
            return;

        // Now mark everything as dirty
        // FIXME: Just do this once, not per tile!
        for (TileSelectionInfo info : tileSelection) {
            Editor.app.markWorldAsDirty(info.x, info.y, 1);
        }
    }
}
