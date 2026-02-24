package org.example.bvh;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class TriangleGroup {
    private ArrayList<Triangle> triangles;
    private AABB boundingBox;
    private String modelName;

    private Matrix4f transformation;


    public TriangleGroup(ArrayList<Triangle> triangles, AABB boundingBox, Matrix4f transformation, String modelName) {
        this.triangles = triangles;
        this.boundingBox = boundingBox;
        this.modelName = modelName;
        this.transformation = transformation;
    }

    public String getModelName() {
        return this.modelName;
    }

    public ArrayList<Triangle> getTriangles() {
        return triangles;
    }


    public AABB getBoundingBox() {
        return boundingBox;
    }



    public Matrix4f getTransformationMatrix() {
        return transformation;
    }

}
