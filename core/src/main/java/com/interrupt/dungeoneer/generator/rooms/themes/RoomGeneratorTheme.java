package com.interrupt.dungeoneer.generator.rooms.themes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.gfx.Material;

import java.util.HashMap;

public class RoomGeneratorTheme {

    public enum HallwayType { DUNGEON, SEWER, CAVE, TEMPLE };

    public enum LakeType { WATER, LAVA, PIT }
    public LakeType[] lakes = null;

    public Array<String> mainRooms = new Array<String>();
    public HashMap<String, RoomTheme> rooms = new HashMap<String, RoomTheme>();
    public HashMap<String, RoomPrefab> prefabs = new HashMap<String, RoomPrefab>();
    public HallwayType hallwayType = HallwayType.DUNGEON;
    public boolean adjustHeights = true;

    public Array<Entity> secretDoors = new Array<Entity>();

    public Material defaultMaterial = null;
    public Material defaultFloorMaterial = null;
    public Material defaultCeilingMaterial = null;

    public Material defaultHallwayFloorMaterial = null;
    public Material defaultHallwayWallMaterial = null;
    public Material defaultHallwayCeilMaterial = null;

    public Material columnMaterial = null;
    public Material columnBrokenMaterial = null;

    public RoomGeneratorTheme() { }

    public RoomTheme pickDefaultRoomTheme(RoomGenerator generator) {
        if(mainRooms.size == 0 || mainRooms == null) {
            return rooms.get("default");
        }
        else {
            return rooms.get(mainRooms.get(generator.random.nextInt(mainRooms.size)));
        }
    }

    public RoomTheme pickRoomTheme(RoomGenerator generator) {
        if(rooms == null) return null;
        Object[] vals = rooms.values().toArray();
        return (RoomTheme)vals[generator.random.nextInt(vals.length)];
    }

    public RoomTheme pickSubroomTheme(RoomGenerator generator, RoomTheme theme) {
        if(theme == null || theme.allowedSubrooms == null || theme.allowedSubrooms.size == 0)
            return pickRoomTheme(generator);

        String picked = theme.allowedSubrooms.get(generator.random.nextInt(theme.allowedSubrooms.size));
        return rooms.get(picked);
    }

    public RoomPrefab getPrefab(String prefabKey) {
        return prefabs.get(prefabKey);
    }
}