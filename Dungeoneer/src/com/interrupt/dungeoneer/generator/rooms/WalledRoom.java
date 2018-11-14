package com.interrupt.dungeoneer.generator.rooms;

import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;
import com.interrupt.dungeoneer.gfx.Material;

import java.util.Random;

public class WalledRoom extends Room {

    public WalledRoom(int x, int y, int width, int height, Random random, Room parent, RoomTheme theme) {
        super(x, y, width, height, random, parent);
        this.theme = theme;
    }

    public void fill(RoomGenerator generator) {
        /*if(parent != null) {
            floorHeight = parent.floorHeight;
        }
        floorHeight -= random.nextInt(2) * 0.2f;*/

        if(theme.canLowerFloor) {
            floorHeight -= 0.2f;
        }
        else {
            if(parent != null && parent.theme.canLowerFloor) {
                floorHeight += 0.2f;
            }
        }

        ceilingHeight = floorHeight + theme.pickRoomHeight(generator);

        wallTex = theme.pickWallMaterial(generator);
        lowerWallTex = theme.pickLowerWallMaterial(generator);
        floorTex = theme.pickFloorMaterial(generator);
        ceilTex = theme.pickCeilingMaterial(generator);

        for (int cx = x; cx < x + width; cx++) {
            for (int cy = y; cy < y + height; cy++) {
                generator.carveTile(cx, cy, this);
            }
        }

        for (int cx = x - 1; cx < x + width + 1; cx++) {
            generator.makeWall(cx, y - 1, this);
            generator.makeWall(cx, y + height, this);
        }

        for (int cy = y - 1; cy < y + height + 1; cy++) {
            generator.makeWall(x - 1, cy, this);
            generator.makeWall(x + width, cy, this);
        }

        // reduce the amount of doors being made
        if (northDoor && (eastDoor || westDoor)) {
            northDoor = random.nextFloat() > 0.4f;
        }
        if (southDoor && (eastDoor || westDoor)) {
            southDoor = random.nextFloat() > 0.4f;
        }
        if (eastDoor && (northDoor || southDoor)) {
            eastDoor = random.nextFloat() > 0.4f;
        }
        if (westDoor && (northDoor || southDoor)) {
            westDoor = random.nextFloat() > 0.4f;
        }
    }
}
