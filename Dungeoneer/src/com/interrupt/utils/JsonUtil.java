package com.interrupt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.function.Supplier;

/** Helper class for working with JSON. */
public class JsonUtil {
    /** Serializes the given object to the specified path. */
    public static void toJson(Object object, String path) {
        FileHandle file = Gdx.files.local(path);
        toJson(object, file);
    }

    /** Serializes the given object to the given file. */
    public static void toJson(Object object, FileHandle file) {
        if (file == null) {
            Gdx.app.log("DelverSerialization", "Attempting to write to a null FileHandle objct.");
            return;
        }

        file.writeString(toJson(object), false, "UTF-8");
    }

    /** Serializes an object to a standardized JSON format. */
    public static String toJson(Object object) {
        return toJson(object, (Class<?>)null);
    }

    /** Serializes an object to a standardized JSON format using a given type. */
    public static String toJson(Object object, Class<?> type) {
        Json json = getJson();

        String contents = "{}";
        int singleLineColumns = 40;

        try {
            if (type != null) {
                contents = json.prettyPrint(json.toJson(object, type), singleLineColumns);
            }
            else {
                contents = json.prettyPrint(json.toJson(object), singleLineColumns);
            }
        }
        catch (Exception ignored) {
            Gdx.app.log("DelverSerialization", "Failed to serialize object.");
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
            Gdx.app.log("DelverSerialization", "Failed to deserialize object. Using default value.");
            result = defaultValueSupplier.get();
        }

        return result;
    }

    /** Deserialize an object from JSON. */
    public static <T> T fromJson(Class<T> type, String json) {
        Json json_ = getJson();
        return json_.fromJson(type, json);
    }

    private static Json getJson() {
        Json json = new Json() {
            @Override
            protected boolean ignoreUnknownField(Class type, String fieldName) {
                Gdx.app.log("DelverSerialization", String.format("Unknown field: %s for class: %s", fieldName, type));

                return true;
            }
        };
        json.setOutputType(JsonWriter.OutputType.json);

        return json;
    }
}
