package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.interrupt.dungeoneer.input.Actions.Action;

public class ActionSpinnerButton extends TextButton{
	private Action buttonAction=Action.ATTACK;
	
	public ActionSpinnerButton(Action action, Skin skin) {
		super(action.toString(), skin);
		setValue(action);
		addDefaultListener();
	}
	
	public ActionSpinnerButton(Action action,TextButtonStyle textButtonStyle) {
		super(action==null?"NONE":action.toString(), textButtonStyle);
		setValue(action);
		addDefaultListener();
	}
	
	private void addDefaultListener(){
		addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				setValueNext();
			}
		});
	}
	
	public float getPrefWidth () {
		float width = super.getPrefWidth();
		
		//Check parent
		if (getStyle().up != null) width = Math.max(width, getStyle().up.getMinWidth());
		if (getStyle().down != null) width = Math.max(width, getStyle().down.getMinWidth());
		if (getStyle().checked != null) width = Math.max(width, getStyle().checked.getMinWidth());
		
		//Check possible text
		for (Action action:Action.values()) {
			width = Math.max(width, FontBounds.GetBounds(this.getStyle().font, action.toString()).width);
		}
		
		return width;
	}
	
	public Action getValue(){
		return buttonAction;
	}

	public void setValue(Action action){
		buttonAction=action;
		if (action==null) {
			setText("NONE");
		} else {
			setText(action.toString());
		}
	}

	public void setValueNext(){
		if (buttonAction==null) {
			setValue(Action.ATTACK);
		} else {
			switch (buttonAction) {
				case ATTACK:
					setValue(Action.USE);
					break;
				case USE:
					setValue(Action.DROP);
					break;
				case DROP:
					setValue(Action.INVENTORY);
					break;
				case INVENTORY:
					setValue(Action.MAP);
					break;
				case MAP:
					setValue(null);
					break;
				default:
					setValue(Action.ATTACK);
					break;
		}
		}
	}
}
