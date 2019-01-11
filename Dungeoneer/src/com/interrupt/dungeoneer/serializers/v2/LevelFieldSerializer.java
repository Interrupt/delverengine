package com.interrupt.dungeoneer.serializers.v2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.TileMaterials;

public class LevelFieldSerializer extends CompatibleFieldSerializer<Level> {
    public LevelFieldSerializer(Kryo kryo, Class type) {
        super(kryo, type);
    }

    @Override
    public Level read(Kryo kryo, Input input, Class<Level> levelClass) {
        Level level = super.read(kryo, input, levelClass);

        // Handle different level versions
        if(level.version >= 2) {
            // Version 2 has tile materials, populate the tile materials into the right spot
            if(level.savedTileMaterials != null && level.savedTileMaterials.length == level.width * level.height) {
                for (int x = 0; x < level.width; x++) {
                    for (int y = 0; y < level.height; y++) {
                        Tile t = level.getTileOrNull(x, y);
                        if (t != null) {
                            t.materials = level.savedTileMaterials[x + y * level.width];
                        }
                    }
                }
            }
        }

        // Version has been upgraded now
        level.version = Level.CURRENT_VERSION;

        return level;
    }

    @Override
    public void write(Kryo kryo, Output output, Level level) {
        if(level != null) {
            if (level.version >= 2) {
                // Version 2 has tile materials that need persisting
                if (level.savedTileMaterials == null)
                    level.savedTileMaterials = new TileMaterials[level.width * level.height];

                for (int x = 0; x < level.width; x++) {
                    for (int y = 0; y < level.height; y++) {
                        Tile t = level.getTileOrNull(x, y);
                        if (t != null && t.materials != null) {
                            level.savedTileMaterials[x + y * level.width] = t.materials;
                        }
                    }
                }
            }

            // Make sure the current version is stored
            level.version = Level.CURRENT_VERSION;
        }

        super.write(kryo, output, level);
    }
}
