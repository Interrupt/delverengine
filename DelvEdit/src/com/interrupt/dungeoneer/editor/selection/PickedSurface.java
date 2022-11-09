package com.interrupt.dungeoneer.editor.selection;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.helpers.FloatTuple;
import com.interrupt.helpers.TileEdges;

public class PickedSurface {
    public TileEdges edge;
    public EditorApplication.TileSurface tileSurface;

    public boolean isPicked = false;
    public Vector3 position = new Vector3();

    public FloatTuple ceilingPoints = new FloatTuple();
    public FloatTuple floorPoints = new FloatTuple();
}
