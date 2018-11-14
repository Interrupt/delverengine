package com.interrupt.dungeoneer.generator.rooms;

import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.rooms.themes.RoomTheme;

import java.util.Random;

public class EmptyRoom extends Room {
    Room featureSubroom = null;

    public EmptyRoom(int x, int y, int width, int height, Random random, Room parent, RoomTheme theme) {
        super(x, y, width, height, random, parent);
        this.theme = theme;
    }

    public void makeFeature(RoomGenerator generator) {

        // enough room to make a middle room!
        if(width > 8 && height > 8) {
            int featureWidth = width - 4;
            int featureHeight = height - 4;

            int diffWidth = width - featureWidth;
            int diffHeight = height - featureHeight;

            featureWidth -= generator.random.nextInt(diffWidth);
            featureHeight -= generator.random.nextInt(diffHeight);

            featureSubroom = generator.pickSubroom(
                    x + (width - featureWidth) / 2,
                    y + (height - featureHeight) / 2,
                    featureWidth,
                    featureHeight,
                    random,
                    this);

            featureSubroom.eastDoor = true;
            featureSubroom.westDoor = true;
            featureSubroom.northDoor = true;
            featureSubroom.southDoor = true;

            featureSubroom.parent = this;

            subrooms.add(featureSubroom);
        }
    }

    @Override
    public void fill(RoomGenerator generator) {
        /*if(parent != null) {
            floorHeight = parent.floorHeight;
        }
        floorHeight -= random.nextInt(2) * 0.2f;*/

        if(theme.canLowerFloor) {
            floorHeight -= 0.2f;
        }

        ceilingHeight = floorHeight + theme.pickRoomHeight(generator);

        wallTex = theme.pickWallMaterial(generator);
        lowerWallTex = theme.pickLowerWallMaterial(generator);
        floorTex = theme.pickFloorMaterial(generator);
        ceilTex = theme.pickCeilingMaterial(generator);

        if(generator.random.nextBoolean()) makeFeature(generator);

        for(int cx = x - 1; cx < x + width + 1; cx++) {
            generator.paintTile(cx, y - 1, this);
            generator.paintTile(cx, y + height, this);
        }

        for(int cy = y; cy < y + height; cy++) {
            generator.paintTile(x - 1, cy, this);
            generator.paintTile(x + width, cy, this);
        }

        for(int cx = x; cx < x + width; cx++) {
            for(int cy = y; cy < y + height; cy++) {
                generator.carveTile(cx, cy, this);
            }
        }

        if(featureSubroom != null) {
            generator.carve(featureSubroom);
            generator.carvedoors(featureSubroom);
            generator.decorateRoom(featureSubroom);
        }
    }
}
