package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Model;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.triggers.Trigger.TriggerStatus;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.ReadableKeys;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class ButtonModel extends Model {
	/** Entity to send trigger event when triggered. */
	@EditorProperty( group = "Trigger" )
	public String triggersId = "";

	/** Reset after being triggered? */
	@EditorProperty( group = "Trigger" )
	public boolean triggerResets = true;

	/** Time to wait before performing trigger action after receiving trigger event. */
	@EditorProperty( group = "Trigger" )
	public float triggerDelay = 0f;

	/** Time to wait to reset after performing trigger action. */
	@EditorProperty( group = "Trigger" )
	public float triggerResetTime = 20f;

	/** Trigger event value. */
	@EditorProperty( group = "Trigger" )
	public String triggerValue = "";

	/** Pass trigger event value to targeted trigger? */
	@EditorProperty( group = "Trigger" )
	public boolean triggerPropogates = true;

	/** Text to show for interaction prompt. */
	@EditorProperty( group = "Trigger" )
	public String useVerb = StringManager.get("triggers.ButtonModel.useVerbText");

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

	/** Triggered animation position component. */
	@EditorProperty
	public Vector3 triggeredTransformation = new Vector3(0,0,-0.05f);

	/** Triggered animation rotation component. */
	@EditorProperty
	public Vector3 triggeredRotation = new Vector3(0,0,20);

	public Vector3 startRotation = null;

	/** Triggered animation time. */
	@EditorProperty
	public float triggerAnimationTime = 20f;
	
	protected TriggerStatus triggerStatus=TriggerStatus.WAITING;
	private float triggerTime = 0;
	
	private float animationTime = 0;
	private boolean animating = false;
	
	public ButtonModel() { meshFile = "meshes/obelisk.obj"; isSolid = true; }
	
	@Override
	public void init(Level level, Source source) {
		if(source == Source.LEVEL_START)
			startRotation = new Vector3(rotation);

		super.init(level, source);
	}
	
	@Override
	public void tick(Level level, float delta) {
		
		if(animating) {
			animationTime += delta;
			
			if(animationTime >= triggerAnimationTime) {
				animating = false;
				animationTime = triggerAnimationTime;
				fire(null);
			}
		}
		
		if (triggerStatus==TriggerStatus.RESETTING){
			triggerTime -= delta;
			
			if (triggerTime<=0){
				triggerStatus=TriggerStatus.WAITING;
				triggerTime=triggerDelay;
			}
			
			if(triggerResetTime > 0)
				animationTime = (triggerTime / triggerResetTime) * triggerAnimationTime;
			else
				animationTime = 0;
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
		
		if(Game.isMobile && Math.abs(Game.instance.player.x - x) < 0.8f && Math.abs(Game.instance.player.y - y) < 0.8f) {
			String useText = ReadableKeys.keyNames.get(Actions.keyBindings.get(Action.USE));
			if(Game.isMobile) useText = StringManager.get("triggers.ButtonModel.useMobileText");
			Game.ShowUseMessage(MessageFormat.format(StringManager.get("triggers.ButtonModel.useText"), useText, this.useVerb));
		}
	}

	@Override
	public void use(Player p, float projx, float projy) {
		if(animating == false && triggerStatus == TriggerStatus.WAITING) {
			animating = true;
			animationTime = 0;
		}
	}
	
	public void fire(String value) {
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
			// just update the value if one was given
			if(value != null && !value.equals(""))
				triggerValue=value;
		}
	}
	
	// triggers can be delayed, fire the actual trigger here
	public void doTriggerEvent(String value) {
		Audio.playPositionedSound(triggerSound, new Vector3((float)x,(float)y,(float)z), 0.8f, 11f);
		Game.instance.level.trigger(this, triggersId, triggerValue);
		if(message != null && !message.equals("")) Game.ShowMessage(message, messageTime, messageSize);
	}
	
	@Override
	public void updateDrawable() {
		super.updateDrawable();
		
		// animate
		float a = animationTime / triggerAnimationTime;
		if(a > 1) a = 0;
		
		drawable.drawOffset.set(
			Interpolation.fade.apply(0, triggeredTransformation.x, a),
			Interpolation.fade.apply(0, triggeredTransformation.y, a),
			Interpolation.fade.apply(0, triggeredTransformation.z, a));

		yOffset = drawable.drawOffset.z;
		
		if(startRotation != null) {
			rotation.set(
					Interpolation.fade.apply(0, triggeredRotation.x, a),
					Interpolation.fade.apply(0, triggeredRotation.y, a),
					Interpolation.fade.apply(0, triggeredRotation.z, a));
			
			rotation.add(startRotation);
		}
	}

	@Override
	public void rotate90() {
		super.rotate90();
		triggeredTransformation.rotate(Vector3.Z, 90);
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersId = makeUniqueIdentifier(triggersId, idPrefix);
	}
}
