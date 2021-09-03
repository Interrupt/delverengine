package com.interrupt.dungeoneer.ui.elements;

import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;

public class PlayerAttributeValue extends DynamicValue {
    public enum PlayerAttribute {
        MIN_DAMAGE,
        MAX_DAMAGE,
        ARMOR_CLASS,
        HEALTH_STAT,
        ATTACK_STAT,
        MAGIC_STAT,
        DEFENSE_STAT,
        SPEED_STAT,
        AGILITY_STAT,
        HITPOINTS,
        MAX_HITPOINTS,
        LEVEL,
        XP,
        NEXT_XP,
        GOLD
    }

    public PlayerAttribute attribute;

    public PlayerAttributeValue() {
        super();
    }

    public PlayerAttributeValue(String value) {
        super(value);
    }

    @Override
    public String stringValue() {
        return String.valueOf(getAttribute());
    }

    @Override
    public int intValue() {
        return getAttribute();
    }

    @Override
    public float floatValue() {
        return (float) getAttribute();
    }

    @Override
    public boolean booleanValue() {
        return getAttribute() == 0;
    }

    private int getAttribute() {
        Player player = Game.instance.player;

        switch (attribute) {
            case MIN_DAMAGE:
                return player.getBaseDamage();

            case MAX_DAMAGE:
                return player.getMaxDamage();

            case ARMOR_CLASS:
                return player.getArmorClass();

            case HEALTH_STAT:
                return player.stats.END;

            case ATTACK_STAT:
                return player.stats.ATK;

            case MAGIC_STAT:
                return player.stats.MAG;

            case DEFENSE_STAT:
                return player.stats.DEF;

            case SPEED_STAT:
                return player.stats.SPD;

            case AGILITY_STAT:
                return player.stats.DEX;

            case HITPOINTS:
                return player.hp;

            case MAX_HITPOINTS:
                return player.maxHp;

            case LEVEL:
                return player.level;

            case XP:
                return player.exp;

            case NEXT_XP:
                return player.getNextLevel();

            case GOLD:
                return player.gold;
        }

        return 0;
    }
}
