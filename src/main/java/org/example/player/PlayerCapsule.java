package org.example.player;

import org.ejml.simple.SimpleMatrix;
import org.example.bvh.Triangle;
import org.example.bvh.TriangleGroup;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import java.util.ArrayList;

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
        Vector3f pos = new Vector3f(position);
        this.base = new Vector3f(pos.x, pos.y - 0.5f, pos.z);
        this.tip = new Vector3f(pos.x, pos.y + CAPSULE_HEIGHT, pos.z);
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
        transformationMatrix = new Matrix4f(transformationMatrix);
        Vector3f[] points = triangle.getPoints();
        Vector3f p0 = transformationMatrix.transformPosition(new Vector3f(points[0]));
        Vector3f p1 = transformationMatrix.transformPosition(new Vector3f(points[1]));
        Vector3f p2 = transformationMatrix.transformPosition(new Vector3f(points[2]));

        Vector3f e0 = new Vector3f(p1).sub(p0);
        Vector3f e1 = new Vector3f(p2).sub(p0);
        Vector3f normal = new Vector3f(e0).cross(e1).normalize();
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

            Vector3f intersect1 = getRayCastedPos(startingPoint, p0, axis, n1);
            Vector3f intersect2 = getRayCastedPos(startingPoint, p1, axis, n2);
            Vector3f intersect3 = getRayCastedPos(startingPoint, p2, axis, n3);


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
            rayCastedPos = getRayCastedPos(centroid, base, axis, normal);
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

    public boolean resolveCollisions(ArrayList<TriangleGroup> triangleGroups, Vector3f position, Vector3f movementVector, int numIterations) {
        this.setPosition(position);
        ArrayList<TriangleMapping> triangles = new ArrayList<TriangleMapping>();
        for (TriangleGroup group : triangleGroups) {
            Matrix4f transformation = group.getTransformationMatrix();
            for (Triangle triangle : group.getTriangles()) {
                Vector3f p0 = transformation.transformPosition(new Vector3f(triangle.getPoints()[0]));
                Vector3f p1 = transformation.transformPosition(new Vector3f(triangle.getPoints()[1]));
                Vector3f p2 = transformation.transformPosition(new Vector3f(triangle.getPoints()[2]));

                Vector3f normal = new Vector3f(new Vector3f(p1).sub(p0)).cross(new Vector3f(p2).sub(p0)).normalize();
                orientNormal(normal, p0);
                triangles.add(new TriangleMapping(triangle, transformation, normal));
            }
        }
        boolean collision = false;
        for (int i = 0; i < numIterations; i++) {
            ArrayList<TrianglePlane> constraints = new ArrayList<>();
            for (TriangleMapping mapping : triangles) {
                Triangle triangle = mapping.triangle();
                Matrix4f transformation = mapping.transformation();
                if (isColliding(triangle, transformation)) {
                    recorrectPosition(position, movementVector, triangle, transformation, numIterations, mapping.normal());
                    this.setPosition(position);
                    collision = true;
                    Vector3f p0 = transformation.transformPosition(new Vector3f(triangle.getPoints()[0]));
                    Vector3f p1 = transformation.transformPosition(new Vector3f(triangle.getPoints()[1]));
                    Vector3f p2 = transformation.transformPosition(new Vector3f(triangle.getPoints()[2]));
                    Vector3f normal = new Vector3f(new Vector3f(p1).sub(p0)).cross(new Vector3f(p2).sub(p0)).normalize();
                    orientNormal(normal, p1);
                    TrianglePlane plane = new TrianglePlane(normal, p0, p1, p2);
                    Vector3f tangent = new Vector3f(p1).sub(p0).normalize();
                    Vector3f bitangent = new Vector3f(normal).cross(tangent).normalize();
                    Matrix4f tbn = new Matrix4f(
                            new Vector4f(tangent, 0),
                            new Vector4f(bitangent, 0),
                            new Vector4f(normal, 0),
                            new Vector4f(0, 0, 0, 1)
                    );
                    try {
//                        Vector3f offset = getTangentialOffset(constraints, plane, tbn, false);
//                        System.out.println(offset);
//                        position.add(offset);
//                        this.setPosition(position);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    constraints.add(plane);
                }
            }
        }

        return collision;
    }



    //Note: Takes in direct mutable reference to player's position and modifies it
    //returns penetration depth
    //something weird is happening here
    private float recorrectPosition(Vector3f position, Vector3f movementVector, Triangle triangle, Matrix4f transformationMatrix, int numIterations, Vector3f normal) {
        Vector3f[] points = triangle.getPoints();
        transformationMatrix = new Matrix4f(transformationMatrix);
        Vector3f p0 = transformationMatrix.transformPosition(new Vector3f(points[0]));
        Vector3f p1 = transformationMatrix.transformPosition(new Vector3f(points[1]));
        Vector3f p2 = transformationMatrix.transformPosition(new Vector3f(points[2]));


        Vector3f rayCastedPos;
        Vector3f centroid = new Vector3f(p0).add(p1).add(p2).div(3.0f);

        float vectorDiff = new Vector3f(movementVector).dot(normal) / numIterations;
        if (vectorDiff < 0.01f) {
            movementVector.sub(new Vector3f(normal).mul(vectorDiff));
        }
        Vector3f closestTrianglePoint;
        if (isPerpendicular(axis, normal) && !(Math.abs(normal.y) > 0.7f)) {
            Vector3f startingPoint = calculateClosestSegmentStartFromSegment(p0, p1, p2, axis, base, normal);
            float normalProj = new Vector3f(startingPoint).dot(normal);
            float axisNormalProj = new Vector3f(base).dot(normal);
            float penetrationDepth =  Math.abs(normalProj - axisNormalProj) + 0.0001f;
            if (Float.isNaN(normal.lengthSquared()) || penetrationDepth < 0) return -1;
            position.add(new Vector3f(normal).mul((radius - penetrationDepth) / numIterations));
            return penetrationDepth;
        } else {
            rayCastedPos = getRayCastedPos(centroid, base, axis, normal);
            closestTrianglePoint = getClosestPoint(triangle, p0, p1, p2, rayCastedPos, transformationMatrix);
        }
        Vector3f referencePoint;
        referencePoint = calculateClosestSegment(closestTrianglePoint,
                new Vector3f(base.x, base.y + radius, base.z),
                new Vector3f(tip.x, tip.y - radius, tip.z));
        float penetrationDepth = closestTrianglePoint.distance(referencePoint);
        Vector3f penetrationNormal = new Vector3f(referencePoint).sub(closestTrianglePoint).normalize();
        if (Float.isNaN(penetrationNormal.lengthSquared()) || penetrationDepth < 0) return -1;
        position.add(new Vector3f(penetrationNormal).mul(Math.max((radius - penetrationDepth) / numIterations, 0)));
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

    //Gets the slide that we need in order to move our capsule out of the way of other triangles.
    //Must be done right after triangle penetration resolution.
    private Vector3f getTangentialOffset(ArrayList<TrianglePlane> constraints, TrianglePlane refPlane, Matrix4f tbn, boolean vertical) {
        SimpleMatrix m = new SimpleMatrix(constraints.size() + 3, 2);
        SimpleMatrix b = new SimpleMatrix(constraints.size() + 3, 1);
        float hMin = this.tip.y - this.base.y;
        Vector3f p1 = new Vector3f(refPlane.p1()).mulDirection(tbn);
        Vector2f v1 = new Vector2f(p1.x, p1.y);

        Vector3f p2 = new Vector3f(refPlane.p2()).mulDirection(tbn);
        Vector2f v2 = new Vector2f(p2.x, p2.y);

        Vector3f p3 = new Vector3f(refPlane.p3()).mulDirection(tbn);
        Vector2f v3 = new Vector2f(p3.x, p3.y);
        Vector3f tBase = new Vector3f(base).mulDirection(tbn);


        for (int i = 0; i < constraints.size(); i++) {
            //height function

            //TODO: ENCODE CONSTRAINTS

            //Radius Constraint Encoding:
            //if (constraints.get(i).normal().dot(refPlane.normal()) > 0) continue;

            //TODO: replace with -dh/db, dh/da... using normals gives second derivative not first use two points instead
            Vector3f nC = constraints.get(i).normal();
            Vector3f nF = refPlane.normal();

            float dx = -nC.x / nC.y + nF.x / nF.y;
            float dz = -nC.z / nC.y + nF.z / nF.y;

            Vector3f T = new Vector3f();
            tbn.getRow(0, T);
            Vector3f B = new Vector3f();
            tbn.getRow(1, B);

            float gradT = dx * T.x + dz * T.z;
            float gradB = dx * B.x + dz * B.z;

            m.set(i, 0, gradT);
            m.set(i, 1, gradB);

            float yCeiling = constraints.get(i).p1().y
                    - (nC.x * (base.x - constraints.get(i).p1().x)
                    +  nC.z * (base.z - constraints.get(i).p1().z)) / nC.y;

            float yFloor = refPlane.p1().y
                    - (nF.x * (base.x - refPlane.p1().x)
                    +  nF.z * (base.z - refPlane.p1().z)) / nF.y;

            float currentGap = yCeiling - yFloor;
            float error = hMin - currentGap;

            float Ty = T.y;
            float By = B.y;

            float scale = Ty * Ty + By * By;
            if (scale > 1e-6f) {
//                float offsetT = error * Ty / scale;
//                float offsetB = error * By / scale;
                b.set(i, 0, error);
            } else {
                b.set(i, 0, 0);
            }


            //TODO: Handle Degenerate Cases
            //1. Parallel Lines: Skip those ones
            //2. Corners/Vertical ref plane:Rather than height function do the same thing except with width function
            //3. Zero b vector: return that immediately more of a optimization than a case to handle
            //4. NaNs and stuff from invert. return zero vector.
        }

        //Triangle Boundaries Encoding:
        //Here we will encode the triangle bounds into our matrices in order to make them constraints
        //edge1
        Vector3f centroid3D = new Vector3f(refPlane.p1()).add(refPlane.p2()).add(refPlane.p3()).div(3).mulDirection(tbn);
        Vector2f centroid = new Vector2f(centroid3D.x, centroid3D.y);
        Vector3f edge1 = new Vector3f(refPlane.p1()).sub(refPlane.p3());
        edge1.mulDirection(tbn);
        edge1.normalize();
        Vector2f edgeNormal = new Vector2f(-edge1.y, edge1.x);
        if (edgeNormal.dot(new Vector2f(centroid).sub(v1)) < 0) {
            edgeNormal.negate();
        }
        m.set(constraints.size(),0, edgeNormal.x);
        m.set(constraints.size(), 1, edgeNormal.y);
        b.set(constraints.size(), 0, edgeNormal.dot(v1) - edgeNormal.dot(new Vector2f(tBase.x, tBase.y)));
        //edge2
        Vector3f edge2 = new Vector3f(refPlane.p1()).sub(refPlane.p2());
        edge2.mulDirection(tbn);
        edge2.normalize();
        edgeNormal = new Vector2f(-edge2.y, edge2.x);
        if (edgeNormal.dot(new Vector2f(centroid).sub(v2)) < 0) {
            edgeNormal.negate();
        }
        m.set(constraints.size() + 1,0, edgeNormal.x);
        m.set(constraints.size() + 1, 1, edgeNormal.y);
        b.set(constraints.size() + 1, 0, edgeNormal.dot(v2) - edgeNormal.dot(new Vector2f(tBase.x, tBase.y)));
        //edge3
        Vector3f edge3 = new Vector3f(refPlane.p2()).sub(refPlane.p3());
        edge3.mulDirection(tbn);
        edge3.normalize();
        edgeNormal = new Vector2f(-edge3.y, edge3.x);
        if (edgeNormal.dot(new Vector2f(centroid).sub(v3)) < 0) {
            edgeNormal.negate();
        }
        m.set(constraints.size() + 2,0, edgeNormal.x);
        m.set(constraints.size() + 2, 1, edgeNormal.y);
        b.set(constraints.size() + 2, 0, edgeNormal.dot(v3) - edgeNormal.dot(new Vector2f(tBase.x, tBase.y)));



//        SimpleMatrix t = m.transpose()
//                .mult(m)
//                .invert()
//                .mult(m.transpose())
//                .mult(b);
//        float x = (float) t.get(0, 0);
//        float z = (float) t.get(1, 0);
//        Vector3f T = new Vector3f();
//        tbn.getRow(0, T);
//        Vector3f B = new Vector3f();
//        tbn.getRow(1, B);
//        Vector3f result = new Vector3f(T).mul(x).add(new Vector3f(B).mul(z));


        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable xVar = model.addVariable("x");
        Variable zVar = model.addVariable("z");
        Expression obj = model.addExpression("obj").weight(1);
        obj.set(xVar, xVar, 0.5f);
        obj.set(zVar, zVar, 0.5f);

        for (int i = 0; i < m.getNumRows(); i++) {
            double bVal = b.get(i, 0);
            if (Double.isInfinite(bVal) || Double.isNaN(bVal)) continue;
            Expression e = model.addExpression("c" + i).lower(b.get(i, 0));

            e.set(xVar, m.get(i, 0));
            e.set(zVar, m.get(i, 1));
        }

        Optimisation.Result r = model.minimise();
        if (r.getState().isFeasible()) {
            double solX = r.get(0).doubleValue();
            double solZ = r.get(1).doubleValue();
            Vector3f T = new Vector3f();
            tbn.getRow(0, T);
            Vector3f B = new Vector3f();
            tbn.getRow(1, B);
            Vector3f result = new Vector3f(T).mul((float)solX).add(new Vector3f(B).mul((float)solZ));
            return result;
        } else {
            return new Vector3f(0, 0, 0);
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



    private Vector3f getRayCastedPos(Vector3f finalPos, Vector3f origin, Vector3f direction, Vector3f normal) {
        float t = rayCast(finalPos, origin, direction, normal);
        Vector3f rayCastedPos = new Vector3f(origin).add(new Vector3f(direction).mul(t));
        return rayCastedPos;
    }

    private float rayCast(Vector3f finalPos, Vector3f origin, Vector3f direction, Vector3f normal) {
        return new Vector3f(finalPos).sub(origin).dot(normal) / direction.dot(normal);
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






    //Returns the first time of impact.
    public CollisionHit sweepCapsule(ArrayList<TriangleGroup> triangleGroups, Vector3f movementVector, Vector3f position) {
        this.setPosition(position);
        CollisionHit nearestHit = null;
        for (TriangleGroup group : triangleGroups) {
            Matrix4f transform = group.getTransformationMatrix();
            Matrix4f inverseTransform = new Matrix4f(transform).invert();
            for (Triangle triangle : group.getTriangles()) {
                if (!isColliding(triangle, transform)) {
                    CollisionHit hit = sweepCapsule(triangle, movementVector, transform, inverseTransform);
                    if (hit == null) continue;
                    float t = hit.t();
                    if (nearestHit == null && t != -1 || nearestHit != null && t < nearestHit.t() && t != -1) {
                        nearestHit = hit;
                    }
                }
            }
        }
        if (nearestHit == null) {
            nearestHit = new CollisionHit(movementVector.length(), new Vector3f(0, 0, 0));
        }
        float finalT = Math.clamp(nearestHit.t(), 0, movementVector.length());
        if (Float.isNaN(nearestHit.t())) {
            finalT = 0;
        }
        return new CollisionHit(finalT, nearestHit.normal());


    }


    private CollisionHit sweepCapsule(Triangle triangle, Vector3f movementVector, Matrix4f transformationMatrix, Matrix4f inverseTransform) {
        Vector3f p0 = transformationMatrix.transformPosition(new Vector3f(triangle.getPoints()[0]));
        Vector3f p1 = transformationMatrix.transformPosition(new Vector3f(triangle.getPoints()[1]));
        Vector3f p2 = transformationMatrix.transformPosition(new Vector3f(triangle.getPoints()[2]));
        Vector3f[] points = new Vector3f[] {p0, p1, p2};

        Vector3f e0 = new Vector3f(points[1]).sub(points[0]);
        Vector3f e1 = new Vector3f(points[2]).sub(points[1]);
        Vector3f normal = new Vector3f(e0).cross(e1).normalize();
        orientNormal(normal, p0);


        Vector3f centroid = new Vector3f(points[0]).add(points[1]).add(points[2]).div(3.0f);

        Vector3f rayCastedPos = getRayCastedPos(centroid, base, axis, normal);
        Vector3f closestTrianglePoint = getClosestPoint(triangle, points[0], points[1], points[2], rayCastedPos, transformationMatrix);
        Vector3f referencePoint = calculateClosestSegment(closestTrianglePoint,
                new Vector3f(base.x, base.y, base.z),
                new Vector3f(tip.x, tip.y, tip.z));
        Vector3f rayDir = new Vector3f(movementVector).normalize();
        float t = rayCast(referencePoint, points[0].sub(new Vector3f(normal).mul(radius)), rayDir, normal);
        Matrix4f invMatrix = new Matrix4f(transformationMatrix).invert();
        Vector3f modelPos = invMatrix.transformPosition(new Vector3f(referencePoint).add(new Vector3f(movementVector).mul(t)));
        if (triangle.isOnExtrudedTriangle(modelPos, radius)) {
            return new CollisionHit(t, normal);
        }
        return null;

    }




}
