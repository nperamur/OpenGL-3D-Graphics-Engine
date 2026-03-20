package org.example.bvh;

import org.joml.Vector3f;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriangleBVH extends AbstractBVH<Triangle> {

    private static final int NUM_LEAF_TRIANGLES = 10;

    public TriangleBVH() {
        super(NUM_LEAF_TRIANGLES);
    }



    public void addData(float[] vertices, int[] indices) {
        ArrayList<Triangle> triangles = new ArrayList<>();
        if (super.getRoot() != null) {
            triangles.addAll(getData());
        }
        for (int i = 0; i < indices.length; i += 3) {
            Triangle triangle;
            Vector3f v1 = new Vector3f(vertices[indices[i] * 3], vertices[indices[i] * 3 + 1], vertices[indices[i] * 3 + 2]);
            Vector3f v2 = new Vector3f(vertices[indices[i + 1] * 3], vertices[indices[i + 1] * 3 + 1], vertices[indices[i + 1] * 3 + 2]);
            Vector3f v3 = new Vector3f(vertices[indices[i + 2] * 3], vertices[indices[i + 2] * 3 + 1], vertices[indices[i + 2] * 3 + 2]);

            triangle = new Triangle(v1, v2, v3);
            triangles.add(triangle);
        }
        buildBVH(triangles);

    }



    @Override
    protected AABB createAABB(List<Triangle> triangles) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        for (Triangle triangle : triangles) {
            if (triangle.getMinX() < min.x()) {
                min.x = triangle.getMinX();
            }
            if (triangle.getMinY() < min.y()) {
                min.y = triangle.getMinY();
            }
            if (triangle.getMinZ() < min.z()) {
                min.z = triangle.getMinZ();
            }
            if (triangle.getMaxX() > max.x()) {
                max.x = triangle.getMaxX();
            }
            if (triangle.getMaxY() > max.y()) {
                max.y = triangle.getMaxY();
            }
            if (triangle.getMaxZ() > max.z()) {
                max.z = triangle.getMaxZ();
            }
        }

        return new AABB(min, max);
    }

    @Override
    protected ArrayList<Triangle>[] partition(BVHNode<Triangle> node, ArrayList<Triangle> triangles) {
        float numBins = 64;
        ArrayList<Triangle>[] finalPartition;
        sortTriangles(triangles, node);
        if (triangles.size() > numBins) {
            int stepSize = (int) (triangles.size() / numBins);
            finalPartition = testSplits(triangles, stepSize, node);
        } else {
            finalPartition = testSplits(triangles, 1, node);
        }
        if (finalPartition[0].isEmpty() || finalPartition[1].isEmpty()) {
            return null;
        }
        return finalPartition;
    }

    private ArrayList<Triangle>[] testSplits(ArrayList<Triangle> triangles, int stepSize, BVHNode<Triangle> node) {
        ArrayList<Triangle>[] partition = (ArrayList<Triangle>[]) new ArrayList[2];
        partition[0] = new ArrayList<>();
        partition[1] = new ArrayList<>();
        int minCostIndex = 0;
        float minCost = Float.POSITIVE_INFINITY;
        for (int i = 0; i < triangles.size() - 1; i += stepSize) {
            float cost = evaluateSplit(triangles, i, node);
            if (cost <= minCost) {
                minCost = cost;
                minCostIndex = i;
            }
        }
        partition[0] = new ArrayList<>(triangles.subList(0, minCostIndex + 1));
        partition[1] = new ArrayList<>(triangles.subList(minCostIndex + 1, triangles.size()));
        return partition;
    }

    private void sortTriangles(ArrayList<Triangle> triangles, BVHNode<Triangle> node) {
        Axis axis = node.getBoundingBox().getLongestAxis();

        triangles.sort((a, b) -> {
            float centerA = a.getCentroidValue(axis);
            float centerB = b.getCentroidValue(axis);
            return Float.compare(centerA, centerB);
        });
    }

    public AABB getAABB() {
        return super.getRoot().getBoundingBox();
    }

    private float evaluateSplit(ArrayList<Triangle> triangles, int i, BVHNode<Triangle> parent) {
        List<Triangle> leftArray = triangles.subList(0, i + 1);
        AABB leftAABB = createAABB(leftArray);
        List<Triangle> rightArray = triangles.subList(i + 1, triangles.size());
        AABB rightAABB = createAABB(rightArray);
        return evaluateSplit(leftAABB.getSurfaceArea(),
                rightAABB.getSurfaceArea(),
                parent.getBoundingBox().getSurfaceArea(),
                leftArray.size(),
                rightArray.size());
    }



    private float evaluateSplit(float surfaceAreaLeft, float surfaceAreaRight, float surfaceAreaParent,
                               int numLeftTriangles, int numRightTriangles) {

        if (surfaceAreaParent <= 0) return Float.POSITIVE_INFINITY;

        float costTraversal = 1.0f;
        float costIntersection = 1.5f;

        return costTraversal +
                (surfaceAreaLeft / surfaceAreaParent) * numLeftTriangles * costIntersection +
                (surfaceAreaRight / surfaceAreaParent) * numRightTriangles * costIntersection;
    }





}
