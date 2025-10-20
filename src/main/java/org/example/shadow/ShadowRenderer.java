package org.example.shadow;

import org.example.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ShadowRenderer {
    private ShadowBox box;
    private ShadowShader shader;

    private ShadowFrameBuffer frameBuffer;
    private Matrix4f lightViewMatrix = new Matrix4f();
    private Matrix4f lightProjectionMatrix = new Matrix4f();
    private Matrix4f projectionViewMatrix = new Matrix4f();
    private Matrix4f offset = createOffset();

    private static final int SHADOW_MAP_SIZE = 3000;

    private Sunlight light;

    private ShadowEntityRenderer renderer;

    public ShadowRenderer(Player player, Sunlight light) {
        box = new ShadowBox(lightViewMatrix, player);
        frameBuffer = new ShadowFrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
        this.light = light;
        this.shader = new ShadowShader();
        this.renderer = new ShadowEntityRenderer(shader, projectionViewMatrix);
    }




    public void render(ArrayList<Entity> entities) {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);

        shader.start();
        box.updateShadowBox();


        updateLightViewMatrix(new Vector3f(light.getPosition().x, -Math.max(5000, light.getPosition().y), light.getPosition().z), box.getCenter());
        updateOrthographicProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
        lightProjectionMatrix.mul(lightViewMatrix, projectionViewMatrix);
        renderer.render(entities);
        shader.stop();


    }


    public void bindFrameBuffer() {
        frameBuffer.bindFrameBuffer();
    }

    public void unbindFrameBuffer() {
        frameBuffer.unbindCurrentFrameBuffer();
    }

    private void updateLightViewMatrix(Vector3f direction, Vector3f center) {
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

    private void updateOrthographicProjectionMatrix(float width, float height, float length) {
        lightProjectionMatrix.identity();
        lightProjectionMatrix.m00(2f / width);
        lightProjectionMatrix.m11(2f / height);
        lightProjectionMatrix.m22(-2f / length);
        lightProjectionMatrix.m33(1);
    }


    public void cleanUp() {
        shader.cleanUp();
        frameBuffer.cleanUp();
    }


    public int getShadowMap() {
        return frameBuffer.getDepthTexture();
    }

    public Matrix4f getLightViewMatrix() {
        return lightViewMatrix;
    }

    public Matrix4f getLightProjectionMatrix() {
        return this.lightProjectionMatrix;
    }

    public Matrix4f getToShadowMapSpaceMatrix() {
        return new Matrix4f(offset).mul(projectionViewMatrix);
    }

    private static Matrix4f createOffset() {
        Matrix4f offset = new Matrix4f();
        offset.translate(new Vector3f(0.5f, 0.5f, 0.5f));
        offset.scale(new Vector3f(0.5f, 0.5f, 0.5f));
        return offset;
    }











}
