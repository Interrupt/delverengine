package com.interrupt.dungeoneer.serializers.v2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.OverworldLevel;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.serializers.*;
import com.interrupt.dungeoneer.tiles.Tile;

import java.io.*;
import java.util.HashMap;

public class ThreadSafeLevelSerializer {
    private Kryo kryo = new Kryo();

    public ThreadSafeLevelSerializer() {
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);

        // register some classes
        kryo.register(Color.class);
        kryo.register(Vector3.class);
        kryo.register(Vector2.class);
        kryo.register(Tile.class);
        kryo.register(Entity.class);
        kryo.register(Monster.class);
        kryo.register(Particle.class);
        kryo.register(Sprite.class);
        kryo.register(Model.class);
        kryo.register(DrawableSprite.class);
        kryo.register(DrawableMesh.class);
        kryo.register(Array.class);

        // register some serializers
        kryo.register(Prefab.class, new PrefabSerializer());
        kryo.register(Array.ArrayIterator.class, new LibGdxArrayIteratorSerializer());
        kryo.register(Tile.class, new TileSerializer());
        kryo.register(Color.class, new ColorSerializer());
        kryo.register(Array.class, new ArraySerializer());
        kryo.register(IntArray.class, new IntArraySerializer());
        kryo.register(HashMap.class, new HashMapSerializer());
    }

    public Level loadLevel(FileHandle file) {
        Input input = new Input(file.read());
        Level level = kryo.readObject(input, Level.class);
        input.close();
        return level;
    }

    public Level loadLevel(File file) {
        try {
            Input input = new Input(new FileInputStream(file));
            Level level = kryo.readObject(input, Level.class);
            input.close();
            return level;
        }
        catch (Exception ex) {
            return null;
        }
    }

    public Level loadLevel(byte[] bytes) {
        Input input = new Input(bytes);
        Level level = kryo.readObject(input, Level.class);
        input.close();
        return level;
    }

    public OverworldLevel loadOverworldLevel(FileHandle file) {
        Input input = new Input(file.read());
        OverworldLevel level = kryo.readObject(input, OverworldLevel.class);
        input.close();
        return level;
    }

    public void saveLevel(FileHandle file, Level level) {
        Output output = new Output(file.write(false));
        kryo.writeObject(output, level);
        output.close();
    }

    public void saveLevel(File file, Level level) {
        try {
            Output output;
            output = new Output(new FileOutputStream(file));
            kryo.writeObject(output, level);
            output.close();
        } catch (FileNotFoundException e) {
            // oops!
        }
    }

    public Object copyObject(Object object) {
        if(object == null) return null;
        try {
            FastOutput output = new FastOutput(64, 20971520);
            kryo.writeObject(output, object);
            FastInput input = new FastInput(output.getBuffer());
            return kryo.readObject(input, object.getClass());
        }
        catch(Exception ex) {
            Gdx.app.log("Delver", ex.toString());
            return null;
        }
    }

    public byte[] toBytes(Object object) {
        if (object == null) return null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Output output = new Output(stream);
            kryo.writeObject(output, object);
            output.close();
            return stream.toByteArray();
        } catch (Exception ex) {
            Gdx.app.log("Delver", ex.toString());
            return null;
        }
    }
}
