package com.interrupt.helpers;

public class FloatTuple {
	public float val1;
	public float val2;

	public FloatTuple() { }

	public FloatTuple(float val) {
		val1 = val;
		val2 = val;
	}

	public FloatTuple(float val1, float val2) {
		this.val1 = val1;
		this.val2 = val2;
	}

	public FloatTuple set(float val1, float val2) {
		this.val1 = val1;
		this.val2 = val2;
		return this;
	}

	public FloatTuple reverse() {
		float swap = val1;
		val1 = val2;
		val2 = swap;

		return this;
	}

    public void set(FloatTuple toCopy) {
        val1 = toCopy.val1;
        val2 = toCopy.val2;
    }

    public float average() {
        return (val1 + val2) / 2f;
    }

    public float max() {
        return Math.max(val1, val2);
    }

    public float min() {
        return Math.min(val1, val2);
    }

    public void add(float v) {
        val1 += v;
        val2 += v;
    }

    public void sub(float v) {
        val1 -= v;
        val2 -= v;
    }
}
