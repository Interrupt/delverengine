package com.interrupt.dungeoneer.serializers;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.v1.LevelSerializer;

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
