package com.interrupt.dungeoneer.serializers;

import java.io.*;
import java.util.HashMap;

import com.interrupt.dungeoneer.serializers.v1.LevelSerializer;
import org.objenesis.strategy.SerializingInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Array.ArrayIterator;
import com.badlogic.gdx.utils.IntArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Model;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.entities.Sprite;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.tiles.Tile;

public class KryoSerializer {
	public static synchronized Level loadLevel(FileHandle file) {
        try {
            return com.interrupt.dungeoneer.serializers.v2.LevelSerializer.loadLevel(file);
        }
        catch(Exception ex) {
            return LevelSerializer.loadLevel(file);
        }
	}
	
	public static synchronized Level loadLevel(byte[] bytes) {
        try {
            return com.interrupt.dungeoneer.serializers.v2.LevelSerializer.loadLevel(bytes);
        }
        catch(Exception ex) {
            return LevelSerializer.loadLevel(bytes);
        }
	}

	public static synchronized Level loadOverworldLevel(FileHandle file) {
	    try {
            return com.interrupt.dungeoneer.serializers.v2.LevelSerializer.loadOverworldLevel(file);
        }
        catch(Exception ex) {
	        return null;
        }
    }
	
	public static synchronized void saveLevel(FileHandle file, Level level) {
        com.interrupt.dungeoneer.serializers.v2.LevelSerializer.saveLevel(file, level);
	}
	
	public static synchronized Object copyObject(Object object) {
		return com.interrupt.dungeoneer.serializers.v2.LevelSerializer.copyObject(object);
	}

    public static synchronized byte[] toBytes(Object object) {
        return com.interrupt.dungeoneer.serializers.v2.LevelSerializer.toBytes(object);
    }
}
