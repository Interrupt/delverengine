package com.interrupt.dungeoneer.generator;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Door;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Stairs;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomGeneratorTheme;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

import java.util.HashMap;

public class GenTheme {
    /** Array of GenInfos. */
	public Array<GenInfo> genInfos = new Array<GenInfo>();

	/** Array of Doors. */
	public Array<Door> doors = new Array<Door>();

	/** Array of light Entities. */
	public Array<Entity> spawnLights = null;

	public Stairs exitUp = new Stairs(Stairs.StairDirection.up);
    public Stairs exitDown = new Stairs(Stairs.StairDirection.down);

    /** Array of decoration Entities. */
    public Array<Entity> decorations = null;

    /** Array of surprise Entities. */
	public Array<Entity> surprises = null;

	/** Name of default TextureAtlas. */
    public String defaultTextureAtlas = null;

    /** Size of a room.  */
    public Integer chunkTiles = null;

    /** Size of a map. */
    public Integer mapChunks = null;

    /** Map complexity heuristic. */
    public Integer mapComplexity = null;

    /** Array of valid lake types to generate. */
    public RoomGeneratorTheme.LakeType[] lakes = null;

    /** TexturePainter for theme. Deprecated. */
    @Deprecated
	public TexturePainter painter = null;

    /** Dictionary providing a mapping of TextureAtlas names to TexturePainters. */
    public HashMap<String, TexturePainter> texturePainters = null;

    public Byte getWallTexture(Byte originalTexture, String atlas) {
        if(atlas == null) atlas = TextureAtlas.cachedRepeatingAtlases.firstKey();
        if(originalTexture == null || texturePainters == null || !texturePainters.containsKey(atlas)) return originalTexture;
        return texturePainters.get(atlas).GetWallTexture(originalTexture);
    }

    public Byte getFloorTexture(Byte originalTexture, String atlas) {
        if(atlas == null) atlas = TextureAtlas.cachedRepeatingAtlases.firstKey();
        if(originalTexture == null || texturePainters == null || !texturePainters.containsKey(atlas)) return originalTexture;
        return texturePainters.get(atlas).GetFloorTexture(originalTexture);
    }

    public Byte getCeilingTexture(Byte originalTexture, String atlas) {
        if(atlas == null) atlas = TextureAtlas.cachedRepeatingAtlases.firstKey();
        if(originalTexture == null || texturePainters == null || !texturePainters.containsKey(atlas)) return originalTexture;
        return texturePainters.get(atlas).GetCeilingTexture(originalTexture);
    }

    public int getChunkTileSize() {
        return chunkTiles != null ? chunkTiles : 17;
    }

    public int getMapChunks() {
        return mapChunks != null ? mapChunks : 4;
    }

    public int getMapComplexity() {
        return mapComplexity != null ? mapComplexity : 4;
    }
}