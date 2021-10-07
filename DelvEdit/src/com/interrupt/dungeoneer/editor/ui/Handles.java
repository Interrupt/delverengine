package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Meshes;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;

public class Handles {
    public static ShapeRenderer renderer = new ShapeRenderer();
    private static Camera camera;
    private static Color color = Color.WHITE;

    private static Color pickColor = Color.WHITE;
    private static boolean picking = false;

    private static final Mesh cone;
    private static final Mesh cube;
    private static final Mesh disc;
    private static final Mesh quad;
    private static final Texture texture;

    static {
        cone = Meshes.cone();
        cube = Meshes.cube();
        disc = Meshes.disc();
        quad = Meshes.quad();

        // Generate texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        texture = new Texture(pixmap);
    }

    public static void setColor(Color color) {
        Handles.color = color;
    }

    public static void setPickColor(Color color) {
        Handles.pickColor = color;
    }

    public static void drawWireDisc(Vector3 position, Vector3 axis, float radius, int segments) {
        beginLineRendering();
        drawWireDiscInternal(position, axis, radius, segments);
        endLineRendering();
    }

    public static void drawWireSphere(Vector3 position, float radius) {
        beginLineRendering();
        drawWireSphereInternal(position, radius);
        endLineRendering();
    }

    public static void drawWireCube(Vector3 position, Vector3 size) {
        beginLineRendering();
        drawWireCubeInternal(position, size);
        endLineRendering();
    }

    public static void drawWireFrustum(Frustum frustum) {
        beginLineRendering();
        drawWireFrustumInternal(frustum);
        endLineRendering();
    }

    public static void drawCone(Vector3 position, Quaternion rotation, Vector3 scale) {
        beginMeshRendering();
        drawMeshInternal(cone, position, rotation, scale);
        endMeshRendering();
    }

    public static void drawCube(Vector3 position, Quaternion rotation, Vector3 scale) {
        beginMeshRendering();
        drawMeshInternal(cube, position, rotation, scale);
        endMeshRendering();
    }

    public static void drawDisc(Vector3 position, Quaternion rotation, Vector3 scale) {
        beginMeshRendering();
        drawMeshInternal(disc, position, rotation, scale);
        endMeshRendering();
    }

    public static void drawQuad(Vector3 position, Quaternion rotation, Vector3 scale) {
        beginMeshRendering();
        drawMeshInternal(quad, position, rotation, scale);
        endMeshRendering();
    }

    private static void beginLineRendering() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        if (picking) {
            Gdx.gl.glLineWidth(4f);
        }
        else {
            Gdx.gl.glLineWidth(1f);
        }

        camera = Editor.app.camera;

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        if (picking) {
            renderer.setColor(pickColor);
        }
        else {
            renderer.setColor(color);
        }
    }

    private static void endLineRendering() {
        renderer.end();

        if (picking) {
            Gdx.gl.glLineWidth(1f);
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private static void beginMeshRendering() {
        GlRenderer.clearBoundTexture();
    }

    private static void endMeshRendering() {

    }

    private static void drawWireDiscInternal(Vector3 position, Vector3 axis, float radius, int segments) {
        Vector3 o = new Vector3(position);
        Vector3 d = new Vector3(axis).nor();
        Vector3 r = new Vector3(1, 0, 0);

        if (axis.epsilonEquals(Vector3.X)) {
            r.set(0, 0, 1);
        }

        r.crs(d).nor();
        Vector3 u = new Vector3(d).crs(r).nor();

        float tau = (float)Math.PI * 2;
        float step = tau / segments;

        Vector3 current = new Vector3();
        Vector3 next = new Vector3();

        Vector3 a = new Vector3();
        Vector3 b = new Vector3();

        for(int i = 0; i < segments; i++) {
            float sin = (float)Math.sin(i * step) * radius;
            float cos = (float)Math.cos(i * step) * radius;

            a.set(u).scl(cos);
            b.set(r).scl(sin);
            current.set(a).add(b).add(o);

            float nextsin = (float)Math.sin((i + 1) * step) * radius;
            float nextcos = (float)Math.cos((i + 1) * step) * radius;

            a.set(u).scl(nextcos);
            b.set(r).scl(nextsin);
            next.set(a).add(b).add(o);

            renderer.line(current, next);
        }
    }

    private static void drawWireSphereInternal(Vector3 position, float radius) {
        // Draw axially aligned discs
        //drawWireDisc(origin, Vector3.X, radius);
        //drawWireDisc(origin, Vector3.Y, radius);
        //drawWireDisc(origin, Vector3.Z, radius);

        // Draw disc that encompasses sphere according to camera perspective.
        Vector3 normal = new Vector3(position).sub(camera.position);
        float sqrMag = normal.len2();
        float n0 = radius * radius;
        float n1 = n0 * n0 / sqrMag;
        float n2 = (float) Math.sqrt(n0 - n1);

        Vector3 a = new Vector3(normal).scl(n0 / sqrMag);
        Vector3 b = new Vector3(position).sub(a);

        drawWireDiscInternal(b, normal, n2, 48);
    }

    private static void drawWireCubeInternal(Vector3 position, Vector3 size) {
        float px = position.x;
        float py = position.y;
        float pz = position.z;
        float sx = size.x;
        float sy = size.y;
        float sz = size.z;

        Vector3 p0 = new Vector3(px - sx, py - sy, pz - sz);
        Vector3 p1 = new Vector3(px + sx, py - sy, pz - sz);
        Vector3 p2 = new Vector3(px + sx, py + sy, pz - sz);
        Vector3 p3 = new Vector3(px - sx, py + sy, pz - sz);
        Vector3 p4 = new Vector3(px - sx, py - sy, pz + sz);
        Vector3 p5 = new Vector3(px + sx, py - sy, pz + sz);
        Vector3 p6 = new Vector3(px + sx, py + sy, pz + sz);
        Vector3 p7 = new Vector3(px - sx, py + sy, pz + sz);

        // Bottom
        renderer.line(p0, p1);
        renderer.line(p1, p2);
        renderer.line(p2, p3);
        renderer.line(p3, p0);

        // Top
        renderer.line(p4, p5);
        renderer.line(p5, p6);
        renderer.line(p6, p7);
        renderer.line(p7, p4);

        // Sides
        renderer.line(p0, p4);
        renderer.line(p1, p5);
        renderer.line(p2, p6);
        renderer.line(p3, p7);
    }

    private static void drawWireFrustumInternal(Frustum frustum) {
        for(int i = 0; i < 4; i++) {
			Vector3 startPoint = frustum.planePoints[i];
			Vector3 endPoint = i != 3 ? frustum.planePoints[i + 1] : frustum.planePoints[0];

			renderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
		}

		for(int i = 0; i < 4; i++) {
			Vector3 startPoint = frustum.planePoints[i];
			Vector3 endPoint = frustum.planePoints[i + 4];

			renderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
		}

		for(int i = 4; i < 8; i++) {
			Vector3 startPoint = frustum.planePoints[i];
			Vector3 endPoint = i != 7 ? frustum.planePoints[i + 1] : frustum.planePoints[4];

			renderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
		}
    }

    private static final Matrix4 combined = new Matrix4();
    private static final Matrix4 model = new Matrix4();
    private static void drawMeshInternal(Mesh mesh, Vector3 position, Quaternion rotation, Vector3 scale) {
        model.idt()
            .translate(position.x, position.z, position.y)
            .rotate(rotation)
            .scale(scale.x, scale.z, scale.y);

        combined.set(Editor.app.camera.combined).mul(model);

        ShaderInfo info = GlRenderer.modelShaderInfo;

        GlRenderer.bindTexture(texture);

        info.setAttributes(
            combined,
            0,
            0,
            0,
            0,
            Color.WHITE,
            picking ? pickColor : color,
            false
        );

        info.begin();
        mesh.render(info.shader, GL20.GL_TRIANGLES);
        info.end();
    }

    public static void pickingBegin() {
        picking = true;
    }

    public static void pickingEnd() {
        picking = false;
    }
}
