package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Array;

public class StaticMeshPool {
    private Array<Mesh> freeMeshes = new Array<Mesh>();
    private Array<Mesh> usedMeshes = new Array<Mesh>();

    public void flush () {
        freeMeshes.addAll(usedMeshes);
        usedMeshes.clear();
    }

    public Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount, boolean createMax) {
        for (int i = 0, n = freeMeshes.size; i < n; ++i) {
            final Mesh mesh = freeMeshes.get(i);
            if (mesh.getVertexAttributes().equals(vertexAttributes) && mesh.getMaxVertices() >= vertexCount
                    && mesh.getMaxIndices() >= indexCount) {
                freeMeshes.removeIndex(i);
                usedMeshes.add(mesh);
                return mesh;
            }
        }

        // Max is 32k for LibGdx
        int maxVerts = 32000 * 9;
        int maxIndices = 32000;

        if(createMax) {
            vertexCount = maxVerts;
            indexCount = maxIndices;
        }

        if(vertexCount > maxVerts)
            vertexCount = maxVerts;

        if(indexCount > maxIndices)
            indexCount = maxIndices;

        try {
            Mesh result = new Mesh(true, vertexCount, indexCount, vertexAttributes);
            usedMeshes.add(result);
            return result;
        }
        catch(Exception ex) {
            Gdx.app.error("StaticMeshPool", "Could not obtain mesh", ex);
        }

        return null;
    }

    public boolean freeMesh(Mesh mesh) {
        // Only add meshes that are actually in use to the free meshes list
        if(usedMeshes.contains(mesh, true)) {
            usedMeshes.removeValue(mesh, true);

            if(!freeMeshes.contains(mesh, true)) {
                freeMeshes.add(mesh);
            }

            return true;
        }

        return false;
    }

    public void freeMeshes(Array<Mesh> meshes) {
        for(int i = 0; i < meshes.size; i++) {
            Mesh mesh = meshes.get(i);
            freeMesh(mesh);
        }
    }

    public void resetAndDisposeAllMeshes() {
        Array<Mesh> allMeshes = new Array<Mesh>();
        for(int i = 0; i < freeMeshes.size; i++) {
            allMeshes.add(freeMeshes.get(i));
        }

        for(int i = 0; i < usedMeshes.size; i++) {
            Mesh m = usedMeshes.get(i);
            if(!allMeshes.contains(m, true))
                allMeshes.add(m);
        }

        // reset the caches
        freeMeshes.clear();
        usedMeshes.clear();

        // Dispose all of the meshes
        for(int i = 0; i < allMeshes.size; i++) {
            Mesh m = allMeshes.get(i);
            try {
                m.dispose();
            }
            catch(Exception ex) {
                Gdx.app.log("StaticMeshPool", ex.getMessage());
            }
        }

        allMeshes.clear();
    }
}
