package com.interrupt.dungeoneer.serializers.v1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Array.ArrayIterator;
import com.badlogic.gdx.utils.IntArray;
import com.esotericsoftware.kryo.Kryo;
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
import com.interrupt.dungeoneer.serializers.ArraySerializer;
import com.interrupt.dungeoneer.serializers.ColorSerializer;
import com.interrupt.dungeoneer.serializers.HashMapSerializer;
import com.interrupt.dungeoneer.serializers.IntArraySerializer;
import com.interrupt.dungeoneer.serializers.LibGdxArrayIteratorSerializer;
import com.interrupt.dungeoneer.serializers.PrefabSerializer;
import com.interrupt.dungeoneer.serializers.TileSerializer;
import com.interrupt.dungeoneer.tiles.Tile;

public class LevelSerializer {
	private static Kryo kryo = new Kryo();

	static {
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
		kryo.register(ArrayIterator.class, new LibGdxArrayIteratorSerializer());
		kryo.register(Tile.class, new TileSerializer());
		kryo.register(Color.class, new ColorSerializer());
		kryo.register(Array.class, new ArraySerializer());
		kryo.register(IntArray.class, new IntArraySerializer());
		kryo.register(HashMap.class, new HashMapSerializer());
	}

	public static Level loadLevel(FileHandle file) {
		Input input = new Input(file.read());
		Level level = kryo.readObject(input, Level.class);
		input.close();
        level.postLoad();
		return level;
	}

	public static Level loadLevel(File file) {
		try {
			Input input = new Input(new FileInputStream(file));
			Level level = kryo.readObject(input, Level.class);
			input.close();
            level.postLoad();
			return level;
		}
		catch (Exception ex) {
			return null;
		}
	}

	public static Level loadLevel(byte[] bytes) {
		Input input = new Input(bytes);
		Level level = kryo.readObject(input, Level.class);
		input.close();
        level.postLoad();
		return level;
	}

	public static void saveLevel(FileHandle file, Level level) {
		Output output = new Output(file.write(false));
		kryo.writeObject(output, level);
		output.close();
	}

	public static void saveLevel(File file, Level level) {
		try {
			Output output;
			output = new Output(new FileOutputStream(file));
			kryo.writeObject(output, level);
			output.close();
		} catch (FileNotFoundException e) {
			// oops!
		}
	}

	public static Object copyObject(Object object) {
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

    public static byte[] toBytes(Object object) {
        if(object == null) return null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Output output = new Output(stream);
            kryo.writeObject(output, object);
            output.close();
            return stream.toByteArray();
        }
        catch(Exception ex) {
            Gdx.app.log("Delver", ex.toString());
            return null;
        }
    }
}
