package org.example.bvh;

import org.example.player.PlayerCapsule;
import org.joml.Vector3f;

import java.util.ArrayList;

public interface WorldObjectData {
    ArrayList<TriangleGroup> getNearbyTrianglesWithCollisionsEnabled(Vector3f position, PlayerCapsule capsule, Vector3f movement);
    ArrayList<TriangleGroup> getNearbyTriangles(Vector3f position, PlayerCapsule capsule, Vector3f movement);

    ArrayList<TriangleGroup> rayCastNearbyTrianglesWithCollisionsEnabled(Vector3f position, Vector3f direction);
}
