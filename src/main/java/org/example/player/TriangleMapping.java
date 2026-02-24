package org.example.player;

import org.example.bvh.Triangle;
import org.example.bvh.TriangleGroup;
import org.joml.Matrix4f;

public record TriangleMapping(
        Triangle triangle,

        Matrix4f transformation) {
}
