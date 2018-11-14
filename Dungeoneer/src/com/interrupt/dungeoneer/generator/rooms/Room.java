package com.interrupt.dungeoneer.generator.rooms;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

public abstract class Room {
    public int x;
    public int y;
    public final int width;
    public final int height;

    public boolean makeExits = true;
    public boolean makeWindows = true;
    public boolean canContainRooms = true;
    public boolean canDecorate = true;
    public boolean canOffset = true;

    public Room parent = null;
    public boolean attachedWest = false;
    public boolean attachedNorth = false;

    public float floorHeight = -0.5f;
    public float ceilingHeight = 0.5f;

    public boolean westDoor, eastDoor, northDoor, southDoor;

    public Array<Room> subrooms = new Array<Room>();

    public final Random random;

    public Material wallTex = null;
    public Material lowerWallTex = null;
    public Material floorTex = null;
    public Material ceilTex = null;

    public RoomTheme theme = null;

    public boolean overlaps(Room other) {
        if(Math.abs(x - other.x) < width + 1 + other.width) {
            if(Math.abs(y - other.y) < height + 1 + other.height) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRoomFor(Room subroom) {
        if(subroom.width >= width || subroom.height >= height) return false;

        for(Room existing : subrooms) {
            if(existing.overlaps(subroom)) return false;
        }

        return true;
    }

    public boolean overlapsSubrooms(int x, int y) {
        for(Room existing : subrooms) {
            if(x >= existing.x && x < existing.x + existing.width
                    && y >= existing.y && y < existing.y + existing.height) return true;
        }
        return false;
    }

    public Room(int x, int y, int width, int height, Random random, Room parent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;
        this.random = random;

        if(parent != null) {
            floorHeight = parent.floorHeight;
        }

        ceilingHeight = floorHeight + 1f + random.nextInt(8) * 0.2f;
    }

    public void transformHeightPosition(float amount) {
        floorHeight += amount;
        ceilingHeight += amount;

        for(Room s : subrooms) {
            s.transformHeightPosition(amount);
        }
    }

    public abstract void fill(RoomGenerator generator);

    // called after all subrooms are generated
    public void finalize(RoomGenerator generator) {

    }

    // called before the room is decorated
    public void decorate(RoomGenerator generator) {

    }

    // Room is done!
    public void done(RoomGenerator generator) {

    }
}