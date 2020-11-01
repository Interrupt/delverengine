package com.interrupt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.function.Supplier;

/** Helper class for working with JSON. */
public class JsonUtil {
    /** Serializes the given object to the specified path. */
    public static void toJson(String path, Object object) {
        FileHandle file = Gdx.files.local(path);
        toJson(file, object);
    }

    /** Serializes the given object to the given file. */
    public static void toJson(FileHandle file, Object object) {
        if (file == null) {
            Gdx.app.log("JsonUtil", "Attempting to write to a null FileHandle objct.");
            return;
        }

        file.writeString(toJson(object), false, "UTF-8");
    }

    /** Serializes an object to a standardized JSON format. */
    public static String toJson(Object object) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        String contents = "{}";

        try {
            contents = json.prettyPrint(json.toJson(object), 40);
        }
        catch (Exception ignored) {
            Gdx.app.log("JsonUtil", "Failed to serialize object.");
        }

        return contents;
    }

    /** Deserializes given file. */
    public static <T> T fromJson(Class<T> type, FileHandle file) {
        return fromJson(type, file.readString());
    }

    /** Deserializes the given file. A Supplier object will be used if the result is null. */
    public static <T> T fromJson(Class<T> type, FileHandle file, Supplier<T> defaultValueSupplier) {
        T result = null;

        try {
            result = fromJson(type, file);
        }
        catch (Exception ignored) { }

        if (result == null) {
            Gdx.app.log("JsonUtil", "Failed to deserialize object. Using default value.");
            result = defaultValueSupplier.get();
        }

        return result;
    }

    /** Deserialize an object from JSON. */
    public static <T> T fromJson(Class<T> type, String json) {
        Json json_ = new Json();
        return json_.fromJson(type, json);
    }
}
