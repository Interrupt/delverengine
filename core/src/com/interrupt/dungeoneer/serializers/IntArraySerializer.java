package com.interrupt.dungeoneer.serializers;

import com.badlogic.gdx.utils.IntArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class IntArraySerializer extends Serializer<IntArray> {
	{
        setAcceptsNull(true);
	}

	public void write (Kryo kryo, Output output, IntArray array) {
	        int length = array.size;
	        output.writeInt(length, true);
	        if (length == 0) return;
	        for (int i = 0, n = array.size; i < n; i++)
	                output.writeInt(array.get(i), true);
	}
	
	public IntArray read (Kryo kryo, Input input, Class<IntArray> type) {
	        IntArray array = new IntArray();
	        kryo.reference(array);
	        int length = input.readInt(true);
	        array.ensureCapacity(length);
	        for (int i = 0; i < length; i++)
	                array.add(input.readInt(true));
	        return array;
	}
}
