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

    public DynamicValue(int value) {
        this.value = String.valueOf(value);
    }

    public DynamicValue(float value) {
        this.value = String.valueOf(value);
    }

    public DynamicValue(boolean value) {
        this.value = String.valueOf(value);
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

    public void setValue(int value) {
        this.value = String.valueOf(value);
        isDirty = true;
    }

    public void setValue(float value) {
        this.value = String.valueOf(value);
        isDirty = true;
    }

    public void setValue(String value) {
        this.value = value;
        isDirty = true;
    }

    public void setValue(boolean value) {
        this.value = String.valueOf(value);
        isDirty = true;
    }

    private void validate() {
        if (!isDirty) return;
        isDirty = false;

        try {
            intValue = Integer.parseInt(value);
        }
        catch (Exception ignored) {
            intValue = 0;
        }

        try {
            floatValue = Float.parseFloat(value);
        }
        catch (Exception ignored) {
            floatValue = 0f;
        }

        try {
            booleanValue = Boolean.parseBoolean(value);
        }
        catch (Exception ignored) {
            booleanValue = false;
        }
    }
}
