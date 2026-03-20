package org.example.player;

import org.example.bvh.Triangle;
import org.example.bvh.TriangleGroup;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public record TriangleMapping(
        Triangle triangle,

        Matrix4f transformation,
        Vector3f normal) {
}
