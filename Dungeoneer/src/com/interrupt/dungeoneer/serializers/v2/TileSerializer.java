package com.interrupt.dungeoneer.serializers.v2;

import com.badlogic.gdx.utils.ArrayMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.Tile.TileSpaceType;

public class TileSerializer extends Serializer<Tile> {
	{
    	setAcceptsNull(true);
    }

    private static ArrayMap<String, String> stringPool = new ArrayMap<String, String>();

	@Override
	public Tile read(Kryo kryo, Input input, Class<Tile> tileClass) {
		Tile tile = new Tile();
		
		tile.renderSolid = input.readBoolean();
		tile.blockMotion = input.readBoolean();
		tile.wallTex = input.readByte();
		tile.seen = input.readBoolean();
		
		if(!tile.renderSolid) {
			tile.canNav = input.readBoolean();
			tile.ceilTex = input.readByte();
			tile.floorTex = input.readByte();
			
			tile.ceilHeight = input.readFloat();
			tile.ceilSlopeNE = input.readFloat();
			tile.ceilSlopeNW = input.readFloat();
			tile.ceilSlopeSE = input.readFloat();
			tile.ceilSlopeSW = input.readFloat();
			
			tile.floorHeight = input.readFloat();
			tile.slopeNE = input.readFloat();
			tile.slopeNW = input.readFloat();
			tile.slopeSE = input.readFloat();
			tile.slopeSW = input.readFloat();
			
			byte wallBottomByte = input.readByte();
			if(wallBottomByte >= 0) tile.wallBottomTex = wallBottomByte;
			
			tile.ceilTexRot = input.readByte();
			tile.floorTexRot = input.readByte();
			
			tile.tileType = input.readByte();
			tile.tileSpaceType = TileSpaceType.values()[input.readByte()];
			
			tile.drawCeiling = input.readBoolean();
			tile.drawWalls = input.readBoolean();
		}

        // top directional textures
        byte northWallByte = input.readByte();
        if(northWallByte >= 0) tile.northTex = northWallByte;

        byte eastWallByte = input.readByte();
        if(eastWallByte >= 0) tile.eastTex = eastWallByte;

        byte southWallByte = input.readByte();
        if(southWallByte >= 0) tile.southTex = southWallByte;

        byte westWallByte = input.readByte();
        if(westWallByte >= 0) tile.westTex = westWallByte;

        // bottom directional textures
        byte bottomNorthWallByte = input.readByte();
        if(bottomNorthWallByte >= 0) tile.bottomNorthTex= bottomNorthWallByte;

        byte bottomEastWallByte = input.readByte();
        if(bottomEastWallByte >= 0) tile.bottomEastTex = bottomEastWallByte;

        byte bottomSouthWallByte = input.readByte();
        if(bottomSouthWallByte >= 0) tile.bottomSouthTex = bottomSouthWallByte;

        byte bottomWestWallByte = input.readByte();
        if(bottomWestWallByte >= 0) tile.bottomWestTex = bottomWestWallByte;

        // texture atlases
        tile.wallTexAtlas = readAtlas(input);
        tile.wallBottomTexAtlas = readAtlas(input);

        tile.floorTexAtlas = readAtlas(input);
        tile.ceilTexAtlas = readAtlas(input);

        // directional top atlases
        tile.northTexAtlas = readAtlas(input);
        tile.eastTexAtlas = readAtlas(input);
        tile.southTexAtlas = readAtlas(input);
        tile.westTexAtlas = readAtlas(input);

        // directional bottom atlases
        tile.bottomNorthTexAtlas = readAtlas(input);
        tile.bottomEastTexAtlas = readAtlas(input);
        tile.bottomSouthTexAtlas = readAtlas(input);
        tile.bottomWestTexAtlas = readAtlas(input);

		return tile;
	}

    // keep memory usage down by keeping a pool of the atlas names
    public String readAtlas(Input input) {
        String k = input.readString();
        if(k == null || k.isEmpty()) return null;

        if(stringPool.containsKey(k)) return stringPool.get(k);
        stringPool.put(k, k);
        return k;
    }

	@Override
	public void write(Kryo kryo, Output output, Tile tile) {
		output.writeBoolean(tile.renderSolid);
		output.writeBoolean(tile.blockMotion);
		output.writeByte(tile.wallTex);
		output.writeBoolean(tile.seen);

		if(!tile.renderSolid) {
			output.writeBoolean(tile.canNav);
			output.writeByte(tile.ceilTex);
			output.writeByte(tile.floorTex);

			output.writeFloat(tile.ceilHeight);
			output.writeFloat(tile.ceilSlopeNE);
			output.writeFloat(tile.ceilSlopeNW);
			output.writeFloat(tile.ceilSlopeSE);
			output.writeFloat(tile.ceilSlopeSW);
			
			output.writeFloat(tile.floorHeight);
			output.writeFloat(tile.slopeNE);
			output.writeFloat(tile.slopeNW);
			output.writeFloat(tile.slopeSE);
			output.writeFloat(tile.slopeSW);
			
			if(tile.wallBottomTex != null) output.writeByte(tile.wallBottomTex);
			else output.writeByte(-1);
			
			output.writeByte(tile.ceilTexRot);
			output.writeByte(tile.floorTexRot);
			
			output.writeByte(tile.tileType);
			output.writeByte(tile.tileSpaceType.ordinal());
			
			output.writeBoolean(tile.drawCeiling);
			output.writeBoolean(tile.drawWalls);
		}

        // top directional textures
        if(tile.northTex != null) output.writeByte(tile.northTex);
        else output.writeByte(-1);

        if(tile.eastTex != null) output.writeByte(tile.eastTex);
        else output.writeByte(-1);

        if(tile.southTex != null) output.writeByte(tile.southTex);
        else output.writeByte(-1);

        if(tile.westTex != null) output.writeByte(tile.westTex);
        else output.writeByte(-1);

        // bottom directional textures
        if(tile.bottomNorthTex != null) output.writeByte(tile.bottomNorthTex);
        else output.writeByte(-1);

        if(tile.bottomEastTex != null) output.writeByte(tile.bottomEastTex);
        else output.writeByte(-1);

        if(tile.bottomSouthTex != null) output.writeByte(tile.bottomSouthTex);
        else output.writeByte(-1);

        if(tile.bottomWestTex != null) output.writeByte(tile.bottomWestTex);
        else output.writeByte(-1);

        // texture atlases
        output.writeString(tile.wallTexAtlas);
        output.writeString(tile.wallBottomTexAtlas);

        output.writeString(tile.floorTexAtlas);
        output.writeString(tile.ceilTexAtlas);

        // directional top atlases
        output.writeString(tile.northTexAtlas);
        output.writeString(tile.eastTexAtlas);
        output.writeString(tile.southTexAtlas);
        output.writeString(tile.westTexAtlas);

        // directional bottom atlases
        output.writeString(tile.bottomNorthTexAtlas);
        output.writeString(tile.bottomEastTexAtlas);
        output.writeString(tile.bottomSouthTexAtlas);
        output.writeString(tile.bottomWestTexAtlas);
	}

}
