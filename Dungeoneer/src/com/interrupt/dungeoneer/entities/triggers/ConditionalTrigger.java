package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

public class ConditionalTrigger extends Trigger {
	public enum CompareType {EQUAL, NOT_EQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL};

	/** Type of comparison to perform. */
	@EditorProperty
	public CompareType compareType = CompareType.EQUAL;

	/** Value to compare against. */
	@EditorProperty
	public String testValue = "";

	/** Treat {@link #triggerValue} as an integer and increment each time triggered.  */
	@EditorProperty
	public boolean incrementWhenTriggered = false;

	/** Entity to send trigger event when comparison fails. */
	@EditorProperty
	public String triggersOnFail = null;

	private int incrementValue = 0;
	private boolean didTrigger = false;
	
	public void doTriggerEvent(String value){

		if(incrementWhenTriggered) {
			incrementValue++;
			triggerValue = Integer.toString(incrementValue);
		}

		switch (compareType){
			case EQUAL:
				if (triggerValue.equals(testValue)) {
					passedCheck();
				}
				else {
					failedCheck();
				}
				break;
			case NOT_EQUAL:
				if (!triggerValue.equals(testValue)) {
					passedCheck();
				}
				else {
					failedCheck();
				}
				break;
			default:
				tryNumberCheck(triggerValue, compareType);
				break;
		}
	}
	
	// For other conditional types, try to parse the values to integers to check against
	private void tryNumberCheck(String value, CompareType conditional) {
		try {
			Integer val = Integer.parseInt(value);
			Integer testVal = Integer.parseInt(testValue);
			
			switch(conditional) {
				case LESS:
					if (val<testVal) {
						passedCheck();
					}
					else {
						failedCheck();
					}
					break;
				case GREATER:
					if (val>testVal) {
						passedCheck();
					}
					else {
						failedCheck();
					}
					break;
				case LESS_EQUAL:
					if (val<=testVal) {
						passedCheck();
					}
					else {
						failedCheck();
					}
					break;
				case GREATER_EQUAL:
					if (val>=testVal) {
						passedCheck();
					}
					else {
						failedCheck();
					}
					break;
			}
			
		} catch (Exception ex) {
			// not a number :(
		}
	}

	private void passedCheck() {
		didTrigger = true;
		super.doTriggerEvent(triggerValue);
	}

	private void failedCheck() {
		if(triggersOnFail != null && !triggersOnFail.isEmpty())
			Game.instance.level.trigger(this, triggersOnFail, triggerValue);
	}

	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);

		if(!didTrigger && triggerStatus == TriggerStatus.DESTROYED) {
			// Don't die yet, it's not your time!
			triggerStatus = TriggerStatus.WAITING;
			isActive = true;
		}
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersOnFail = makeUniqueIdentifier(triggersOnFail, idPrefix);
	}
}
