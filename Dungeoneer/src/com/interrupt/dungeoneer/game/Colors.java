package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.graphics.Color;

public class Colors {
    // DamageType enchantment effects
    public static Color FIRE = Color.RED;
    public static Color ICE = Color.BLUE;
    public static Color LIGHTNING = Color.WHITE;
    public static Color MAGIC = new Color(0.6172f, 0.0937f, 0.7695f, 1.0f);
    public static Color POISON = new Color(0.1529f, 1.0f, 0.3333f, 1.0f);
    public static Color VAMPIRE = new Color(0.13f, 0.1f, 0.15f, 1.0f);
    public static Color PARALYZE = new Color(0.9294f, 0.7882f, 0.1921f, 1.0f);
    public static Color MUNDANE = Color.LIGHT_GRAY;
    public static Color HEALING = Color.CYAN;

    // Blood
    public static Color DEFAULT_BLOOD = Color.RED;
    public static Color SLIME_BLOOD = Color.GREEN;
    public static Color INSECT_BLOOD = Color.YELLOW;
    public static Color BONE_BLOOD = Color.LIGHT_GRAY;

    // Screen flashes
    public static Color HEAL_FLASH = Color.GREEN;
    public static Color HURT_FLASH = Color.RED;

    // Scroll effects
    public static Color ENCHANT_ITEM = Color.GREEN;

    // UI
    public static Color PLAY_BUTTON = Color.GREEN;
    public static Color ERASE_BUTTON = Color.RED;
    public static Color DISCORD_BUTTON = new Color(88f / 255f, 101f / 255f, 242f / 255f, 1f);

    public static Color EXPLOSION = Color.ORANGE;
    public static Color EXPLOSION_LIGHT_END =  Color.BLACK;
    public static Color BOMB_FLASH = Color.WHITE;
}
