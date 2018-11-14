package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;

public class ProgressionTrigger extends Trigger {
	
	@EditorProperty
	public String progressionKey = "TO_CHECK";

	@EditorProperty
	public String newProgressionValue = "did_trigger";

	@EditorProperty
	public String checkProgressionKey = null;

	@EditorProperty
	public String checkProgressionValue = null;

	@EditorProperty
	public ProgressionType progressionType = ProgressionType.ONCE;

	@EditorProperty
	public String triggersOnFail = null;

	@EditorProperty
	public ProgressionPersistance persistance = ProgressionPersistance.FOREVER;

	public enum ProgressionType { ONCE, UNLIMITED }

	public enum ProgressionPersistance { FOREVER, UNTIL_DEATH }

	@Override
	public void doTriggerEvent(String value) {

		if(progressionKey != null) {
			String pv = getMyProgressionValue();

			if(progressionType == ProgressionType.ONCE) {
				if (pv == null || pv.isEmpty()) {
					if(checkProgressionValue()) {
						super.doTriggerEvent(value);
						updateProgressionValue();
					}
					else {
						triggerOnFail();
					}
				}
			}
			else  {
				if(checkProgressionValue()) {
					super.doTriggerEvent(value);
					updateProgressionValue();
				}
				else {
					triggerOnFail();
				}
			}
		}
	}

	public void triggerOnFail() {
		if(triggersOnFail != null && !triggersOnFail.isEmpty())
			Game.instance.level.trigger(this, triggersOnFail, null);
	}

	public boolean checkProgressionValue() {
		// Make sure this can actually trigger
		if (checkProgressionValue == null || checkProgressionValue.isEmpty()) {
			return true;
		}

		if (checkProgressionKey == null || checkProgressionKey.isEmpty()) {
			return true;
		}

		String pv = getOtherProgressionValue();

		return pv != null && pv.equals(checkProgressionValue);

	}

	private ArrayMap<String, String> getProgressionStorage() {
		if(persistance == ProgressionPersistance.FOREVER) {
			return Game.instance.progression.progressionTriggers;
		}
		else {
			return Game.instance.progression.untilDeathProgressionTriggers;
		}
	}

	private String getProgressionValue(String key) {
		// check forever progression first
		String value = Game.instance.progression.progressionTriggers.get(key);
		if(value != null && !value.isEmpty()) {
			return value;
		}

		// fall back to transient progression
		return Game.instance.progression.untilDeathProgressionTriggers.get(key);
	}

	private String getMyProgressionValue() {
		return getProgressionValue(progressionKey);
	}

	private String getOtherProgressionValue() {
		return getProgressionValue(checkProgressionKey);
	}

	private void updateProgressionValue() {
		getProgressionStorage().put(progressionKey, newProgressionValue);
	}
}
