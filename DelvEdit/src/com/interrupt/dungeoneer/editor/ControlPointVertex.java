package com.interrupt.dungeoneer.editor;

import com.interrupt.dungeoneer.tiles.Tile;

public class ControlPointVertex {
    public Tile tile;
    public ControlVertex vertex = ControlVertex.slopeNE;

    public enum ControlVertex { slopeNW, slopeNE, slopeSW, slopeSE, ceilNW, ceilNE, ceilSW, ceilSE }

    public ControlPointVertex(Tile tile, ControlVertex vertex) {
        this.tile = tile;
        this.vertex = vertex;
    }
}
