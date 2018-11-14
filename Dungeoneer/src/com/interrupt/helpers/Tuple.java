package com.interrupt.helpers;

public class Tuple<V> {
	
	public V val1;
	public V val2;
	
	public Tuple() { }
	
	
	public Tuple(V val) {
		val1 = val;
		val2 = val;
	}
	
	public Tuple(V val1, V val2) {
		this.val1 = val1;
		this.val2 = val2;
	}
	
	public void set(V val1, V val2) {
		this.val1 = val1;
		this.val2 = val2;
	}
	
	public Tuple<V> reverse() {
		V swap = val1;
		val1 = val2;
		val2 = swap;
		
		return this;
	}
}
