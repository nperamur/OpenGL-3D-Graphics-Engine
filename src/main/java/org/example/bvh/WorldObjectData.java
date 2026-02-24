package org.example.bvh;

import org.joml.Vector3f;

import java.util.ArrayList;

public interface WorldObjectData {
    ArrayList<TriangleGroup> getNearbyTrianglesWithCollisionsEnabled(Vector3f position);
    ArrayList<TriangleGroup> getNearbyTriangles(Vector3f position);

    ArrayList<TriangleGroup> rayCastNearbyTrianglesWithCollisionsEnabled(Vector3f position, Vector3f direction);
}
