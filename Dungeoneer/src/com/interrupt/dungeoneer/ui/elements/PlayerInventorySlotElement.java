package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.ui.InventorySlot;

public class PlayerInventorySlotElement extends Element {
    public String image;
    public int index;

    @Override
    protected Actor createActor() {
        return new InventorySlot(index, image);
    }
}
