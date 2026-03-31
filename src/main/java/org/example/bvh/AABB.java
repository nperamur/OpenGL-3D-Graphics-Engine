package org.example.bvh;

import org.joml.Vector3f;

public class AABB {

    private Vector3f min;

    private Vector3f max;
    public AABB(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public Vector3f getMin() {
        return new Vector3f(min);
    }

    public void setMin(Vector3f min) {
        this.min = min;
    }

    public Vector3f getMax() {
        return new Vector3f(max);
    }

    public void setMax(Vector3f max) {
        this.max = max;
    }

    public Vector3f getCenter() {
        return (new Vector3f(getMax()).add(getMin())).div(2);
    }

    public Axis getLongestAxis() {
        Vector3f size = getMax().sub(getMin());
        Axis longestAxis;
        if (size.x >= size.y && size.x >= size.z) longestAxis = Axis.X;
        else if (size.y >= size.z) longestAxis = Axis.Y;
        else longestAxis = Axis.Z;
        return longestAxis;
    }

    public boolean inBox(Vector3f position) {
        return position.x >= min.x && position.y >= min.y && position.z >= min.z
                && position.x <= max.x && position.y <= max.y && position.z <= max.z;
    }

    public float getSurfaceArea() {
        float dx = max.x - min.x;
        float dy = max.y - min.y;
        float dz = max.z - min.z;

        if (dx < 0 || dy < 0 || dz < 0) return 0f;
        return 2.0f * (dx * dy + dy * dz + dz * dx);
    }

    public static AABB merge(AABB a, AABB b) {
        Vector3f min = new Vector3f(
                Math.min(a.getMin().x, b.getMin().x),
                Math.min(a.getMin().y, b.getMin().y),
                Math.min(a.getMin().z, b.getMin().z)
        );
        Vector3f max = new Vector3f(
                Math.max(a.getMax().x, b.getMax().x),
                Math.max(a.getMax().y, b.getMax().y),
                Math.max(a.getMax().z, b.getMax().z)
        );
        return new AABB(min, max);
    }

    public static AABB translate(AABB a, Vector3f movement) {
        return new AABB(new Vector3f(a.getMin()).add(movement), new Vector3f(a.getMax()).add(movement));
    }

    public boolean intersectsCapsule(Vector3f capsuleStart, Vector3f capsuleEnd, float radius) {
        float distSq = closestDistanceSquaredToSegment(capsuleStart, capsuleEnd);
        return distSq <= radius * radius;
    }

    private float closestDistanceSquaredToSegment(Vector3f segStart, Vector3f segEnd) {
        Vector3f segDir = new Vector3f(segEnd).sub(segStart);
        float segLenSq = segDir.lengthSquared();
        //get dist along capsule axis to move to
        float t = 0f;
        if (segLenSq > 1e-10f) {
            Vector3f aabbCenter = getCenter();
            t = new Vector3f(aabbCenter).sub(segStart).dot(segDir) / segLenSq;
            t = Math.max(0f, Math.min(1f, t));
        }

        Vector3f closest = new Vector3f(segDir).mul(t).add(segStart);

        //clamp into box
        float cx = Math.max(min.x, Math.min(max.x, closest.x));
        float cy = Math.max(min.y, Math.min(max.y, closest.y));
        float cz = Math.max(min.z, Math.min(max.z, closest.z));

        //get diff between original & clamped pos
        float dx = cx - closest.x;
        float dy = cy - closest.y;
        float dz = cz - closest.z;
        return dx * dx + dy * dy + dz * dz;
    }

}
