package org.example.bvh;

import org.joml.Vector3f;

public record BVHNodeData<E>(AABB aabb, int index, boolean isLeaf, int nodeCount, E firstNode) {
}
