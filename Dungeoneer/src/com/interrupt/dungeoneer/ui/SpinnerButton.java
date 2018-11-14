package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.input.Actions.Action;

public class SpinnerButton<T> extends TextButton{
	private Array<T> items;
	private int selectedIndex = 0;

	public SpinnerButton(Skin skin, Array<T> items) {
		super("", skin);
		addDefaultListener();

		this.items = items;
		setText(getSelectedTitle());
	}

	public SpinnerButton(Array<T> items, TextButtonStyle textButtonStyle) {
		super("", textButtonStyle);
		addDefaultListener();

		this.items = items;
		setText(getSelectedTitle());
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
	
	public T getValue(){
		return items.get(selectedIndex);
	}

	public void setValue(T value) {
		String checkValue = value.toString().toLowerCase();
		for(int i = 0; i < items.size; i++) {
			String v = items.get(i).toString().toLowerCase();
			if(v.equals(checkValue)) {
				selectedIndex = i;
			}
		}
		setText(getSelectedTitle());
	}

	public String getSelectedTitle() {
		if(items == null || items.size == 0) return "NONE";
		return items.get(selectedIndex).toString();
	}

	public void setValueNext(){
		selectedIndex++;
		selectedIndex %= items.size;
		setText(getSelectedTitle());
	}
}
