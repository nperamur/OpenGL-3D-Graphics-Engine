package org.example.bvh;

import org.joml.Vector3f;

import java.util.Arrays;

public class Triangle {

    private Vector3f[] points;

    private Vector3f[] edges;

    public Triangle(Vector3f p1, Vector3f p2, Vector3f p3) {
        this.points = new Vector3f[3];
        points[0] = p1;
        points[1] = p2;
        points[2] = p3;

        this.edges = new Vector3f[2];
        edges[0] = new Vector3f(p2).sub(p1);
        edges[1] = new Vector3f(p3).sub(p1);

    }

    public Vector3f[] getEdges() {
        return Arrays.copyOf(this.edges, 2);
    }

    public Vector3f[] getPoints() {
        return Arrays.copyOf(points, 3);
    }

    public float getMinX() {
        return Math.min(points[0].x, Math.min(points[1].x, points[2].x));
    }
    public float getMinY() {
        return Math.min(points[0].y, Math.min(points[1].y, points[2].y));
    }
    public float getMinZ() {
        return Math.min(points[0].z, Math.min(points[1].z, points[2].z));
    }
    public float getMaxX() {
        return Math.max(points[0].x, Math.max(points[1].x, points[2].x));
    }
    public float getMaxY() {
        return Math.max(points[0].y, Math.max(points[1].y, points[2].y));
    }
    public float getMaxZ() {
        return Math.max(points[0].z, Math.max(points[1].z, points[2].z));
    }

    public int compareToCentroid(Vector3f center, Axis axis) {
        Vector3f centroid = calculateCentroid();
        return switch (axis) {
            case X -> center.x < centroid.x ? 1 : -1;
            case Y -> center.y < centroid.y ? 1 : -1;
            case Z -> center.z < centroid.z ? 1 : -1;
        };
    }

    public float getCentroidValue(Axis axis) {
        Vector3f centroid = calculateCentroid();
        return switch (axis) {
            case X -> centroid.x;
            case Y -> centroid.y;
            case Z -> centroid.z;
        };
    }

    private Vector3f calculateCentroid() {
        Vector3f[] points = getPoints();
        return (new Vector3f(points[0]).add(new Vector3f(points[1])).add(new Vector3f(points[2])).div(3));
    }

    public boolean isOnTriangle(Vector3f position) {
        Vector3f normal = new Vector3f(edges[0]).cross(edges[1]).normalize();

        Vector3f edge0 = new Vector3f(points[1]).sub(points[0]);
        Vector3f edge1 = new Vector3f(points[2]).sub(points[1]);
        Vector3f edge2 = new Vector3f(points[0]).sub(points[2]);

        boolean test0 = computeLR(edge0, points[0], position, normal);
        boolean test1 = computeLR(edge1, points[1], position, normal);
        boolean test2 = computeLR(edge2, points[2], position, normal);

        return (test0 && test1 && test2) || (!test0 && !test1 && !test2);
    }

    public boolean isOnExtrudedTriangle(Vector3f position, float extrudeRadius) {
        Vector3f normal = new Vector3f(edges[0]).cross(edges[1]).normalize();

        Vector3f edge0 = new Vector3f(points[1]).sub(points[0]);
        Vector3f edge1 = new Vector3f(points[2]).sub(points[1]);
        Vector3f edge2 = new Vector3f(points[0]).sub(points[2]);

        boolean test0 = computeLR(edge0, points[0], position, normal);
        boolean test1 = computeLR(edge1, points[1], position, normal);
        boolean test2 = computeLR(edge2, points[2], position, normal);

        float dist0 = distanceToEdge(edge0, points[0], position);
        float dist1 = distanceToEdge(edge1, points[1], position);
        float dist2 = distanceToEdge(edge2, points[2], position);

        boolean insideExtruded = ((test0 && test1 && test2) || (!test0 && !test1 && !test2))
                || (dist0 <= extrudeRadius && dist1 <= extrudeRadius && dist2 <= extrudeRadius);

        return insideExtruded;
    }


    private float distanceToEdge(Vector3f edge, Vector3f edgeStart, Vector3f point) {
        Vector3f pointToStart = new Vector3f(point).sub(edgeStart);
        Vector3f edgeDir = new Vector3f(edge).normalize();
        Vector3f proj = new Vector3f(edgeDir).mul(pointToStart.dot(edgeDir));
        return new Vector3f(pointToStart).sub(proj).length();
    }

    private boolean computeLR(Vector3f edge, Vector3f edgeStart, Vector3f position, Vector3f normal) {
        Vector3f pointVector = new Vector3f(position).sub(edgeStart);
        return new Vector3f(edge).cross(pointVector).dot(normal) > 0;
    }


}
