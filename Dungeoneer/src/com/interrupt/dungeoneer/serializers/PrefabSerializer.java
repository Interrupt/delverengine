package com.interrupt.dungeoneer.serializers;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.interrupt.dungeoneer.entities.Prefab;

public class PrefabSerializer extends Serializer<Prefab> {

	@Override
	public Prefab read(Kryo kryo, Input input, Class<Prefab> prefab) {
		Prefab newPrefab = new Prefab();
		newPrefab.category = input.readString();
		newPrefab.name = input.readString();
		newPrefab.x = input.readFloat();
		newPrefab.y = input.readFloat();
		newPrefab.z = input.readFloat();
		newPrefab.rotation = new Vector3(input.readFloat(), input.readFloat(), input.readFloat());

        // TODO: Add the prefab chance back
        //newPrefab.spawnChance = input.readFloat();

		return newPrefab;
	}

	@Override
	public void write(Kryo kryo, Output output, Prefab prefab) {
		output.writeString(prefab.category);
		output.writeString(prefab.name);
		output.writeFloat(prefab.x);
		output.writeFloat(prefab.y);
		output.writeFloat(prefab.z);
		output.writeFloat(prefab.rotation.x);
		output.writeFloat(prefab.rotation.y);
		output.writeFloat(prefab.rotation.z);

        //output.writeFloat(prefab.spawnChance);
	}

}
