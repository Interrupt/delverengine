package com.interrupt.managers;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.TileData;

public class TileManager {

    public HashMap<String, HashMap<String, TileData>> tileData = new HashMap<String, HashMap<String, TileData>>();

    @Deprecated
	public HashMap<String, TileData> tiles = new HashMap<String, TileData>();

	private HashMap<String, IntMap<TileData>> parsedTiles = new HashMap<String, IntMap<TileData>>();
	
	public static TileManager instance;
	
	public static TileData DEFAULT_TILEDATA = new TileData();
	
	static {
		// Load tile data
		try {
            instance = Game.getModManager().loadTileManager();
            instance.init();
		}
		catch(Exception e)
		{
			instance = new TileManager();
            instance.init();
		}
	}
	
	private void init() {
        for(String key: tileData.keySet()) {
            for(Entry<String, TileData> data: tileData.get(key).entrySet()) {
                if(!parsedTiles.containsKey(key)) {
                    parsedTiles.put(key, new IntMap<TileData>());
                }

                parsedTiles.get(key).put(Integer.parseInt(data.getKey()), data.getValue());
            }
        }
	}

    public TileData getDataForTile(Tile tile) {
        IntMap<TileData> data = parsedTiles.get(tile.floorTexAtlas != null
                ? tile.floorTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey());
        if(data != null && data.containsKey(tile.floorTex)) {
            return data.get(tile.floorTex);
        }
        return DEFAULT_TILEDATA;
    }

    public TileData getDataForTileCeiling(Tile tile) {
        IntMap<TileData> data = parsedTiles.get(tile.ceilTexAtlas != null
                ? tile.ceilTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey());
        if(data != null && data.containsKey(tile.ceilTex)) {
            return data.get(tile.ceilTex);
        }
        return DEFAULT_TILEDATA;
    }

    public TileData getDataForTileWall(Tile tile) {
        IntMap<TileData> data = parsedTiles.get(tile.floorTexAtlas != null
                ? tile.floorTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey());
        if(data != null && data.containsKey(tile.wallTex)) {
            return data.get(tile.wallTex);
        }
        return DEFAULT_TILEDATA;
    }
	
	public boolean isWater(Tile tile) {
        TileData data = getDataForTile(tile);
        if(data != null) return data.isWater;
		return false;
	}

	public boolean isLava(Tile tile) {
        TileData data = getDataForTile(tile);
        if(data != null) return data.isLava;
		return false;
	}
	
	public Integer getFlowTexture(Tile tile) {
        TileData data = getDataForTile(tile);
        if(data != null && data.flowTex != null) return data.flowTex;
		
		return 26;
	}
	
	public Color getLightColor(Tile tile) {
        TileData data = getDataForTile(tile);
        if(data != null) return data.lightColor;
		return null;
	}
	
	public Color getMapColor(Tile tile) {
        TileData data = getDataForTile(tile);
        if(data != null) return data.mapColor;
		return null;
	}

	public boolean drawCeiling(Tile tile) {
        TileData data = getDataForTileCeiling(tile);
        if(data != null) return data.drawCeiling;
		return true;
	}
	
	public boolean drawWalls(Tile tile) {
        TileData data = getDataForTileWall(tile);
        if(data != null) return data.drawWalls;
		return true;
	}

	public int hurts(Tile tile) {
        TileData data = getDataForTile(tile);
        if(data != null) return data.hurts;
		return 0;
	}
}
