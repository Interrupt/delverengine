package com.badlogic.gdx.utils;

import java.util.HashSet;

public class TagSet extends HashSet<String> {
    @Override
    public String toString() {
        return String.join(", ", this);
    }
}
