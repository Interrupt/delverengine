package com.interrupt.dungeoneer.gfx;

public class TesselatorGroups {
    public TesselatorGroup world;
    public TesselatorGroup water;
    public TesselatorGroup waterfall;
    public TesselatorGroup waterEdges;
    public TesselatorGroup waterfallEdges;

    public TesselatorGroups() {
        world = new TesselatorGroup(GlRenderer.worldShaderInfo);
        water = new TesselatorGroup(GlRenderer.waterShaderInfo);
        waterfall = new TesselatorGroup(GlRenderer.waterShaderInfo);
        waterEdges = new TesselatorGroup(GlRenderer.waterEdgeShaderInfo);
        waterfallEdges = new TesselatorGroup(GlRenderer.waterEdgeShaderInfo);
    }

    public void clear() {
        world.clear();
        water.clear();
        waterfall.clear();
        waterEdges.clear();
        waterfallEdges.clear();
    }

    public void build() {
        world.build();
        water.build();
        waterfall.build();
        waterEdges.build();
        waterfallEdges.build();
    }

    public void refresh() {
        world.refresh();
        water.refresh();
        waterfall.refresh();
        waterEdges.refresh();
        waterfallEdges.refresh();
    }

    public boolean isEmpty() {
        return world.isEmpty() && water.isEmpty() && waterfall.isEmpty();
    }

    public void render() {

    }
}