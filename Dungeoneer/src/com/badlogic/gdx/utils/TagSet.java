package com.badlogic.gdx.utils;

public class TagSet extends ObjectSet<String> {
    @Override
    public String toString() {
        return String.join(", ", this);
    }
}
