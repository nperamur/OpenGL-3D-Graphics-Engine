package org.example.player;

import org.example.bvh.Triangle;
import org.example.bvh.TriangleGroup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerCapsule {
    private float radius;

    private static final int CAPSULE_HEIGHT = 3;
    private Vector3f base;

    private Vector3f tip;

    private Vector3f axis;


    public PlayerCapsule(float radius, Vector3f base) {
        this.radius = radius;

        setPosition(base);
    }

    private void setPosition(Vector3f position) {
        this.base = new Vector3f(new Vector3f(position.x, position.y - 0.5f, position.z));
        this.tip = new Vector3f(position.x, position.y + CAPSULE_HEIGHT, position.z);
        this.axis = new Vector3f(tip).sub(base);
        this.axis.normalize();
    }


    public float getRadius() {
        return radius;
    }

    public void setHorizontalRadius(float horizontalRadius) {
        this.radius = horizontalRadius;
    }


    private boolean isColliding(Triangle triangle, Matrix4f transformationMatrix) {
        Vector3f[] points = triangle.getPoints();
        Vector3f p0 = transformationMatrix.transformPosition(new Vector3f(points[0]));
        Vector3f p1 = transformationMatrix.transformPosition(new Vector3f(points[1]));
        Vector3f p2 = transformationMatrix.transformPosition(new Vector3f(points[2]));

        Vector3f e0 = new Vector3f(p1).sub(p0);
        Vector3f e1 = new Vector3f(p2).sub(p0);
        Vector3f normal = new Vector3f(e0).cross(e1).normalize();
        orientNormal(normal, p0);
        Vector3f rayCastedPos;
        Vector3f centroid = new Vector3f(p0).add(p1).add(p2).div(3.0f);

        Vector3f closestTrianglePoint;
        if (isPerpendicular(axis, new Vector3f(normal))) {
            Vector3f startingPoint = calculateClosestSegmentStartFromSegment(p0, p1, p2, axis, base, normal);
            if (new Vector2f(startingPoint.x, startingPoint.z).distance(new Vector2f(base.x, base.z)) > radius) {
                return false;
            }


            Vector3f edge1 = new Vector3f(p1).sub(p0);
            Vector3f edge2 = new Vector3f(p2).sub(p0);
            Vector3f edge3 = new Vector3f(p2).sub(p1);

            Vector3f n1 = new Vector3f(edge1).cross(normal);
            n1.normalize();
            Vector3f n2 = new Vector3f(edge2).cross(normal);
            n2.normalize();
            Vector3f n3 = new Vector3f(edge3).cross(normal);
            n3.normalize();

            Vector3f intersect1 = rayCast(startingPoint, p0, axis, n1);
            Vector3f intersect2 = rayCast(startingPoint, p1, axis, n2);
            Vector3f intersect3 = rayCast(startingPoint, p2, axis, n3);


            float intersectProj1 = new Vector3f(intersect1).dot(axis);
            float intersectProj2 = new Vector3f(intersect2).dot(axis);
            float intersectProj3 = new Vector3f(intersect3).dot(axis);
            float baseProj = new Vector3f(base).dot(axis);
            float tipProj = new Vector3f(tip).dot(axis);

            Vector2f triangleInterval = new Vector2f(
                    Math.min(
                            Float.isNaN(intersectProj1) ? Float.POSITIVE_INFINITY : intersectProj1,
                            Math.min(
                                    Float.isNaN(intersectProj2) ? Float.POSITIVE_INFINITY : intersectProj2,
                                    Float.isNaN(intersectProj3) ? Float.POSITIVE_INFINITY : intersectProj3)
                    ),
                    Math.max(
                            Float.isNaN(intersectProj1) ? Float.NEGATIVE_INFINITY : intersectProj1,
                            Math.max(
                                    Float.isNaN(intersectProj2) ? Float.NEGATIVE_INFINITY : intersectProj2,
                                    Float.isNaN(intersectProj3) ? Float.NEGATIVE_INFINITY : intersectProj3)
                    )
            );
            Vector2f capsuleInterval = new Vector2f(Math.min(baseProj, tipProj), Math.max(baseProj, tipProj));

            return capsuleInterval.x <= triangleInterval.y &&
                    capsuleInterval.y >= triangleInterval.x;

        } else {
            rayCastedPos = rayCast(centroid, base, axis, normal);
            closestTrianglePoint = getClosestPoint(triangle, p0, p1, p2, rayCastedPos, transformationMatrix);
        }

        return inCapsule(closestTrianglePoint);
    }


//    public boolean resolveCollisions(ArrayList<Triangle> triangles, Vector3f position, Matrix4f transformationMatrix) {
//        triangles = new ArrayList<>(triangles);
//        int numPenetrations = -1;
//        boolean collision = false;
//        while (!triangles.isEmpty() && (numPenetrations > 0 || numPenetrations == -1)) {
//            Penetration maxPenetration = null;
//            numPenetrations = 0;
//            for (Triangle triangle : triangles) {
//                if (isColliding(triangle, transformationMatrix)) {
//                    Vector3f recorrectedPosition = new Vector3f(position);
//                    float penetrationDepth = recorrectPosition(recorrectedPosition, triangle, transformationMatrix);
//                    if (maxPenetration == null || penetrationDepth > maxPenetration.depth()) {
//                        maxPenetration = new Penetration(penetrationDepth, triangle, recorrectedPosition);
//                    }
//                    numPenetrations++;
//                }
//            }
//            if (maxPenetration != null) {
//                position = maxPenetration.playerPos();
//                setPosition(position);
//                collision = true;
//                triangles.remove(maxPenetration.triangle());
//            }
//        }
//        return collision;
//    }

    public boolean resolveCollisions(ArrayList<TriangleGroup> triangleGroups, Vector3f position, int numIterations) {
        this.setPosition(position);
        ArrayList<TriangleMapping> triangles = new ArrayList<TriangleMapping>();
        for (TriangleGroup group : triangleGroups) {
            Matrix4f transformation = group.getTransformationMatrix();
            for (Triangle triangle : group.getTriangles()) {
                triangles.add(new TriangleMapping(triangle, transformation));
            }
        }
        boolean collision = false;

        for (int i = 0; i < numIterations; i++) {
            for (TriangleMapping mapping : triangles) {
                Triangle triangle = mapping.triangle();
                Matrix4f transformation = mapping.transformation();
                if (isColliding(triangle, transformation)) {
                    recorrectPosition(position, triangle, transformation, numIterations);
                    this.setPosition(position);
                    collision = true;
                }
            }
        }

        return collision;
    }



    //Note: Takes in direct mutable reference to player's position and modifies it
    //returns penetration depth
    private float recorrectPosition(Vector3f position, Triangle triangle, Matrix4f transformationMatrix, int numIterations) {
        Vector3f[] points = triangle.getPoints();
        Vector3f p0 = transformationMatrix.transformPosition(new Vector3f(points[0]));
        Vector3f p1 = transformationMatrix.transformPosition(new Vector3f(points[1]));
        Vector3f p2 = transformationMatrix.transformPosition(new Vector3f(points[2]));

        Vector3f e0 = new Vector3f(p1).sub(p0);
        Vector3f e1 = new Vector3f(p2).sub(p0);
        Vector3f normal = new Vector3f(e0).cross(e1).normalize();
        orientNormal(normal, p0);

        Vector3f rayCastedPos;
        Vector3f centroid = new Vector3f(p0).add(p1).add(p2).div(3.0f);

        Vector3f closestTrianglePoint;
        if (isPerpendicular(axis, normal) && !(Math.abs(normal.y) > 0.7f)) {
            Vector3f startingPoint = calculateClosestSegmentStartFromSegment(p0, p1, p2, axis, base, normal);
            float normalProj = new Vector3f(startingPoint).dot(normal);
            float axisNormalProj = new Vector3f(base).dot(normal);
            float penetrationDepth = radius - Math.abs(normalProj - axisNormalProj) + 0.0001f;
            if (Float.isNaN(normal.lengthSquared())) return -1;
            position.add(new Vector3f(normal).mul(penetrationDepth / numIterations));
            return penetrationDepth;
        } else {
            rayCastedPos = rayCast(centroid, base, axis, normal);
            closestTrianglePoint = getClosestPoint(triangle, p0, p1, p2, rayCastedPos, transformationMatrix);

        }

        Vector3f referencePoint;
        referencePoint = calculateClosestSegment(closestTrianglePoint,
                new Vector3f(base.x, base.y + radius, base.z),
                new Vector3f(tip.x, tip.y - radius, tip.z));
        float penetrationDepth = closestTrianglePoint.distance(referencePoint);
        Vector3f penetrationNormal = new Vector3f(referencePoint).sub(closestTrianglePoint).normalize();
        if (Float.isNaN(penetrationNormal.lengthSquared())) return -1;
        position.add(penetrationNormal.mul(Math.max((radius - penetrationDepth + 0.01f) / numIterations, 0)));
        return penetrationDepth;
    }

    private boolean isPerpendicular(Vector3f rayDir, Vector3f normal) {
        return Math.abs(new Vector3f(rayDir).dot(normal)) <= 0.08;
    }

    private void orientNormal(Vector3f normal, Vector3f pointOnTriangle) {
        float result = new Vector3f(normal).dot(new Vector3f(base).sub(pointOnTriangle));
        if (result < 0) {
            normal.mul(-1);
        }
    }



    private boolean inCapsule(Vector3f position) {
        Vector3f referencePoint;
        referencePoint = calculateClosestSegment(position,
                new Vector3f(base.x, base.y + radius, base.z),
                new Vector3f(tip.x, tip.y - radius, tip.z));
        return inSphere(position, referencePoint);
    }

    private boolean inSphere(Vector3f position, Vector3f referencePoint) {
        return Math.abs(new Vector3f(position).sub(referencePoint).length()) <= radius;
    }


    //Assumption: Passed in value is coplanar
    private Vector3f getClosestPoint(Triangle triangle, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f pos, Matrix4f transformationMatrix) {

        Matrix4f invMatrix = new Matrix4f(transformationMatrix).invert();
        Vector3f modelPos = invMatrix.transformPosition(new Vector3f(pos));

        if (triangle.isOnTriangle(modelPos)) {
            return pos;
        }

        Vector3f candidate1 = calculateClosestSegment(pos, p0, p1);
        Vector3f candidate2 = calculateClosestSegment(pos, p0, p2);
        Vector3f candidate3 = calculateClosestSegment(pos, p1, p2);

        return getClosestCandidate(candidate1, candidate2, candidate3, pos);
    }

    private Vector3f getClosestCandidate(Vector3f c1, Vector3f c2, Vector3f c3, Vector3f pos) {
        float d1 = c1.distanceSquared(pos);
        float d2 = c2.distanceSquared(pos);
        float d3 = c3.distanceSquared(pos);

        float min = Math.min(d1, Math.min(d2, d3));
        if (min == d1) {
            return c1;
        }
        if (min == d2) {
            return c2;
        }
        return c3;

    }

    private Vector3f calculateClosestSegment(Vector3f pos, Vector3f p1, Vector3f p2) {
        Vector3f v = new Vector3f(p2).sub(p1);
        Vector3f w = new Vector3f(pos).sub(p1);

        float lengthSquared = v.dot(v);

        if (lengthSquared < 1e-6f) {
            return new Vector3f(p1);
        }

        float t = w.dot(v) / lengthSquared;
        t = Math.max(0.0f, Math.min(1.0f, t));
        return new Vector3f(p1).add(v.mul(t));
    }



    private Vector3f rayCast(Vector3f p0, Vector3f position, Vector3f direction, Vector3f normal) {
        float t = new Vector3f(p0).sub(position).dot(normal) / direction.dot(normal);

        Vector3f rayCastedPos = new Vector3f(position).add(new Vector3f(direction).mul(t));
        return rayCastedPos;
    }


    //Handles Parallel case... yeah

    //Step 1: Project triangle onto ray that is perpendicular to axis & also perpendicular to normal prob bitangent
    //Step 2: Select the one point along that ray that is closest to the axis. This will be where our parallel ray will begin.
    //Step 3: Return the starting position of where the parallel ray will begin
    private Vector3f calculateClosestSegmentStartFromSegment(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f axis, Vector3f pos, Vector3f normal) {
        Vector3f bitangent = new Vector3f(axis).cross(normal);
        //Normalize just to make sure...
        bitangent.normalize();

        Vector3f toTriangle = new Vector3f(p0).sub(pos);
        if (bitangent.dot(toTriangle) < 0) {
            bitangent.mul(-1);
        }
        float projectedP0 = new Vector3f(p0).sub(pos).dot(bitangent);
        float projectedP1 = new Vector3f(p1).sub(pos).dot(bitangent);
        float projectedP2 = new Vector3f(p2).sub(pos).dot(bitangent);
        Vector2f projInterval = new Vector2f(Math.min(projectedP0, Math.min(projectedP1, projectedP2)),
                Math.max(projectedP0, Math.max(projectedP1, projectedP2)));

        int selection;
        float closestProj;
        if (0 >= projInterval.x && 0 <= projInterval.y) {
            closestProj = 0;
            selection = 0;
        } else if (0 < projInterval.x) {
            closestProj = projInterval.x;
            selection = 1;
        } else if (0 > projInterval.y) {
            closestProj = projInterval.y;
            selection = 2;
        } else {
            //If this case ever happens something has definitely gone horribly wrong...
            closestProj = -1;
            selection = -1;
        }

        int minIdx = (projectedP0 == projInterval.x) ? 0 : (projectedP1 == projInterval.x ? 1 : 2);
        int maxIdx = (projectedP0 == projInterval.y) ? 0 : (projectedP1 == projInterval.y ? 1 : 2);

        Vector3f result = switch (selection) {
            case 0 -> new Vector3f(p0).sub(new Vector3f(bitangent).mul(projectedP0));
            case 1 -> (minIdx == 0) ? p0 : (minIdx == 1 ? p1 : p2);
            case 2 -> (maxIdx == 0) ? p0 : (maxIdx == 1 ? p1 : p2);
            default -> throw new IllegalStateException("Unexpected selection value: " + selection);
        };
        return result;
    }

    //Step 4: Do check whether the axis starting at the point is within the radius of the cylinder. If it's not, autoreject.
    //Step 5: Finally, clamp to triangle bounds and see if the segment ANYWHERE will align with the bounds of the cylinder. ie. get shared interval




}
