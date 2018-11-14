package com.interrupt.dungeoneer.serializers;

import com.badlogic.gdx.utils.Array.ArrayIterator;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class LibGdxArrayIteratorSerializer extends Serializer<ArrayIterator> {

	@Override
	public ArrayIterator read(Kryo kryo, Input arg1, Class<ArrayIterator> type) {
		return null;
	}

	@Override
	public void write(Kryo kryo, Output output, ArrayIterator obj) {	
	}

}
