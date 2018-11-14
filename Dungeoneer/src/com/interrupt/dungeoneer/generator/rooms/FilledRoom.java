package com.interrupt.dungeoneer.generator.rooms;

import com.badlogic.gdx.math.Vector2;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;

import java.util.Random;

public class FilledRoom extends Room {
    public FilledRoom(int x, int y, int width, int height, Random random, Room parent) {
        super(x, y, width, height, random, parent);
        this.canContainRooms = false;
        this.makeExits = false;
        this.makeWindows = false;
        this.canDecorate = false;
        this.canOffset = false;
    }

    public void fill(RoomGenerator generator) {

        boolean makeSecret = generator.random.nextFloat() < 0.65f;

        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                generator.deleteTile(cx, cy);
            }
        }

        if(makeSecret) {
            theme = generator.theme.pickDefaultRoomTheme(generator);
            wallTex = theme.pickWallMaterial(generator);
            lowerWallTex = wallTex;
            floorTex = theme.pickFloorMaterial(generator);
            ceilTex = theme.pickCeilingMaterial(generator);

            if(height >= 3 && width >= 3) {
                if (northDoor) {
                    int px = x + width / 2;
                    int py = y;
                    makeSecret(px, py, 0, 1, 1, generator);
                } else if (southDoor) {
                    int px = x + width / 2;
                    int py = y + height - 1;
                    makeSecret(px, py, 0, -1, 3, generator);
                } else if (eastDoor) {
                    int px = x + width - 1;
                    int py = y + height / 2;
                    makeSecret(px, py, -1, 0, 0, generator);
                } else if (westDoor) {
                    int px = x;
                    int py = y + height / 2;
                    makeSecret(px, py, 1, 0, 2, generator);
                }
            }
        }
    }

    public void makeSecret(int doorX, int doorY, int xDir, int yDir, int rot, RoomGenerator generator) {
        generator.carveDoor(doorX, doorY, this);
        generator.carveTile(doorX + xDir, doorY + yDir, this);

        Tile outside = generator.level.getTileOrNull(doorX - xDir, doorY - yDir);
        if(outside == null || outside.blockMotion) return;

        Tile doorTile = generator.level.getTile(doorX, doorY);
        doorTile.floorHeight = outside.floorHeight;
        doorTile.ceilHeight = doorTile.floorHeight + 1f;

        Tile inTile = generator.level.getTile(doorX + xDir, doorY + yDir);
        inTile.floorHeight = outside.floorHeight;
        inTile.ceilHeight = outside.ceilHeight;

        Entity door = null;

        if(generator.theme.secretDoors != null && generator.theme.secretDoors.size > 0) {
            door = EntityManager.instance.Copy(generator.theme.secretDoors.random());
        }
        else {
            door = EntityManager.instance.getEntity("Doors", "Hidden_1");
        }

        if(door != null) {
            for(int i = 0; i < rot; i++) {
                door.rotate90();
            }

            door.z = doorTile.floorHeight + 0.5f;
            door.x = doorX + 0.5f - (0.5f - door.collision.x) * xDir;
            door.y = doorY + 0.5f - (0.5f - door.collision.y) * yDir;

            generator.level.addEntity(door);
        }

        generator.level.editorMarkers.add(new EditorMarker(GenInfo.Markers.secret, doorX + xDir, doorY + yDir));

        generator.level.lockTilesAround(new Vector2(doorX + xDir, doorY + yDir), 1);
    }
}
