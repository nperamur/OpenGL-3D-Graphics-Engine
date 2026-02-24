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


}
