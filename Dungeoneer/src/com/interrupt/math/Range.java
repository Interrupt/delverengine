package com.interrupt.math;

import java.io.Serializable;

public class Range implements Serializable {
    private static final long serialVersionUID = -5294586091312091415L;

    private float min;
    private float max;

    public Range(float min, float max) {
        set(min, max);
    }

    public Range(float value) {
        set(value);
    }

    public void set(float min, float max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public void set(float value) {
        this.min = value;
        this.max = value;
    }

    public boolean intersects(Range other) {
        if (other.min > max) return false;
        if (other.max < min) return false;

        return true;
    }

    public boolean contains(float value) {
        if (value < min) return false;
        if (value > max) return false;

        return true;
    }

    public void extend(float value) {
        min = Math.min(value, min);
        max = Math.max(value, max);
    }
}
