package org.example;

import org.example.bvh.AABB;
import org.example.bvh.Axis;
import org.example.bvh.TriangleBVH;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Model implements WorldObject {
    private int id;
    private int vertexCount;
    private boolean hasCollision = true;

    private boolean hasRaytracedShadows = true;

    private TriangleBVH bvh;

    private Matrix4f transformationMatrix = new Matrix4f();

    private String name;



    public Model(int id, int vertexCount, TriangleBVH bvh, String name) {
        this.id = id;
        this.vertexCount = vertexCount;
        this.bvh = bvh;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public void setBVH(TriangleBVH bvh) {
        this.bvh = bvh;
    }


    @Override
    public AABB getAABB() {
        return bvh.getAABB();
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f();
    }

    @Override
    public TriangleBVH getBVH() {
        return this.bvh;
    }

    @Override
    public Matrix4f getInverseTransformationMatrix() {
        return new Matrix4f(transformationMatrix).invert();
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }

    @Override
    public boolean hasCollision() {
        return hasCollision;
    }

    @Override
    public boolean hasRaytracedShadows() {
        return hasRaytracedShadows;
    }

    public void setHasCollision(boolean hasCollision) {
        this.hasCollision = hasCollision;
    }

    public void setHasRaytracedShadows(boolean hasRaytracedShadows) {
        this.hasRaytracedShadows = hasRaytracedShadows;
    }


    public void setTransformationMatrix(Matrix4f transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
    }

    public String getName() {
        return name;
    }


}
