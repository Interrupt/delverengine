package com.interrupt.dungeoneer.serializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class HashMapSerializer extends Serializer<HashMap> {
	
    private Class genericKeyType;
    private Class genericValueType;

    public void setGenerics (Kryo kryo, Class[] generics) {
        if (kryo.isFinal(generics[0])) genericKeyType = generics[0];
        genericValueType = generics[1];
    }

	@Override
	public HashMap read(Kryo kryo, Input input, Class<HashMap> type) {
		
		HashMap hashmap = new HashMap();
        kryo.reference(hashmap);
        int length = input.readInt(true);
        
        if (genericKeyType != null && genericValueType != null) {
            Class elementKeyClass = genericKeyType;
            Class elementValueClass = genericValueType;
            
            Serializer keySerializer = kryo.getSerializer(elementKeyClass);
            Serializer valueSerializer = kryo.getSerializer(elementValueClass);
            
            genericKeyType = null;
            genericValueType = null;
            for (int i = 0; i < length; i++) {
                hashmap.put(kryo.readObjectOrNull(input, elementKeyClass, keySerializer),
                		kryo.readObjectOrNull(input, elementValueClass, valueSerializer));
            }
        } else {
            for (int i = 0; i < length; i++) {
            	hashmap.put(kryo.readClassAndObject(input), kryo.readClassAndObject(input));
            }
        }
        
		return hashmap;
	}

	@Override
	public void write(Kryo kryo, Output output, HashMap hashmap) {
		int length = hashmap.size();
		output.writeInt(length, true);
		if (length == 0) return;
		
		if (genericKeyType != null && genericValueType != null) {
            Serializer keySerializer = kryo.getSerializer(genericKeyType);
            Serializer valueSerializer = kryo.getSerializer(genericValueType);
            genericKeyType = null;
            genericValueType = null;
            for (Object key : hashmap.keySet()) {
                kryo.writeObjectOrNull(output, key, keySerializer);
                kryo.writeObjectOrNull(output, hashmap.get(key), valueSerializer);
            }
        } else {
            for (Object key : hashmap.keySet()) {
            	kryo.writeClassAndObject(output, key);
            	kryo.writeClassAndObject(output, hashmap.get(key));
            }
        }
	}
}
