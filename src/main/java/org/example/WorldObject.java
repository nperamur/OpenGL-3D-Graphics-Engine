package org.example;

import org.example.bvh.AABB;
import org.example.bvh.TriangleBVH;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface WorldObject {
    AABB getAABB();
    Vector3f getPosition();

    TriangleBVH getBVH();

    Matrix4f getInverseTransformationMatrix();

    boolean hasCollision();

    boolean hasRaytracedShadows();

    String getName();
    Matrix4f getTransformationMatrix();
}
