package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.utils.JsonUtil;

public class EditorClipboard {
    public Array<Entity> entities = new Array<>();
    public Array<TileSelectionInfo> tiles = new Array<>();

    private static EditorClipboard instance = null;

    /** Copies EditorClipboard object to system clipboard as JSON. */
    public static void copy() {
        instance = new EditorClipboard();

        // Copy entities
        if(Editor.selection.picked != null) {
            Entity picked = Editor.selection.picked;
            for (Entity e : Editor.selection.all) {
                Entity copy = copyEntity(e);
                copy.x -= (int) picked.x + 1;
                copy.y -= (int) picked.y + 1;
                copy.z -= - Editor.app.level.getTile((int)picked.x, (int)picked.y).getFloorHeight(0.5f, 0.5f);

                instance.entities.add(copy);
            }
        }

        // Copy tiles
        if(Editor.selection.picked == null) {
            for (TileSelectionInfo info : Editor.selection.tiles) {
                Tile t = info.tile;
                if (t != null) {
                    info.tile = Tile.copy(t);
                }

                // Calculate offset
                info.x -= Editor.selection.tiles.x;
                info.y -= Editor.selection.tiles.y;

                instance.tiles.add(info);
            }
        }

        // Serialize to system clipboard.
        Clipboard systemClipboard = Gdx.app.getClipboard();
        systemClipboard.setContents(JsonUtil.toJson(instance));
    }

    private static Entity copyEntity(Entity entity) {
        return JsonUtil.fromJson(entity.getClass(), JsonUtil.toJson(entity));
    }

    /** Serialize JSON from system clipboard and add to level. */
    public static void paste() {
        instance = null;

        // Deserialize from system clipboard.
        try {
            Clipboard systemClipboard = Gdx.app.getClipboard();
            instance = JsonUtil.fromJson(EditorClipboard.class, systemClipboard.getContents());
        }
        catch (Exception ignored) {}

        // Try deserializing a single entity.
        if (instance == null) {
            try {
                Clipboard systemClipboard = Gdx.app.getClipboard();
                Entity e = JsonUtil.fromJson(Entity.class, systemClipboard.getContents());
                instance = new EditorClipboard();

                if (e != null) {
                    instance.entities.add(e);
                }
            } catch (Exception e) {
                Gdx.app.log("Editor", e.getMessage());
            }
        }

        if (instance == null) {
            return;
        }

        int cursorTileX = Editor.selection.tiles.x;
        int cursorTileY = Editor.selection.tiles.y;

        // Paste tiles
        for (TileSelectionInfo info : instance.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t = Tile.copy(t);
            }

            int offsetX = info.x + cursorTileX;
            int offsetY = info.y + cursorTileY;

            Editor.app.level.setTile(offsetX, offsetY, t);
            Editor.app.markWorldAsDirty(offsetX, offsetY, 1);
        }

        // Paste entities
        for(Entity e : instance.entities) {
            Entity copy = copyEntity(e);
            copy.x += cursorTileX + 1;
            copy.y += cursorTileY + 1;

            Tile copyAt = Editor.app.level.getTileOrNull(cursorTileX, cursorTileY);
            if(copyAt != null) {
                copy.z = copyAt.getFloorHeight(copy.x, copy.y) + 0.5f;
            }

            Editor.app.addEntity(copy);
        }

        // Save undo history
        Editor.app.history.saveState(Editor.app.level);
    }
}
