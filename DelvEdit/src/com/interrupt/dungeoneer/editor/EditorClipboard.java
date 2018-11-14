package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.tiles.Tile;

public class EditorClipboard {
    public Array<Entity> entities = new Array<Entity>();
    public Tile tiles[][] = null;
    public Vector3 offset = new Vector3();
    public int selWidth = 1;
    public int selHeight = 1;
}
