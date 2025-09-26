package com.interrupt.helpers;

import com.interrupt.dungeoneer.entities.Item;

public class ShopItem {
	public Item item = null;
	public Upgrade upgrade = null;
	public Integer cost = 10;
	public boolean useOnBuy = false;
	public String achieveOnBuy = null;
	
	public ShopItem() { }
	public ShopItem(Item item) { this.item = item; if(item != null) this.cost = item.cost; }
	public ShopItem(Item item, String achievement) { this.item = item; if(item != null) this.cost = item.cost; this.achieveOnBuy = achievement; }
	public ShopItem(Item item, boolean useOnBuy) { this.item = item; if(item != null) this.cost = item.cost; this.useOnBuy = useOnBuy; }
	public ShopItem(Item item, boolean useOnBuy, String achievement) { this.item = item; if(item != null) this.cost = item.cost; this.useOnBuy = useOnBuy; this.achieveOnBuy = achievement; }
	public ShopItem(Item item, Integer cost) { this.item = item; this.cost = cost; }
	public ShopItem(Upgrade upgrade, Integer cost) { this.upgrade = upgrade; this.cost = cost; }
	
	public String getName() {
		if(item != null) return item.GetName();
		if(upgrade != null) return upgrade.name;
		return "NOTHING";
	}

    public CharSequence getDescription() {
		if(item != null) return item.GetInfoText();
		return "";
    }
}
