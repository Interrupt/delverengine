package com.interrupt.dungeoneer.generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Group;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.halls.CaveHallway;
import com.interrupt.dungeoneer.generator.halls.Hallway;
import com.interrupt.dungeoneer.generator.halls.SewerHallway;
import com.interrupt.dungeoneer.generator.halls.TempleHallway;
import com.interrupt.dungeoneer.generator.rooms.*;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomGeneratorTheme;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomPrefab;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;
import com.interrupt.utils.JsonUtil;
import com.noise.PerlinNoise;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RoomGenerator {

    public Level level;
    private Room room;
    private Array<Hallway> exitHallways = new Array<Hallway>();
    public Random random = new Random();

    private Array<Vector2> avoidDecorating = new Array<Vector2>();

    Hallway northHallway = null;
    Hallway southHallway = null;
    Hallway eastHallway = null;
    Hallway westHallway = null;

    Array<Vector2> hallStarts = new Array<Vector2>();

    Array<Room> allRooms = new Array<Room>();

    Array<RoomPrefab> placedPrefabs = new Array<RoomPrefab>();

    private IntMap<Boolean> lockedTiles = new IntMap<Boolean>();

    private transient PerlinNoise perlinNoise = new PerlinNoise(1, 1f, 2f, 1f, 1);

    public RoomGeneratorTheme theme = new RoomGeneratorTheme();

    public RoomGenerator(Level level, String roomGeneratorType) {
        this.level = level;
        loadThemeFor(roomGeneratorType);
    }

    public void loadThemeFor(String roomGeneratorType) {
        try {
            theme = JsonUtil.fromJson(RoomGeneratorTheme.class, Game.getModManager().findFile("data/room-builders/" + roomGeneratorType.toLowerCase() + ".dat"));
        }
        catch(Exception ex) {
            // going to be really boring!
            theme = new RoomGeneratorTheme();
        }
    }

    public Room getRoom() { return room; }

    public void generate() {
        generate(true, true, true, true);
    }

    public void resetLevel() {
        if(level == null)
            return;

        // Make sure we are starting fresh
        if(level.entities != null)
            level.entities.clear();

        if(level.static_entities != null)
            level.static_entities.clear();

        if(level.non_collidable_entities != null)
            level.non_collidable_entities.clear();

        if(level.editorMarkers != null)
            level.editorMarkers.clear();

        for (int x = 0; x < level.width; x++) {
            for (int y = 0; y < level.height; y++) {
                level.setTile(x, y, null);
            }
        }
    }

    public boolean generate(boolean northExit, boolean southExit, boolean eastExit, boolean westExit) {
        try {
            makeRoom(4 + random.nextInt(level.width - 8), 4 + random.nextInt(level.height - 8));
            makeExits(northExit, southExit, eastExit, westExit);

            if(theme.lakes != null && theme.lakes.length > 0) {
                LakeGenerator lakes = new LakeGenerator(theme.lakes, theme);
                //lakes.makeLakes(level, random.nextInt(5));
            }

            // decorate rooms
            for(int i = 0; i < allRooms.size; i++) {
                Room r = allRooms.get(i);
                decorateRoom(r);
            }

            // paint some stuff if needed
            if(theme.defaultMaterial != null) {
                for (int x = 0; x <= level.width; x++) {
                    for (int y = 0; y <= level.height; y++) {
                        Tile t = level.getTileOrNull(x, y);
                        if ((t == null || (t.renderSolid && t.wallTex == 0 && t.wallTexAtlas == null)) && hasAdjacentOpenSpace(x, y)) {
                            t = new Tile();
                            t.copy(Tile.solidWall);
                            t.wallTex = theme.defaultMaterial.tex;
                            t.wallTexAtlas = theme.defaultMaterial.texAtlas;
                            t.blockMotion = true;
                            t.renderSolid = true;
                            level.setTile(x, y, t);
                        }
                    }
                }
            }

            room.done(this);

            if(!isNavigatableLevel()) {
                Gdx.app.log("RoomGenerator", "Made non-navigatable level!");
                return false;
            }
        }
        catch(Exception ex) {
            Gdx.app.error("RoomGenerator", ex.getMessage());
            return false;
        }

        return true;
    }

    private boolean hasAdjacentOpenSpace(int x, int y) {
        if(!tileIsSolid(getTile(x + 1, y))) return true;
        if(!tileIsSolid(getTile(x - 1, y))) return true;
        if(!tileIsSolid(getTile(x, y + 1))) return true;
        if(!tileIsSolid(getTile(x, y - 1))) return true;
        return false;
    }

    private void makeExits(boolean north, boolean south, boolean east, boolean west) {
        Vector2 defaultHallwayPos = new Vector2(room.x + room.width / 2, room.y + room.height / 2);

        float northHeight = -0.5f;
        float southHeight = -0.5f;
        float westHeight = -0.5f;
        float eastHeight = -0.5f;

        if(north) {
            Array<Vector2> possiblePositions = findPossibleDoorPositions(room, true, false, false, false, false, true);
            if(possiblePositions.size == 0) possiblePositions.add(defaultHallwayPos);

            Vector2 pickedEndPos = possiblePositions.get(random.nextInt(possiblePositions.size));
            northHallway = createHallway(
                    true,
                    new Vector2(level.width / 2, 0),
                    new Vector2(pickedEndPos),
                    new Vector2(-0.5f, room.floorHeight));

            exitHallways.add(northHallway);
            hallStarts.add(pickedEndPos);

            Tile t = level.getTile((int)pickedEndPos.x, (int)pickedEndPos.y);
            northHeight = t.isWater() ? room.floorHeight : t.floorHeight;
        }
        if(south) {
            Array<Vector2> possiblePositions = findPossibleDoorPositions(room, false, true, false, false, false, true);
            if(possiblePositions.size == 0) possiblePositions.add(defaultHallwayPos);

            Vector2 pickedEndPos = possiblePositions.get(random.nextInt(possiblePositions.size));
            southHallway = createHallway(
                    true,
                    new Vector2(pickedEndPos),
                    new Vector2(level.width / 2, level.height - 1),
                    new Vector2(room.floorHeight, -0.5f));

            exitHallways.add(southHallway);
            hallStarts.add(pickedEndPos);

            Tile t = level.getTile((int)pickedEndPos.x, (int)pickedEndPos.y);
            southHeight = t.isWater() ? room.floorHeight : t.floorHeight;
        }
        if(east) {
            Array<Vector2> possiblePositions = findPossibleDoorPositions(room, false, false, true, false, false, true);
            if(possiblePositions.size == 0) possiblePositions.add(defaultHallwayPos);

            Vector2 pickedEndPos = possiblePositions.get(random.nextInt(possiblePositions.size));
            eastHallway = createHallway(
                    false,
                    new Vector2(pickedEndPos),
                    new Vector2(level.width - 1, level.height / 2),
                    new Vector2(room.floorHeight, -0.5f));

            exitHallways.add(eastHallway);
            hallStarts.add(pickedEndPos);

            Tile t = level.getTile((int)pickedEndPos.x, (int)pickedEndPos.y);
            eastHeight = t.isWater() ? room.floorHeight : t.floorHeight;
        }
        if(west) {
            Array<Vector2> possiblePositions = findPossibleDoorPositions(room, false, false, false, true, false, true);
            if(possiblePositions.size == 0) possiblePositions.add(defaultHallwayPos);

            Vector2 pickedEndPos = possiblePositions.get(random.nextInt(possiblePositions.size));
            westHallway = createHallway(
                    false,
                    new Vector2(0, level.height / 2),
                    new Vector2(pickedEndPos),
                    new Vector2(-0.5f, room.floorHeight));

            exitHallways.add(westHallway);
            hallStarts.add(pickedEndPos);

            Tile t = level.getTile((int)pickedEndPos.x, (int)pickedEndPos.y);
            westHeight = t.isWater() ? room.floorHeight : t.floorHeight;
        }

        float minHallwayHeight = Math.min(northHeight, southHeight);
        minHallwayHeight = Math.min(minHallwayHeight, eastHeight);
        minHallwayHeight = Math.min(minHallwayHeight, westHeight);

        // find out how long the shortest hallway is
        int minHallLength = Integer.MAX_VALUE;
        for(Hallway hall : exitHallways) {
            int length = hall.getLength();
            if(minHallLength > length) minHallLength = length;
        }

        // using the minimum hallway size, offset the room a bit
        if(theme.adjustHeights) {
            float mod = random.nextBoolean() ? 1 : -1;
            float offsetAmount = (minHallLength * 0.2f) * mod;
            offsetAmount += (minHallwayHeight + 0.5f) * mod;

            room.transformHeightPosition(offsetAmount);

            // offset the level by that amount
            for (int x = 0; x < level.width; x++) {
                for (int y = 0; y < level.height; y++) {
                    Tile t = level.getTileOrNull(x, y);
                    if (t != null && !tileIsSolid(t)) {
                        t.floorHeight += offsetAmount;
                        t.ceilHeight += offsetAmount;
                    }
                }
            }

            // offset the entities as well
            for (Entity e : level.entities) {
                e.z += offsetAmount;
            }

            // offset the hallways
            northHeight += offsetAmount;
            southHeight += offsetAmount;
            eastHeight += offsetAmount;
            westHeight += offsetAmount;
        }

        if(room.theme.type == RoomTheme.RoomType.CAVE) {
            northHeight = -0.5f;
            southHeight = -0.5f;
            westHeight = -0.5f;
            eastHeight = -0.5f;
        }

        // now setup the new hallway start and end heights
        if(northHallway != null) northHallway.setHeight(new Vector2(-0.5f, northHeight));
        if(southHallway != null) southHallway.setHeight(new Vector2(southHeight, -0.5f));
        if(eastHallway != null) eastHallway.setHeight(new Vector2(eastHeight, -0.5f));
        if(westHallway != null) westHallway.setHeight(new Vector2(-0.5f, westHeight));

        for(Hallway hall : exitHallways) {
            hall.carve(this);
        }

        for(Hallway hall : exitHallways) {
            hall.finalize(this);
        }

        // place some exit torches to light the way
        for(Vector2 pos : hallStarts) {
            if(hasWallNearby((int)pos.x, (int)pos.y))
                level.editorMarkers.add(new EditorMarker(GenInfo.Markers.torch, (int)pos.x, (int)pos.y));
        }
    }

    public Hallway createHallway(boolean vertical, Vector2 start, Vector2 end, Vector2 heights) {
        Hallway hallway = null;

        if(theme.hallwayType == RoomGeneratorTheme.HallwayType.CAVE) {
            hallway = new CaveHallway(vertical, start, end, heights);
        }
        else if(theme.hallwayType == RoomGeneratorTheme.HallwayType.SEWER) {
            hallway = new SewerHallway(vertical, start, end, heights);
        }
        else if(theme.hallwayType == RoomGeneratorTheme.HallwayType.TEMPLE) {
            hallway = new TempleHallway(vertical, start, end, heights);
        }
        else {
            hallway = new Hallway(vertical, start, end, heights);
        }

        return hallway;
    }

    public void makeRoom(int width, int height) {

        // make sure we start fresh
        resetLevel();

        int startX = (level.width - width) / 2;
        int startY = (level.height - height) / 2;

        if(width < level.width - 6) startX += random.nextInt(level.width - width - 4) - (level.width - width - 4) / 2;
        if(height < level.height - 6) startY += random.nextInt(level.height - height - 4) - (level.height - height - 4)/ 2;

        RoomTheme defaultRoomTheme = pickDefaultRoomTheme();

        // Main room type should be able to change sometimes
        if(defaultRoomTheme.type == RoomTheme.RoomType.CAVE) {
            room = new CaveRoom(startX, startY, width, height, random, null, defaultRoomTheme);
            room.canContainRooms = false;
        }
        else if(defaultRoomTheme.type == RoomTheme.RoomType.SEWER) {
            room = new SewerRoom(startX, startY, width, height, random, null, defaultRoomTheme);
        }
        else if(defaultRoomTheme.type == RoomTheme.RoomType.TEMPLE) {
            room = new TempleRoom(startX, startY, width , height, random, null, defaultRoomTheme);
        }
        else {
            room = new EmptyRoom(startX, startY, width, height, random, null, defaultRoomTheme);
        }

        carve(room);

        for(int i = 0; i < 20; i++) {
            Room subroom = makeSubroomIn(room);

            if(subroom != null && room.hasRoomFor(subroom) && room.canContainRooms) {
                carve(subroom);
                room.subrooms.add(subroom);

                // Temples only get one subroom
                if(room.theme.type == RoomTheme.RoomType.TEMPLE) room.canContainRooms = false;

                for(int ii = 0; ii < 3; ii++) {
                    Room ss = makeSubroomIn(subroom);
                    if(ss != null && subroom.canContainRooms && subroom.hasRoomFor(ss)) {
                        carve(ss);
                        carvedoors(ss);
                        ss.finalize(this);
                        allRooms.add(ss);
                        subroom.subrooms.add(ss);
                    }
                }
                carvedoors(subroom);
                subroom.finalize(this);
                allRooms.add(subroom);
            }
        }

        room.finalize(this);
        allRooms.add(room);
    }

    public Room makeSubroomIn(Room room) {

        if(room.height <= 3 || room.width <= 3 || room.canContainRooms == false) return null;

        boolean westWall = random.nextBoolean();
        boolean northWall = random.nextBoolean();

        int roomWidth = random.nextInt(room.width - 3) + 2;
        int roomHeight = random.nextInt(room.height - 3) + 2;

        Room subroom = pickSubroom(westWall ? room.x : room.x + room.width - roomWidth,
                northWall ? room.y : room.y + room.height - roomHeight,
                roomWidth,
                roomHeight,
                random,
                room);

        if(subroom.canOffset) {
            int xOffset = 0;
            int yOffset = 0;

            if(westWall) xOffset -= random.nextInt(2);
            else xOffset += random.nextInt(2);

            if(northWall) yOffset -= random.nextInt(2);
            else yOffset += random.nextInt(2);

            subroom.x += xOffset;
            subroom.y += yOffset;
        }

        subroom.attachedNorth = northWall;
        subroom.attachedWest = westWall;

        subroom.eastDoor = westWall;
        subroom.westDoor = !westWall;
        subroom.northDoor = !northWall;
        subroom.southDoor = northWall;

        return subroom;
    }

    public Room pickSubroom(int x, int y, int roomWidth, int roomHeight, Random random, Room inRoom) {
        if(room.theme != null) {
            if(room.theme.type == RoomTheme.RoomType.SEWER) {
                return new FilledRoom(x, y, roomWidth, roomHeight, random, inRoom);
            }
            if(room.theme.type == RoomTheme.RoomType.TEMPLE) {
                return new FilledRoom(x, y, roomWidth, roomHeight, random, inRoom);
            }
        }

        if(inRoom.theme != null && random.nextFloat() < 0.35f) {
            if (random.nextFloat() < 0.5f && inRoom.theme.hazardsAllowed) {
                return new HazardRoom(x, y, roomWidth, roomHeight, random, inRoom);
            } else if (inRoom.theme.filledCornersAllowed) {
                return new FilledRoom(x, y, roomWidth, roomHeight, random, inRoom);
            }
        }

        return new WalledRoom(x, y, roomWidth, roomHeight, random, inRoom, pickRoomTheme(inRoom));
    }

    private RoomTheme pickRoomTheme(Room parentRoom) {
        if(theme == null) return null;
        if(parentRoom != null) {
            theme.pickSubroomTheme(this, parentRoom.theme);
        }
        return theme.pickRoomTheme(this);
    }

    private RoomTheme pickDefaultRoomTheme() {
        if(theme != null) {
            return theme.pickDefaultRoomTheme(this);
        }
        return null;
    }

    public void carve(Room room) {
        room.fill(this);
     }

    public void decorateRoom(Room room) {
        room.decorate(this);

        if(room.canDecorate) {
            Array<Vector2> walls = new Array<Vector2>();
            Array<Vector2> floors = new Array<Vector2>();

            for (int x = room.x; x < room.width + room.x; x++) {
                for (int y = room.y; y < room.height + room.y; y++) {
                    if (!room.overlapsSubrooms(x, y)) {
                        Tile center = level.getTileOrNull(x, y);
                        Tile north = level.getTileOrNull(x, y - 1);
                        Tile east = level.getTileOrNull(x + 1, y);
                        Tile south = level.getTileOrNull(x, y + 1);
                        Tile west = level.getTileOrNull(x - 1, y);

                        if (center != null && tileIsEmpty(center) && !tileHurts(center) && !tileIsLiquid(center)) {
                            Vector2 thisPosition = new Vector2(x, y);
                            floors.add(thisPosition);
                            if (tileIsSolid(north) || tileIsSolid(east) || tileIsSolid(south) || tileIsSolid(west)) walls.add(thisPosition);
                        }
                    }
                }
            }

            // Remove doors
            walls.removeAll(avoidDecorating, false);
            floors.removeAll(avoidDecorating, false);

            // make lights
            if(walls.size > 0) {
                int lightNum = (walls.size / 10) + random.nextInt(1);
                if(walls.size < 10) lightNum = (walls.size / 2) + random.nextInt(1);

                for (int i = 0; i < lightNum; i++) {
                    if (walls.size > 0) {
                        Vector2 pickedPos = walls.get(random.nextInt(walls.size));
                        walls.removeValue(pickedPos, true);
                        floors.removeValue(pickedPos, true);
                        level.editorMarkers.add(new EditorMarker(GenInfo.Markers.torch, (int) pickedPos.x, (int) pickedPos.y));
                    }
                }
            }

            makeMarkersForRoom(room, floors, walls);

            // make columns in bigger rooms
            if(room.width >= 4 || room.height >= 4) {
                int columnDistance = 1 + random.nextInt(3);
                float columnChance = ((float)columnDistance / 3f) * (room.theme == null ? 1f : room.theme.columnChance);
                for (Vector2 p : floors) {
                    int free = freeTileRadiusAround((int) p.x, (int) p.y);
                    if (free >= columnDistance && random.nextFloat() < columnChance) {
                        makeColumn((int) p.x, (int) p.y, room);
                    }
                }
            }

            // make wall prefabs
            for (int i = 0; i < walls.size; i++) {
                Vector2 pickedPos = walls.get(i);
                if(room.theme != null && theme != null) {
                    RoomPrefab roomPrefab = room.theme.pickWallPrefab(theme, this, freeSpaceAroundWall((int) pickedPos.x, (int) pickedPos.y), level.getTile((int)pickedPos.x, (int)pickedPos.y));

                    if (roomPrefab != null) {
                        Entity placed = placePrefab(roomPrefab, pickedPos);
                        if(placed != null) {
                            if(placed instanceof Group) {
                                placed.updateDrawable();
                                ((Group) placed).updateCollision();
                            }
                            level.decorateWallWith(placed, true, true);
                        }
                    }
                }
            }

            // make floor prefabs
            for (int i = 0; i < floors.size; i++) {
                Vector2 pickedPos = floors.get(i);
                if(room.theme != null && theme != null) {
                    RoomPrefab roomPrefab = room.theme.pickFloorPrefab(theme, this, freeTileAndPrefabRadiusAround((int)pickedPos.x, (int)pickedPos.y), level.getTile((int)pickedPos.x, (int)pickedPos.y));
                    if (roomPrefab != null) {
                        Entity placed = placePrefab(roomPrefab, pickedPos);
                        if(placed != null) {
                            // randomly rotate floor prefabs
                            for(int ri = 0; ri < random.nextInt(4); ri++) {
                                placed.rotate90();
                            }
                        }
                    }
                }
            }
        }

        // cull markers
        cullSomeMarkers();
    }

    private void cullSomeMarkers() {
        for(int i = 0; i < level.editorMarkers.size; i++) {
            EditorMarker m = level.editorMarkers.get(i);
            if(m.type == GenInfo.Markers.torch) {
                for(int ii = 0; ii < level.editorMarkers.size; ii++) {
                    EditorMarker mm = level.editorMarkers.get(ii);
                    if(m != mm && mm.type == GenInfo.Markers.torch) {
                        int distX = Math.abs(m.x - mm.x);
                        int distY = Math.abs(m.y - mm.y);
                        if(distX <= 1 && distY <= 1) {
                            level.editorMarkers.removeValue(mm, true);
                        }
                    }
                }
            }
        }
    }

    private void makeMarkersForRoom(Room room, Array<Vector2> floors, Array<Vector2> walls) {

        RoomTheme theme = room.theme;
        if(theme == null) theme = new RoomTheme();

        float weightTotal = 0;
        HashMap<GenInfo.Markers, Float> markerWeights = new HashMap<GenInfo.Markers, Float>();

        boolean isMonsterCloset = false;
        if(room.width < 5 && room.height < 5) {
            isMonsterCloset = random.nextFloat() > 0.9f;
        }

        if(theme.monsterMarkerChance > 0) {
            if(!isMonsterCloset)
                markerWeights.put(GenInfo.Markers.monster, weightTotal += theme.monsterMarkerChance);
            else
                markerWeights.put(GenInfo.Markers.monster, weightTotal += 4f);
        }
        if(theme.lootMarkerChance > 0)
            markerWeights.put(GenInfo.Markers.loot, weightTotal += theme.lootMarkerChance);
        if(theme.decorMarkerChance > 0)
            markerWeights.put(GenInfo.Markers.decor, weightTotal += theme.decorMarkerChance);
        if(theme.decorPileMarkerChance > 0)
            markerWeights.put(GenInfo.Markers.decorPile, weightTotal += theme.decorPileMarkerChance);

        float markerDensity = theme.markerDensity;
        if(isMonsterCloset) markerDensity = 0.7f;

        int numToMake = (int)(floors.size * markerDensity);
        for (int i = 0; i <= numToMake; i++) {
            if (floors.size > 0) {
                Vector2 pickedPos = floors.get(random.nextInt(floors.size));

                GenInfo.Markers markerType = null;
                float r = random.nextFloat() * weightTotal;
                float pickedChance = weightTotal;

                for(Map.Entry<GenInfo.Markers, Float> set : markerWeights.entrySet()) {
                    if(set.getValue() >= r && set.getValue() <= pickedChance) {
                        markerType = set.getKey();
                        pickedChance = set.getValue();
                    }
                }

                if(markerType != null) {
                    level.editorMarkers.add(new EditorMarker(markerType, (int) pickedPos.x, (int) pickedPos.y));
                    floors.removeValue(pickedPos, true);
                    walls.removeValue(pickedPos, true);
                }
            }
        }
    }

    private Entity placePrefab(RoomPrefab roomPrefab, Vector2 pickedPos) {
        if (roomPrefab != null) {
            Tile t = level.getTileOrNull((int) pickedPos.x, (int) pickedPos.y);
            if (t != null && tileIsEmpty(t)) {
                Prefab prefab = new Prefab(roomPrefab.group, roomPrefab.name);
                if(prefab != null) prefab.collision.set(EntityManager.instance.getEntityCollisionSize(roomPrefab.group, roomPrefab.name));

                // place the prefab
                prefab.setPosition(pickedPos.x + 0.5f, pickedPos.y + 0.5f, t.floorHeight + 0.5f + roomPrefab.heightOffset);

                // some prefabs get attached to the ceiling
                if(roomPrefab.placeOnCeiling) prefab.z = t.ceilHeight + 0.5f - prefab.collision.z + roomPrefab.heightOffset;
                else if(roomPrefab.placeCentered) prefab.z = ((t.ceilHeight - t.floorHeight) * 0.5f) + t.floorHeight + 0.5f - prefab.collision.z * 0.5f + roomPrefab.heightOffset;

                // rotate the prefab a bit randomly
                prefab.rotation.x += roomPrefab.rotateAmount.x * 0.5f - random.nextFloat() * roomPrefab.rotateAmount.x;
                prefab.rotation.y += roomPrefab.rotateAmount.y * 0.5f - random.nextFloat() * roomPrefab.rotateAmount.y;
                prefab.rotation.z += roomPrefab.rotateAmount.z * 0.5f - random.nextFloat() * roomPrefab.rotateAmount.z;

                level.entities.add(prefab);
                placedPrefabs.add(new RoomPrefab(roomPrefab.group, roomPrefab.name, new Vector2(prefab.x, prefab.y)));

                return prefab;
            }
        }

        return null;
    }

    private int freeTileRadiusAround(int cx, int cy) {

        float matchFloorHeight = level.getTile(cx, cy).floorHeight;

        int checkSize;
        for(checkSize = 1; checkSize <= 5; checkSize++) {
            for (int x = -checkSize; x <= checkSize; x++) {
                for (int y = -checkSize; y <= checkSize; y++) {
                    Tile t = level.getTileOrNull(x + cx, y + cy);
                    if (t == null || t.blockMotion || t.floorHeight != matchFloorHeight || t.ceilHeight - t.floorHeight < 0.7f) {
                        return checkSize - 1;
                    }
                }
            }
        }
        return checkSize;
    }

    private int freeTileAndPrefabRadiusAround(int cx, int cy) {
        int freeTileSpace = freeTileRadiusAround(cx, cy);

        Vector2 l = new Vector2();
        for(RoomPrefab p : placedPrefabs) {
            l.set(cx, cy);
            int dist = (int)(l.sub(p.placedAt).len() - (float)p.size);
            if(dist < freeTileSpace) freeTileSpace = dist;
        }

        return freeTileSpace;
    }

    private int freeSpaceAroundWall(int cx, int cy) {

        Vector2 startLoc = new Vector2(cx, cy);

        Tile north = level.getTileOrNull(cx, cy - 1);
        Tile east = level.getTileOrNull(cx + 1, cy);
        Tile south = level.getTileOrNull(cx, cy + 1);
        Tile west = level.getTileOrNull(cx - 1, cy);

        int startX = cx - 5;
        int startY = cy - 5;
        int endX = cx + 5;
        int endY = cy + 5;

        if(tileIsSolid(north)) {
            startY = cy;
        }
        else if(tileIsSolid(east)) {
            endX = cx;
        }
        else if(tileIsSolid(south)) {
            endY = cy;
        }
        else if(tileIsSolid(west)) {
            startX = cx;
        }

        int freeSpace = 5;
        float checkFloorHeight = level.getTile(cx, cy).floorHeight;

        Vector2 l = new Vector2();
        for(int x = startX; x <= endX; x++) {
            for(int y = startY; y <= endY; y++) {
                Tile t = level.getTileOrNull(x, y);
                if(tileIsSolid(t) || t.floorHeight != checkFloorHeight) {
                    int free = (int)(l.set(x, y).sub(startLoc).len()) - 1;
                    if(free < freeSpace) freeSpace = free;
                }
            }
        }

        for(RoomPrefab p : placedPrefabs) {
            l.set(cx, cy);
            int dist = (int)(l.sub(p.placedAt).len() - (float)p.size);
            if(dist < freeSpace) freeSpace = dist;
        }

        return freeSpace;
    }

    public Array<Vector2> findPossibleWindowLocations(Room room) {
        return findPossibleDoorPositions(room, !room.northDoor, !room.southDoor, !room.eastDoor, !room.westDoor, true);
    }

    public Array<Vector2> findPossibleDoorPositions(Room room) {
        return findPossibleDoorPositions(room, room.northDoor, room.southDoor, room.eastDoor, room.westDoor, true);
    }

    public Array<Vector2> findPossibleDoorPositions(Room room, boolean north, boolean south, boolean east, boolean west, boolean checkBothSides) {
        return findPossibleDoorPositions(room, north, south, east, west, checkBothSides, false);
    }

    public Array<Vector2> findPossibleDoorPositions(Room room, boolean north, boolean south, boolean east, boolean west, boolean checkBothSides, boolean isForHallway) {

        int minY = room.y;
        int maxY = room.y + room.height - 1;
        int minX = room.x;
        int maxX = room.x + room.width - 1;

        Array<Vector2> doorPositions = new Array<Vector2>();
        if (north) {
            int foundDoors = 0;
            for(int y = room.y; y < room.height + room.y && foundDoors == 0; y++) {
                for (int x = room.x; x < room.width + room.x; x++) {
                    if (isGoodDoorLocation(x, y, isForHallway) && (!checkBothSides || isGoodDoorLocation(x, y - 2, isForHallway))) {
                        doorPositions.add(new Vector2(x, y - 1));
                        foundDoors++;
                    }
                }
            }
        }
        if (south) {
            int foundDoors = 0;
            for(int y = room.y + room.height; y >= room.y && foundDoors == 0; y--) {
                for (int x = room.x; x < room.width + room.x; x++) {
                    if (isGoodDoorLocation(x, y - 1, isForHallway) && (!checkBothSides || isGoodDoorLocation(x, y + 1, isForHallway))) {
                        doorPositions.add(new Vector2(x, y));
                        foundDoors++;
                    }
                }
            }
        }
        if (east) {
            int foundDoors = 0;
            for(int x = room.x + room.width; x >= room.x && foundDoors == 0; x--) {
                for (int y = room.y; y < room.y + room.height; y++) {
                    if (isGoodDoorLocation(x - 1, y, isForHallway) && (!checkBothSides || isGoodDoorLocation(x + 1, y, isForHallway))) {
                        doorPositions.add(new Vector2(x, y));
                        foundDoors++;
                    }
                }
            }
        }
        if (west) {
            int foundDoors = 0;
            for(int x = room.x; x < room.x + room.width && foundDoors == 0; x++) {
                for (int y = room.y; y < room.y + room.height; y++) {
                    if (isGoodDoorLocation(x, y, isForHallway) && (!checkBothSides || isGoodDoorLocation(room.x - 2, y, isForHallway))) {
                        doorPositions.add(new Vector2(x - 1, y));
                        foundDoors++;
                    }
                }
            }
        }

        // don't make doors or windows where hallways are
        doorPositions.removeAll(hallStarts, true);

        return doorPositions;
    }

    public boolean isGoodDoorLocation(int x, int y, boolean isForHallway) {
        Tile t = level.getTileOrNull(x, y);
        if(t != null) {
            if(t.isLocked) return false;
            t.init(Level.Source.EDITOR);
        }

        if(theme != null && theme.hallwayType == RoomGeneratorTheme.HallwayType.SEWER) {
            return !tileIsSolid(t) && tileIsLiquid(t) && t.getMinOpenHeight() >= 0.5f;
        }
        else if(theme != null && theme.hallwayType == RoomGeneratorTheme.HallwayType.TEMPLE && !isForHallway) {
            return !tileIsSolid(t) && t.getMinOpenHeight() >= 0.5f && t.floorHeight == room.floorHeight;
        }
        else {
            return tileIsEmpty(t) && t.hasRoomFor(0.9f) && t.getMinOpenHeight() >= 0.9f && !t.data.isLava && !t.data.isWater && t.data.hurts == 0;
        }
    }

    public void carvedoors(Room room) {
        if(room.makeExits) {
            Array<Vector2> doorPositions = findPossibleDoorPositions(room);

            // make some doors
            int numDoorsMade = 0;
            for(int i = 0; i < random.nextInt(3) + 1; i++) {
                if(doorPositions.size > 0) {
                    Vector2 pickedDoorPosition = doorPositions.get(random.nextInt(doorPositions.size));
                    doorPositions.removeValue(pickedDoorPosition, true);
                    carveDoor((int) pickedDoorPosition.x, (int) pickedDoorPosition.y, room);

                    avoidDecorating.add(pickedDoorPosition);

                    numDoorsMade++;
                }
            }

            // make some windows
            if(room.makeWindows) {
                Array<Vector2> windowPositions = findPossibleWindowLocations(room);

                for (int i = 0; i < random.nextInt(3); i++) {
                    if (windowPositions.size > 0) {
                        Vector2 pickedWindowPosition = windowPositions.get(random.nextInt(windowPositions.size));
                        windowPositions.removeValue(pickedWindowPosition, true);
                        carveWindow((int) pickedWindowPosition.x, (int) pickedWindowPosition.y, room);
                    }
                }
            }

            // check if we made any doors here, might need to fill this in
            if(numDoorsMade == 0 && room.theme.type == RoomTheme.RoomType.TEMPLE) {
                // no exits, fill in this room :(
                for(int cx = room.x; cx < room.x + room.width; cx++) {
                    for(int cy = room.y; cy < room.y + room.height; cy++) {
                        deleteTile(cx, cy);
                    }
                }
            }
        }
    }

    public Tile paintTile(int x, int y, Room room) {
        Tile t = level.getTileOrNull(x, y);
        if(t == null) {
            t = Tile.NewSolidTile();
            level.setTile(x, y, t);
        }

        if(room != null) {
            if(room.wallTex != null) {
                t.wallTex = room.wallTex.tex;
                t.wallTexAtlas = room.wallTex.texAtlas;
            }

            if(room.lowerWallTex != null) {
                t.wallBottomTexAtlas = room.lowerWallTex.texAtlas;
                t.wallBottomTex = room.lowerWallTex.tex;
            }
            else if(room.wallTex != null) {
                t.wallTex = room.wallTex.tex;
                t.wallTexAtlas = room.wallTex.texAtlas;
            }

            if(room.ceilTex != null) {
                t.ceilTex = room.ceilTex.tex;
                t.ceilTexAtlas = room.ceilTex.texAtlas;
            }
            if(room.floorTex != null) {
                t.floorTex = room.floorTex.tex;
                t.floorTexAtlas = room.floorTex.texAtlas;
            }
        }

        return t;
    }

    public Tile carveTile(int x, int y, Room room) {
        Tile t = new Tile();
        t.copy(Tile.EmptyTile());

        if(room != null) {
            t.floorHeight = room.floorHeight;
            t.ceilHeight = room.ceilingHeight;

            if(room.wallTex != null) {
                t.wallTexAtlas = room.wallTex.texAtlas;
                t.wallTex = room.wallTex.tex;
            }

            if(room.floorTex != null) {
                t.floorTexAtlas = room.floorTex.texAtlas;
                t.floorTex = room.floorTex.tex;

                if(room.floorTex.tileNoiseAmount > 0) {
                    t.floorHeight += Game.rand.nextFloat() * room.floorTex.tileNoiseAmount;

                    // if the side shows, paint it the floor texture
                    t.wallBottomTexAtlas = t.floorTexAtlas;
                    t.wallBottomTex = t.floorTex;
                }

                if(room.floorTex.heightNoiseAmount > 0) {
                    perlinNoise.setFrequency(room.floorTex.heightNoiseFrequency);
                    float noiseAmount = room.floorTex.heightNoiseAmount;
                    t.slopeNE -= perlinNoise.getHeight(x - 0.5f, y - 0.5f) * noiseAmount;
                    t.slopeNW -= perlinNoise.getHeight(x + 0.5f, y - 0.5f) * noiseAmount;
                    t.slopeSE -= perlinNoise.getHeight(x - 0.5f, y + 0.5f) * noiseAmount;
                    t.slopeSW -= perlinNoise.getHeight(x + 0.5f, y + 0.5f) * noiseAmount;

                    // if the side shows, paint it the floor texture
                    t.wallBottomTexAtlas = t.floorTexAtlas;
                    t.wallBottomTex = t.floorTex;
                }
            }
            if(room.ceilTex != null) {
                t.ceilTexAtlas = room.ceilTex.texAtlas;
                t.ceilTex = room.ceilTex.tex;

                if(room.ceilTex.tileNoiseAmount > 0) {
                    t.ceilHeight += Game.rand.nextFloat() * room.ceilTex.tileNoiseAmount;

                    // if the side shows, paint it the ceiling texture
                    t.wallTexAtlas = t.ceilTexAtlas;
                    t.wallTex = t.ceilTex;
                }

                if(room.ceilTex.heightNoiseAmount > 0) {
                    perlinNoise.setFrequency(room.ceilTex.heightNoiseFrequency);
                    float noiseAmount = -room.ceilTex.heightNoiseAmount;
                    t.ceilSlopeNE -= perlinNoise.getHeight(x - 0.5f + level.width, y - 0.5f + level.height) * noiseAmount;
                    t.ceilSlopeNW -= perlinNoise.getHeight(x + 0.5f + level.width, y - 0.5f + level.height) * noiseAmount;
                    t.ceilSlopeSE -= perlinNoise.getHeight(x - 0.5f + level.width, y + 0.5f + level.height) * noiseAmount;
                    t.ceilSlopeSW -= perlinNoise.getHeight(x + 0.5f + level.width, y + 0.5f + level.height) * noiseAmount;

                    // if the side shows, paint it the ceiling texture
                    if (t.wallBottomTex == null) {
                        t.wallBottomTex = t.wallTex;
                        t.wallBottomTexAtlas = t.wallTexAtlas;
                    }

                    t.wallTexAtlas = t.ceilTexAtlas;
                    t.wallTex = t.ceilTex;
                }
            }
            if(room.lowerWallTex != null) {
                t.wallBottomTexAtlas = room.lowerWallTex.texAtlas;
                t.wallBottomTex = room.lowerWallTex.tex;
            }
        }

        level.setTile(x, y, t);

        return t;
    }

    public Tile carveHallway(int x, int y) {
        Tile t = level.getTileOrNull(x, y);
        boolean paintFloor = true;

        if(t == null) {
            t = new Tile();
            Tile.copy(Tile.EmptyTile());
            level.setTile(x, y, t);
        }
        else if(tileIsSolid(t)) {
            t.blockMotion = false;
            t.renderSolid = false;
            t.floorHeight = -0.5f;
            t.ceilHeight = 0.5f;

            t.wallBottomTexAtlas = "t1";
            t.wallBottomTex = 0;

            t.floorTex = 2;
            t.floorTexAtlas = null;

            t.tileSpaceType = Tile.TileSpaceType.EMPTY;
        }
        else {
            paintFloor = false;
        }

        // Paint this tile
        Material wallMaterial = theme.defaultMaterial;

        Material floorMaterial = theme.defaultFloorMaterial;
        if(theme.defaultHallwayFloorMaterial != null) {
            floorMaterial = theme.defaultHallwayFloorMaterial;
        }

        Material ceilMaterial = theme.defaultCeilingMaterial;
        if(theme.defaultHallwayCeilMaterial != null) {
            ceilMaterial = theme.defaultHallwayCeilMaterial;
        }

        if(wallMaterial != null) {
            t.wallTex = wallMaterial.tex;
            t.wallTexAtlas = wallMaterial.texAtlas;

            t.wallBottomTex = wallMaterial.tex;
            t.wallBottomTexAtlas = wallMaterial.texAtlas;

            t.ceilTex = wallMaterial.tex;
            t.ceilTexAtlas = wallMaterial.texAtlas;
        }

        if(theme.defaultHallwayWallMaterial != null) {
            t.wallBottomTex = theme.defaultHallwayWallMaterial.tex;
            t.wallBottomTexAtlas = theme.defaultHallwayWallMaterial.texAtlas;
        }

        if(paintFloor) {
            if (floorMaterial != null) {
                t.floorTex = floorMaterial.tex;
                t.floorTexAtlas = floorMaterial.texAtlas;
            }

            if (ceilMaterial != null) {
                t.ceilTex = ceilMaterial.tex;
                t.ceilTexAtlas = ceilMaterial.texAtlas;
            }
        }

        // Done painting
        t.init(Level.Source.EDITOR);
        return t;
    }

    public void carveDoor(int x, int y, Room room) {
        Tile t = new Tile();
        Tile.copy(Tile.EmptyTile());
        t.init(Level.Source.EDITOR);

        if(room != null) {
            t.floorHeight = room.floorHeight;
            t.ceilHeight = t.floorHeight + 1;

            if(room.theme.type == RoomTheme.RoomType.TEMPLE) {
                t.ceilHeight += 0.5f;
            }

            if(room.parent != null) {
                if (room.floorHeight != room.parent.floorHeight) {
                    t.ceilHeight += Math.abs(room.parent.floorHeight - room.floorHeight);
                }
            }

            if(room.wallTex != null) {
                t.wallTexAtlas = room.wallTex.texAtlas;
                t.wallTex = room.wallTex.tex;
            }
            if(room.floorTex != null && !t.data.isLava && !t.data.isWater && t.data.hurts == 0) {
                t.floorTexAtlas = room.floorTex.texAtlas;
                t.floorTex = room.floorTex.tex;
            }
            if(room.ceilTex != null) {
                t.ceilTexAtlas = room.ceilTex.texAtlas;
                t.ceilTex = room.ceilTex.tex;
            }
        }

        level.setTile(x, y, t);
        t.init(Level.Source.EDITOR);
    }

    public void carveWindow(int x, int y, Room room) {

        // only carve here if this is already solid!
        if(tileIsSolid(level.getTileOrNull(x, y))) {
            Tile t = new Tile();
            Tile.copy(Tile.EmptyTile());

            if (room != null) {
                float minCeilHeight = room.ceilingHeight;
                if (room.parent != null && room.parent.ceilingHeight < minCeilHeight)
                    minCeilHeight = room.parent.ceilingHeight;

                float maxFloorHeight = room.floorHeight;
                if (room.parent != null && room.parent.floorHeight > maxFloorHeight)
                    maxFloorHeight = room.parent.floorHeight;

                t.floorHeight = maxFloorHeight + 0.4375f;
                t.ceilHeight = t.floorHeight + 0.4375f;
                if (t.ceilHeight > minCeilHeight) t.ceilHeight = minCeilHeight;

                if (room.wallTex != null) {
                    t.wallTexAtlas = room.wallTex.texAtlas;
                    t.wallTex = room.wallTex.tex;

                    t.floorTexAtlas = room.wallTex.texAtlas;
                    t.floorTex = room.wallTex.tex;

                    t.ceilTexAtlas = room.wallTex.texAtlas;
                    t.ceilTex = room.wallTex.tex;
                }
            }

            level.setTile(x, y, t);
            t.init(Level.Source.EDITOR);
        }
    }

    public void deleteTile(int x, int y) {
        level.setTile(x, y, null);
    }

    public Tile makeColumn(int x, int y, Room room) {
        Tile t = makeWall(x, y, room);

        if(theme.columnMaterial != null) {
            t.wallTex = theme.columnMaterial.tex;
            t.wallTexAtlas = theme.columnMaterial.texAtlas;

            t.wallBottomTex = theme.columnMaterial.tex;
            t.wallBottomTexAtlas = theme.columnMaterial.texAtlas;
        }

        // crack some columns!
        // todo: make this a room data setting?
        if(room.theme != null && random.nextFloat() < room.theme.crackedColumnChance) {
            t.blockMotion = false;
            t.renderSolid = false;

            float center = (room.ceilingHeight - room.floorHeight) / 2f + room.floorHeight;

            t.ceilHeight = center + 0.08f;
            t.floorHeight = center - 0.08f;

            t.ceilSlopeNE += random.nextFloat() * 0.3f;
            t.ceilSlopeNW += random.nextFloat() * 0.3f;
            t.ceilSlopeSE += random.nextFloat() * 0.3f;
            t.ceilSlopeSW += random.nextFloat() * 0.3f;

            t.slopeSW -= random.nextFloat() * 0.3f;
            t.slopeSE -= random.nextFloat() * 0.3f;
            t.slopeNW -= random.nextFloat() * 0.3f;
            t.slopeNE -= random.nextFloat() * 0.3f;

            t.floorTex = t.wallTex;
            t.ceilTex = t.wallTex;
            t.floorTexAtlas = t.wallTexAtlas;
            t.ceilTexAtlas = t.wallTexAtlas;

            if(theme.columnBrokenMaterial != null) {
                t.floorTex = theme.columnBrokenMaterial.tex;
                t.floorTexAtlas = theme.columnBrokenMaterial.texAtlas;

                t.ceilTex = t.floorTex;
                t.ceilTexAtlas = t.floorTexAtlas;

                t.wallTex = t.floorTex;
                t.wallTexAtlas = t.floorTexAtlas;

                t.wallBottomTex = t.floorTex;
                t.wallBottomTexAtlas = t.floorTexAtlas;
            }
        }

        return t;
    }

    public Tile makeWall(int x, int y, Room room) {
        Tile t = new Tile();
        t.copy(Tile.solidWall);
        t.blockMotion = true;
        t.renderSolid = true;

        if(room != null) {
            t.floorHeight = room.floorHeight;
            t.ceilHeight = room.ceilingHeight;

            if(room.wallTex != null) {
                t.wallTexAtlas = room.wallTex.texAtlas;
                t.wallTex = room.wallTex.tex;
            }
            if(room.lowerWallTex != null) {
                t.wallBottomTexAtlas = room.lowerWallTex.texAtlas;
                t.wallBottomTex = room.lowerWallTex.tex;
            }
            if(room.floorTex != null) {
                t.floorTexAtlas = room.floorTex.texAtlas;
                t.floorTex = room.floorTex.tex;
            }
            if(room.ceilTex != null) {
                t.ceilTexAtlas = room.ceilTex.texAtlas;
                t.ceilTex = room.ceilTex.tex;
            }
        }

        level.setTile(x, y, t);

        return t;
    }

    public Tile getTile(int x, int y) {
        return level.getTileOrNull(x, y);
    }

    public boolean tileIsLiquid(Tile t) {
        if(t == null || t.data == null) return false;
        return t.data.isLava || t.data.isWater;
    }

    public boolean tileIsEmpty(Tile t) {
        return t != null && !tileIsSolid(t) && !tileIsLiquid(t) && t.ceilHeight - t.floorHeight > 0.9f;
    }

    public boolean tileHurts(Tile t) {
        t.init(Level.Source.EDITOR);
        if(t.data == null) return false;
        return t.data.hurts > 0;
    }

    public boolean tileIsSolid(Tile t) {
        return t == null || t.blockMotion || t.renderSolid || (Math.abs(t.floorHeight - t.ceilHeight) < 0.05f);
    }

    public boolean hasWallNearby(int x, int y) {
        Tile center = level.getTileOrNull(x, y);
        Tile north = level.getTileOrNull(x, y - 1);
        Tile east = level.getTileOrNull(x + 1, y);
        Tile south = level.getTileOrNull(x, y + 1);
        Tile west = level.getTileOrNull(x - 1, y);

        if (center != null && tileIsEmpty(center)) {
            if (tileIsSolid(north) || tileIsSolid(east) || tileIsSolid(south) || tileIsSolid(west)) return true;
        }
        return false;
    }

    public Array<Vector2> getCardinalLocationsAround(int x, int y) {

        Array<Vector2> locs = new Array<Vector2>();

        locs.add(new Vector2(x, y - 1));
        locs.add(new Vector2(x - 1, y));
        locs.add(new Vector2(x, y + 1));
        locs.add(new Vector2(x + 1, y));

        return locs;
    }

    public void lockTile(int x, int y) {
        lockedTiles.put(x + y * level.height, true);
    }

    public boolean isTileLocked(int x, int y) {
        return lockedTiles.containsKey(x + y * level.height);
    }

    public boolean isNavigatableLevel() {
        try {
            Vector2 firstHall = exitHallways.first().start;
            Boolean[][] navigatable = new Boolean[level.width][level.height];

            level.updateSpatialHash(null);

            // simple check to make sure this room is valid,
            // by making sure the navigatable flood fill reached all of the hall starts
            floodFillNavigation((int) firstHall.x, (int) firstHall.y, navigatable, null);

            // visualize check
            /*for(int x = 0; x < level.width; x++) {
                for(int y = 0; y < level.height; y++) {
                    if(navigatable[x][y] != null && navigatable[x][y]) {
                        Sprite sprite = new Sprite();
                        sprite.x = x + 0.5f;
                        sprite.y = y + 0.5f;
                        sprite.z = level.getTile(x, y).floorHeight;
                        sprite.tex = 2;
                        sprite.spriteAtlas = "editor";
                        sprite.persists = false;
                        level.entities.add(sprite);
                    }
                }
            }*/

            for (int i = 0; i < exitHallways.size; i++) {
                Hallway hall = exitHallways.get(i);
                if (!navigatable[(int) hall.start.x][(int) hall.start.y] && !navigatable[(int) hall.end.x][(int) hall.end.y])
                    return false;
            }

            return true;
        }
        catch(Exception ex) {
            Gdx.app.log("RoomGenerator", ex.getMessage());
            return false;
        }
    }

    Entity testCollider = new Entity();
    public void floodFillNavigation(int x, int y, Boolean[][] navigatable, Float previousFloorHeight) {
        if(x < 0 || y < 0 || x >= level.width || y >= level.height) return;

        // been here before?
        if(navigatable[x][y] != null) return;

        Tile currentTile = level.getTileOrNull(x, y);
        if(currentTile == null || currentTile.blockMotion || currentTile.getMinOpenHeight() < 0.8f) {
            navigatable[x][y] = false;
            return;
        }

        float floorHeight = currentTile.floorHeight;

        // can we step up to this next height?
        if(previousFloorHeight != null) {

            testCollider.x = x + 0.5f;
            testCollider.y = y + 0.5f;
            testCollider.z = previousFloorHeight - 0.15f + 0.5f;
            testCollider.collision.set(0.1f, 0.1f, 0.6f);
            testCollider.isSolid = true;
            testCollider.isDynamic = true;
            testCollider.collidesWith = Entity.CollidesWith.all;
            testCollider.stepHeight = 0;

            Entity found = level.getHighestEntityCollision(testCollider.x, testCollider.y, testCollider.z, testCollider.collision, testCollider);

            boolean canStepOnEntity = false;
            float maxStepHeight = 0.4f;

            if(found != null && found.collision.x >= 0.4f && found.collision.y >= 0.4f && (int)found.x == x && (int)found.y == y) {
                canStepOnEntity = Math.abs((found.z + found.collision.z) - testCollider.z) < 0.2f;
                if(canStepOnEntity) {
                    floorHeight = found.z + found.collision.z - 0.5f;
                }
            }

            if (!canStepOnEntity && Math.abs(floorHeight - previousFloorHeight) > maxStepHeight) {
                navigatable[x][y] = false;
                return;
            }
        }

        // we can walk here, continue on with the flood fill
        navigatable[x][y] = true;

        floodFillNavigation(x + 1, y, navigatable, floorHeight);
        floodFillNavigation(x - 1, y, navigatable, floorHeight);
        floodFillNavigation(x, y + 1, navigatable, floorHeight);
        floodFillNavigation(x, y - 1, navigatable, floorHeight);
    }
}
