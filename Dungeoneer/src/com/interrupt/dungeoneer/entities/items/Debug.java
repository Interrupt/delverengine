package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.overlays.DebugOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class Debug extends Item {
	public Debug() {
		tex = 56;
		name = "YORIC";
		isSolid = true; yOffset = -0.1f;
	}

	public boolean inventoryUse(Player player){
		OverlayManager.instance.push(new DebugOverlay(player));
        return true;
	}
}
