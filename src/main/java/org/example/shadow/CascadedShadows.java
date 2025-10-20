package org.example.shadow;

import org.example.Player;
import org.example.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CascadedShadows {
    private static final int NUM_CASCADES = 3;

    private ShadowBox[] shadowBoxes;

    private Matrix4f[] lightViewMatrices;

    private Matrix4f[] lightProjectionMatrices;

    public CascadedShadows(Player player, int distance) {
        this.lightViewMatrices = new Matrix4f[NUM_CASCADES];
        this.lightProjectionMatrices = new Matrix4f[NUM_CASCADES];
        this.shadowBoxes = new ShadowBox[NUM_CASCADES];

        for (int i = 1; i < NUM_CASCADES + 1; i++) {
            lightViewMatrices[i - 1] = new Matrix4f();
            lightProjectionMatrices[i - 1] = new Matrix4f();
            float bias = 0.5f;
            float uniformSplit = Renderer.NEAR_PLANE + (distance - Renderer.NEAR_PLANE) * ((float) i / NUM_CASCADES);
            float logSplit     = Renderer.NEAR_PLANE * (float)Math.pow(distance / Renderer.NEAR_PLANE, (float)i / NUM_CASCADES);
            float splitDist    = bias * logSplit + (1 - bias) * uniformSplit;

            shadowBoxes[i - 1] = new ShadowBox(lightViewMatrices[i - 1], player, Math.round(splitDist));
        }
    }

    public void updateShadowBoxes() {
        for (ShadowBox shadowBox : shadowBoxes) {
            shadowBox.updateShadowBox();
        }
    }

    public void updateLightViewMatrices(Vector3f direction) {
        for (int i = 0; i < shadowBoxes.length; i++) {
            updateLightViewMatrix(direction, shadowBoxes[i].getCenter(), lightViewMatrices[i]);
        }
    }

    public void updateLightProjectionMatrices() {
        for (int i = 0; i < shadowBoxes.length; i++) {
            updateOrthographicProjectionMatrix(shadowBoxes[i].getWidth(), shadowBoxes[i].getHeight(), shadowBoxes[i].getLength(), lightProjectionMatrices[i]);
        }
    }


    private void updateLightViewMatrix(Vector3f direction, Vector3f center, Matrix4f lightViewMatrix) {
        direction.normalize(direction);
        center.negate();
        lightViewMatrix.identity();
        float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
        lightViewMatrix.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix);
        float yaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        yaw = direction.z > 0 ? yaw - 180 : yaw;
        lightViewMatrix.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0), lightViewMatrix);
        lightViewMatrix.translate(center, lightViewMatrix);
    }

    private void updateOrthographicProjectionMatrix(float width, float height, float length, Matrix4f lightProjectionMatrix) {
        lightProjectionMatrix.identity();
        lightProjectionMatrix.m00(2f / width);
        lightProjectionMatrix.m11(2f / height);
        lightProjectionMatrix.m22(-2f / length);
        lightProjectionMatrix.m33(1);
    }
}
