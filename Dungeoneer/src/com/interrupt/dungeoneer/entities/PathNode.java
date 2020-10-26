package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Pathfinding;

public class PathNode extends Entity {
    public transient short playerSmell = 0;
    public short weight = 0;
    public boolean nodeEnabled = true;

    private transient Array<PathNode> connections = new Array<PathNode>();
    private transient Array<PathNode> jumps = new Array<PathNode>();

    public transient Vector3 loc = new Vector3();

    public PathNode() { }

    public PathNode(Vector3 location) {
        this.loc.set(location);
        x = location.x;
        y = location.y;
        z = location.z;
    }

    public Vector3 getLocation() {
        return loc.set(x, y, z);
    }

    private void addConnection(PathNode node) {
        if(node == this)
            return;

        if(connections.contains(node, true))
            return;

        connections.add(node);
    }

    private void addJumpDown(PathNode node) {
        if(node == this)
            return;

        if(jumps.contains(node, true))
            return;

        jumps.add(node);
    }

    public boolean addConnectionIfPossible(PathNode node, float stepHeight, float fallHeight) {
        float height = loc.z - node.loc.z;
        if(height > stepHeight) {
            if(height < fallHeight) {
                addJumpDown(node);
            }
        }

        if(Math.abs(height) < stepHeight) {
            addConnection(node);
            return true;
        }

        return false;
    }

    public Array<PathNode> getConnections() {
        return connections;
    }

    public Array<PathNode> getJumps() {
        return jumps;
    }

    public void walk(int iteration, PathNode previous) {
        synchronized (this) {
            if(playerSmell <= iteration)
                return;

            playerSmell = (short) Math.min(iteration, playerSmell);
        }

        if(iteration++ < Pathfinding.MaxTraversal) {
            // normal connections
            for (int i = 0; i < connections.size; i++) {
                PathNode node = connections.get(i);
                if(node != previous && node.nodeEnabled) {
                    node.walk(iteration, this);
                }
            }
        }
    }

    public void reset() {
        synchronized (this) {
            playerSmell = Short.MAX_VALUE;
        }
    }

    public void setEnabled(boolean isEnabled) {
        synchronized (this) {
            this.nodeEnabled = isEnabled;
        }
    }
}
