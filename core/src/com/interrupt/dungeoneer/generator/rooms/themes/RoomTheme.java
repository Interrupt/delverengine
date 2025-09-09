package com.interrupt.dungeoneer.generator.rooms.themes;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;

public class RoomTheme {

    public enum RoomType { DUNGEON, CAVE, SEWER, TEMPLE };

    public Array<Material> walls = new Array<Material>();
    public Array<Material> lowerWalls = new Array<Material>();
    public Array<Material> floors = new Array<Material>();
    public Array<Material> ceilings = new Array<Material>();
    public Array<String> wallPrefabs = new Array<String>();
    public Array<String> floorPrefabs = new Array<String>();
    public Array<String> allowedSubrooms = new Array<String>();
    public boolean hazardsAllowed = true;
    public boolean filledCornersAllowed = true;
    public RoomType type = RoomType.DUNGEON;

    public float minHeight = 1f;
    public float maxHeight = 2.6f;

    public float markerDensity = 0.066f;
    public float monsterMarkerChance = 0.7f;
    public float decorMarkerChance = 0.1f;
    public float decorPileMarkerChance = 0.1f;
    public float lootMarkerChance = 0.1f;

    public float columnChance = 1f;
    public float crackedColumnChance = 0.15f;

    public boolean canLowerFloor = false;

    public Material pickWallMaterial(RoomGenerator generator) {
        if(walls.size == 0) return null;
        return walls.get(generator.random.nextInt(walls.size));
    }

    public Material pickLowerWallMaterial(RoomGenerator generator) {
        if(lowerWalls == null || lowerWalls.size == 0) return pickWallMaterial(generator);
        return lowerWalls.get(generator.random.nextInt(lowerWalls.size));
    }

    public Material pickFloorMaterial(RoomGenerator generator) {
        if(floors.size == 0) return null;
        return floors.get(generator.random.nextInt(floors.size));
    }

    public Material pickCeilingMaterial(RoomGenerator generator) {
        if(ceilings.size == 0) return null;
        return ceilings.get(generator.random.nextInt(ceilings.size));
    }

    public RoomPrefab pickFloorPrefab(RoomGeneratorTheme theme, RoomGenerator generator, int freeSpace, Tile atTile) {
        if(floorPrefabs.size == 0) return null;

        Array<RoomPrefab> possibles = new Array<RoomPrefab>();
        for(int i = 0; i < floorPrefabs.size; i++) {
            String s = floorPrefabs.get(i);
            RoomPrefab p = theme.getPrefab(s);
            if(p != null && p.space <= freeSpace && generator.random.nextFloat() <= p.chance) {
                float availableHeight = atTile.ceilHeight - atTile.floorHeight;
                if(availableHeight <= p.maxFreeHeight && availableHeight >= p.minFreeHeight)
                    possibles.add(p);
            }
        }

        if(possibles.size == 0) return null;
        return possibles.get(generator.random.nextInt(possibles.size));
    }

    public RoomPrefab pickWallPrefab(RoomGeneratorTheme theme, RoomGenerator generator, int freeSpace, Tile atTile) {
        if(wallPrefabs.size == 0) return null;

        Array<RoomPrefab> possibles = new Array<RoomPrefab>();
        for(int i = 0; i < wallPrefabs.size; i++) {
            String s = wallPrefabs.get(i);
            RoomPrefab p = theme.getPrefab(s);
            if(p != null && p.space <= freeSpace && generator.random.nextFloat() <= p.chance) {
                float availableHeight = atTile.ceilHeight - atTile.floorHeight;
                if(availableHeight <= p.maxFreeHeight && availableHeight >= p.minFreeHeight)
                    possibles.add(p);
            }
        }

        if(possibles.size == 0) return null;
        return possibles.get(generator.random.nextInt(possibles.size));
    }

    public float pickRoomHeight(RoomGenerator generator) {
        return minHeight + (int)(generator.random.nextFloat() * (maxHeight - minHeight) * 16) / 16f;
    }

    public RoomTheme() { }

    public RoomTheme(float minCeilingHeight) { this.minHeight = minCeilingHeight; }
}