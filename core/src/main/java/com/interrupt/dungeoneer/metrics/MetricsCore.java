package com.interrupt.dungeoneer.metrics;

import java.util.HashMap;

public class MetricsCore {
	private static HashMap<String, Long> startTimes = new HashMap<String,Long>();
	private static HashMap<String, Long> totalTimes = new HashMap<String,Long>();
	private static HashMap<String, Long> counts = new HashMap<String,Long>();
	public static boolean enabled = false;
	
	// start the timer
	public static void startTimer(String name) {
		if(!enabled) return;

		startTimes.put(name, System.nanoTime());
	}
	
	// end the timer, return elapsed in milliseconds
	public static long endTimer(String name) {
		if(!enabled) return -1;
		
		Long curTime = System.nanoTime();
		Long startTime = startTimes.get(name);
		if(startTime == null) return -1;
		Long elapsed = curTime - startTime;
		
		// keep track of frame totals
		Long totalTime = totalTimes.get(name);
		if(totalTime != null) totalTime += elapsed;
		else totalTimes.put(name, elapsed);
		
		return elapsed;
	}
	
	public static void count(String name) {
		if(!enabled) return;
		
		Long cur = counts.get(name);
		if(cur == null) counts.put(name, 1l);
		else counts.put(name, cur + 1);
	}
	
	public static void frameReset() {
		if(!enabled) return;

		startTimes.clear();
		totalTimes.clear();
		counts.clear();
	}
	
	public static HashMap<String,Long> getTotals() {
		return totalTimes;
	}
	
	public static HashMap<String,Long> getCounts() {
		return counts;
	}
}
