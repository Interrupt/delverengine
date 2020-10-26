package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.ReadableKeys;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.helpers.PlayerHistory;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class Trigger extends Entity {
	/** Does this count as a secret? */
	@EditorProperty( group = "Trigger" )
	public boolean isSecret = false;

	public enum TriggerStatus {WAITING, TRIGGERED, RESETTING, DESTROYED}
	public enum TriggerType {USE, PLAYER_TOUCHED, ACTOR_TOUCHED, ANY_TOUCHED}
	public enum GameTime {WHENEVER, DESCENT, ESCAPE}

	/** Kind of action that causes trigger. */
	@EditorProperty( group = "Trigger" )
	public TriggerType triggerType = TriggerType.USE;

	/** Entity to send trigger event when triggered. */
	@EditorProperty( group = "Trigger" )
	public String triggersId = "";

	/** Reset after being triggered? */
	@EditorProperty( group = "Trigger" )
	public boolean triggerResets = true;

	/** Only allow trigger events from Entities with specified id. */
    @EditorProperty( group = "Trigger" )
    public String onlyTriggeredById = null;

    /** Time to wait before performing trigger action after receiving trigger event. */
	@EditorProperty( group = "Trigger" )
	public float triggerDelay = 0f;

	/** Time to wait to reset after performing trigger action. */
	@EditorProperty( group = "Trigger" )
	public float triggerResetTime = 0f;

	/** Trigger event value. */
	@EditorProperty( group = "Trigger" )
	public String triggerValue = "";

	/** Pass trigger event value to targeted trigger? */
	@EditorProperty( group = "Trigger" )
	public boolean triggerPropogates = true;

	/** Text to show for interaction prompt. */
	@EditorProperty( group = "Trigger" )
	public String useVerb = StringManager.get("entities.Trigger.defaultUseVerb");

	/** Message to display when triggered. */
	@EditorProperty( group = "Trigger" )
	public String message = "";

	/** Duration to display message in seconds. */
	@EditorProperty( group = "Trigger" )
	public float messageTime = 5f;

	/** Size of displayed message. */
	@EditorProperty( group = "Trigger" )
	public float messageSize = 1f;

	/** Filepath of sound to play when triggered. */
	@EditorProperty( group = "Trigger" )
	public String triggerSound = null;

	/** Which phase of the game to permit triggering. */
	@EditorProperty( group = "Trigger" )
	public GameTime triggersDuring = GameTime.WHENEVER;

	/** Does this appear during end game? */
	@EditorProperty( group = "End Game" )
	public boolean appearsDuringEndgame = true;
	
	protected boolean selfDestructs = true;
	
	protected TriggerStatus triggerStatus=TriggerStatus.WAITING;
	private float triggerTime = 0;
	
	public Trigger() {
		hidden = true; spriteAtlas = "editor"; tex = 11;
	}
	
	public Trigger(String triggers) {
		this.artType = ArtType.hidden;
		this.triggersId = triggers;
	}
	
	public Trigger(String triggers, float delay) {
		this.artType = ArtType.hidden;
		this.triggersId = triggers;
		this.triggerDelay = delay;
	}

	@Override
	public void init(Level level, Level.Source source) {
		if(Game.instance != null && Game.instance.player != null) {
			// Might need to despawn this during the endgame
			if(Game.instance.player.isHoldingOrb) {
				if(!appearsDuringEndgame) {
					isActive = false;
				}
			}
		}
	}
	
	@Override
	public void tick(Level level, float delta) {
		
		// check for touch events
		if(triggerType != TriggerType.USE) {
            Array<Entity> encroaching = level.getEntitiesColliding(x, y, z, this);
            for (int i = 0; i < encroaching.size; i++) {
                Entity touching = encroaching.get(i);

                // skip entities that don't match the entity ID filter
                if(onlyTriggeredById != null && !onlyTriggeredById.isEmpty()) {
                    if(touching.id == null || !onlyTriggeredById.equals(touching.id)) continue;
                }

                if (touching instanceof Player && triggerType == TriggerType.PLAYER_TOUCHED) fire(null);
                else if (touching instanceof Actor && triggerType == TriggerType.ACTOR_TOUCHED) fire(null);
                else if (triggerType == TriggerType.ANY_TOUCHED) fire(null);
            }
		}
		
		if (triggerStatus==TriggerStatus.DESTROYED && selfDestructs){
			this.isActive=false;
		}
		if (triggerStatus==TriggerStatus.RESETTING){
			triggerTime-=delta;
			if (triggerTime<=0){
				triggerStatus=TriggerStatus.WAITING;
				triggerTime=triggerDelay;
			}
		}
		if (triggerStatus==TriggerStatus.TRIGGERED){
			triggerTime-=delta;
			if (triggerTime<=0){
				doTriggerEvent(triggerValue); // fire!
				if (triggerResets){
					triggerStatus=TriggerStatus.RESETTING;
					triggerTime=triggerResetTime;
				} else {
					triggerStatus=TriggerStatus.DESTROYED;
				}
			}
		}
		
		if(Game.isMobile && triggerType == TriggerType.USE && Math.abs(Game.instance.player.x - x) < 0.8f && Math.abs(Game.instance.player.y - y) < 0.8f) {
			String useText = ReadableKeys.keyNames.get(Actions.keyBindings.get(Action.USE));
			if(Game.isMobile) useText = StringManager.get("entities.Trigger.mobileUseText");
			Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Trigger.mobileUseText"), useText, this.getUseVerb()));
		}
	}
	
	@Override
	public void use(Player p, float projx, float projy) {
		fire(null);
	}

	public String getUseVerb() {
		String useVerbLocalized = StringManager.get("triggers.Trigger.useVerbs." + useVerb);
		if (useVerbLocalized.startsWith("triggers.")) {
			useVerbLocalized = useVerb;
		}

		return useVerbLocalized;
	}

	public void fire(String value) {

		// Check if we can actually fire now
		if(triggersDuring != GameTime.WHENEVER) {
			if(Game.instance != null && Game.instance.player != null) {
				boolean endgame = Game.instance.player.isHoldingOrb;
				if(triggersDuring == GameTime.DESCENT && endgame) {
					return;
				}
				else if(triggersDuring == GameTime.ESCAPE && !endgame) {
					return;
				}
			}
		}

		// Track secrets
		if(isSecret) {
			isSecret = false;
			Game.instance.player.history.foundSecret();
		}

		// Triggering an already triggered trigger will do nothing
		if (triggerStatus==TriggerStatus.WAITING){
			triggerStatus=TriggerStatus.TRIGGERED;
			triggerTime=triggerDelay;
			
			// update the value if one was given
			if(value != null && !value.equals(""))
				triggerValue=value;
		}
	}
	
	@Override
	public void onTrigger(Entity instigator, String value) {
		if(triggerPropogates) {
			fire(value);
		}
		else {
			fire(triggerValue);
		}
	}
	
	// triggers can be delayed, fire the actual trigger here
	public void doTriggerEvent(String value) {
		Audio.playPositionedSound(triggerSound, new Vector3((float)x,(float)y,(float)z), 0.8f, 11f);
		Game.instance.level.trigger(this, triggersId, triggerValue);
		if(message != null && !message.equals("")) Game.ShowMessage(message, messageTime, messageSize);
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersId = makeUniqueIdentifier(triggersId, idPrefix);
	}

	public TriggerStatus getTriggerStatus() {
		return triggerStatus;
	}
}
