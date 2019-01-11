package com.interrupt.dungeoneer.tiles;


import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.helpers.TileEdges;

public class TileMaterials {
    private ArrayMap<TileEdges, TileSurface> topSurfaces = new ArrayMap<TileEdges, TileSurface>();
    private ArrayMap<TileEdges, TileSurface> bottomSurfaces = new ArrayMap<TileEdges, TileSurface>();

    public TileMaterials() { }

    public void rotate90() {
        TileSurface topNorth = topSurfaces.get(TileEdges.North);
        TileSurface topEast = topSurfaces.get(TileEdges.East);
        TileSurface topSouth = topSurfaces.get(TileEdges.South);
        TileSurface topWest = topSurfaces.get(TileEdges.West);

        TileSurface botNorth = bottomSurfaces.get(TileEdges.North);
        TileSurface botEast = bottomSurfaces.get(TileEdges.East);
        TileSurface botSouth = bottomSurfaces.get(TileEdges.South);
        TileSurface botWest = bottomSurfaces.get(TileEdges.West);

        topSurfaces.put(TileEdges.North, topEast);
        topSurfaces.put(TileEdges.East, topSouth);
        topSurfaces.put(TileEdges.South, topWest);
        topSurfaces.put(TileEdges.West, topNorth);

        bottomSurfaces.put(TileEdges.North, botEast);
        bottomSurfaces.put(TileEdges.East, botSouth);
        bottomSurfaces.put(TileEdges.South, botWest);
        bottomSurfaces.put(TileEdges.West, botNorth);
    }

    public TileSurface getTopSurface(TileEdges edge) {
        return topSurfaces.get(edge);
    }

    public TileSurface getBottomSurface(TileEdges edge) {
        return bottomSurfaces.get(edge);
    }

    public void setTopSurface(TileEdges dir, TileSurface s) {
        topSurfaces.put(dir, s);
    }

    public void setBottomSurface(TileEdges dir, TileSurface s) {
        bottomSurfaces.put(dir, s);
    }
}
