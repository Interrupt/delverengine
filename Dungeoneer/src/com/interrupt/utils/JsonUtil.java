package com.interrupt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.function.Supplier;

/** Helper class for working with JSON. */
public class JsonUtil {
    /** Serializes the given object to the given file. */
    public static void toJson(Object object, FileHandle file) {
        if (file == null) {
            log("Serialization", "Attempting to write to a null FileHandle object.");
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

            contents = contents.replace("\t", "    ");
        }
        catch (Exception ignored) {
            log("Serialization", "Failed to serialize object.");
        }

        return contents;
    }

    /** Deserializes given file. */
    public static <T> T fromJson(Class<T> type, FileHandle file) {
        return fromJson(type, file.readString("UTF-8"));
    }

    /** Deserializes the given file. A Supplier object will be used if the result is null. */
    public static <T> T fromJson(Class<T> type, FileHandle file, Supplier<T> defaultValueSupplier) {
        T result = null;

        try {
            result = fromJson(type, file);
        }
        catch (Exception ignored) { }

        if (result == null) {
            log("Serialization", String.format("Warning: Failed to deserialize file: \"%s\". Using default value.", file.toString()));
            result = defaultValueSupplier.get();
        }

        return result;
    }

    /** Deserialize an object from JSON. */
    public static <T> T fromJson(Class<T> type, String json) {
        Json json_ = getJson();
        T result;

        try {
            result = json_.fromJson(type, json);
        }
        catch (Exception ex) {
            log("Serialization", String.format("Error: Failed to deserialize JSON: \"%s\"", json));
            throw ex;
        }

        return result;
    }

    /** Returns a correctly configured Json object. */
    private static Json getJson() {
        Json json = new Json() {
            @Override
            protected boolean ignoreUnknownField(Class type, String fieldName) {
                if (!fieldName.equals("$schema")) {
                    log("Serialization", String.format("Warning: Unknown field: %s for class: %s", fieldName, type));
                }

                return true;
            }
        };
        json.setOutputType(JsonWriter.OutputType.json);

        return json;
    }

    private static void log(String tag, String message) {
        if (Gdx.app == null) {
            System.out.println("[" + tag + "] " + message);
            return;
        }

        Gdx.app.log(tag, message);
    }
}
