package org.example;

import org.example.bvh.AABB;
import org.example.bvh.TriangleBVH;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TexturedModel implements WorldObject {
    private Model rawModel;
    private ModelTexture texture;

    public TexturedModel(Model model, ModelTexture texture) {
        this.rawModel = model;
        this.texture = texture;
    }

    public Model getRawModel() {
        return rawModel;
    }

    public void setRawModel(Model rawModel) {
        this.rawModel = rawModel;
    }

    public ModelTexture getTexture() {
        return texture;
    }

    public void setTexture(ModelTexture texture) {
        this.texture = texture;
    }

    @Override
    public AABB getAABB() {
        return rawModel.getAABB();
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f();
    }

    @Override
    public TriangleBVH getBVH() {
        return rawModel.getBVH();
    }

    @Override
    public Matrix4f getInverseTransformationMatrix() {
        return rawModel.getInverseTransformationMatrix();
    }

    @Override
    public boolean hasCollision() {
        return rawModel.hasCollision();
    }

    @Override
    public boolean hasRaytracedShadows() {
        return rawModel.hasRaytracedShadows();
    }

    @Override
    public String getName() {
        return rawModel.getName();
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return rawModel.getTransformationMatrix();
    }
}
