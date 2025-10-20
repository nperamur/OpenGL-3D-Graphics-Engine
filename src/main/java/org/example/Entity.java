package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Entity {
    private TexturedModel model;
    private Vector3f position;
    private float rotX, rotY, rotZ;
    private float scale;

    public Entity(TexturedModel model, Vector3f position, float rotZ, float rotY, float rotX, float scale) {
        this.model = model;
        this.position = position;
        this.rotZ = rotZ;
        this.rotY = rotY;
        this.rotX = rotX;
        this.scale = scale;
    }

    public void increasePosition(float deltaX, float deltaY, float deltaZ) {
        this.position.x += deltaX;
        this.position.y += deltaY;
        this.position.z += deltaZ;
    }

    public void increaseRotation(float deltaX, float deltaY, float deltaZ) {
        this.rotX += deltaX;
        this.rotY += deltaY;
        this.rotZ += deltaZ;
    }


    public TexturedModel getModel() {
        return model;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void render(TestShader shader) {
        Matrix4f transformationMatrix = GameMath.createTransformationMatrix(position, rotX, rotY, rotZ, scale);
        shader.loadTransformationMatrix(transformationMatrix);
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.model.getTexture().getTextureID());
        Renderer.renderModel(this.model.getRawModel());
    }

    public void setRotation(Vector3f vec) {
        this.rotX = vec.x;
        this.rotY = vec.y;
        this.rotZ = vec.z;
    }
}
