package org.example;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameMath {
    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(translation);
        matrix.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix);
        matrix.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix);
        matrix.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix);
        matrix.scale(new Vector3f(scale, scale, scale));
        return matrix;
    }

    public static Matrix4f createViewMatrix(Player player) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.rotate((float) (Math.toRadians(player.getPitch())), new Vector3f(1, 0, 0), matrix);
        matrix.rotate((float) (Math.toRadians(player.getYaw())), new Vector3f(0, 1, 0), matrix);
        Vector3f cameraPos = player.getPosition();
        Vector3f negativeCameraPos;
        if (player.isCrouching()) {
            negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y - 1f, -cameraPos.z);
        } else {
            negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y - 1.5f, -cameraPos.z);
        }
        matrix.translate(negativeCameraPos, matrix);

        return matrix;
    }

    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

}
