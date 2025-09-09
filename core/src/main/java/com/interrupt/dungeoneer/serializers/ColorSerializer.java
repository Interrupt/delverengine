package com.interrupt.dungeoneer.serializers;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ColorSerializer extends Serializer<Color> {

	@Override
	public Color read(Kryo kryo, Input input, Class<Color> type) {
		Color color = new Color();
		Color.rgba8888ToColor(color, input.readInt());
		return color;
	}

	@Override
	public void write(Kryo kryo, Output output, Color object) {
		output.writeInt(Color.rgba8888(object));
	}
}
