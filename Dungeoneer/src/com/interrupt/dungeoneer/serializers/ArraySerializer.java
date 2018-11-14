package com.interrupt.dungeoneer.serializers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ArraySerializer extends Serializer<Array> {
    {
    	setAcceptsNull(true);
    }

    private Class genericType;

    public void setGenerics (Kryo kryo, Class[] generics) {
        if (kryo.isFinal(generics[0])) genericType = generics[0];
    }

    public void write (Kryo kryo, Output output, Array array) {
        int length = array.size;
        output.writeInt(length, true);
        if (length == 0) return;
        if (genericType != null) {
            Serializer serializer = kryo.getSerializer(genericType);
            genericType = null;
            for (Object element : array)
                kryo.writeObjectOrNull(output, element, serializer);
        } else {
            for (Object element : array)
            	kryo.writeClassAndObject(output, element);
        }
    }

    public Array read (Kryo kryo, Input input, Class<Array> type) {
            Array array = new Array();
            kryo.reference(array);
            int length = input.readInt(true);
            array.ensureCapacity(length);
            if (genericType != null) {
                Class elementClass = genericType;
                Serializer serializer = kryo.getSerializer(genericType);
                genericType = null;
                for (int i = 0; i < length; i++) {
                    try {
                        array.add(kryo.readObjectOrNull(input, elementClass, serializer));
                    }
                    catch(Exception ex) {
                        Gdx.app.error("Serializer", ex.getMessage());
                        return array;
                    }
                }
            } else {
                for (int i = 0; i < length; i++) {
                    try {
                        array.add(kryo.readClassAndObject(input));
                    }
                    catch(Exception ex) {
                        Gdx.app.error("Serializer", ex.getMessage());
                        return array;
                    }
                }
            }
            return array;
    }
}
