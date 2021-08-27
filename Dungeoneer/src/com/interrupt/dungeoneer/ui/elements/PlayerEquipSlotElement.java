package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.ui.EquipSlot;

public class PlayerEquipSlotElement extends Element {
    public String image;
    public String equipLoc = "ARMOR";

    @Override
    protected Actor createActor() {
        return new EquipSlot(image, equipLoc);
    }
}
