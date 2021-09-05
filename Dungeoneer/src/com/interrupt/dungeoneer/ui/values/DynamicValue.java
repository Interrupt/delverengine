package com.interrupt.dungeoneer.ui.values;

public class DynamicValue {
    private String value;

    public DynamicValue() {}

    public DynamicValue(String value) {
        this.value = value;
    }

    public String stringValue() {
        return value;
    }

    public int intValue() {
        return Integer.parseInt(value);
    }

    public float floatValue() {
        return Float.parseFloat(value);
    }

    public boolean booleanValue() {
        return Boolean.parseBoolean(value);
    }
}
