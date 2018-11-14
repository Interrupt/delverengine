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
}
