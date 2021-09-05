package com.interrupt.dungeoneer.ui.values;

public class DynamicValue {
    private String value;

    private boolean isDirty = true;

    private int intValue;
    private float floatValue;
    private boolean booleanValue;

    public DynamicValue() {}

    public DynamicValue(String value) {
        this.value = value;
    }

    public String stringValue() {
        return value;
    }

    public int intValue() {
        validate();
        return intValue;
    }

    public float floatValue() {
        validate();
        return floatValue;
    }

    public boolean booleanValue() {
        validate();
        return booleanValue;
    }

    private void validate() {
        if (!isDirty) return;
        isDirty = false;

        intValue = Integer.parseInt(value);
        floatValue = Float.parseFloat(value);
        booleanValue = Boolean.parseBoolean(value);
    }
}
