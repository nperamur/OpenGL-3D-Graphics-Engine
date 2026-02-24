package org.example.bvh;

import org.joml.Vector3f;

import java.util.ArrayList;

public class BVHNode<E> {
    private BVHNode<E> parent;
    private BVHNode<E> left;
    private BVHNode<E> right;

    private ArrayList<E> data;

    private AABB boundingBox;
    
    public BVHNode(AABB boundingBox, ArrayList<E> data) {
        this.boundingBox = boundingBox;
        this.data = data;
    }

    public BVHNode<E> getLeft() {
        return left;
    }

    public void setLeft(BVHNode<E> left) {
        this.left = left;
    }

    public BVHNode<E> getParent() {
        return parent;
    }

    public void setParent(BVHNode<E> parent) {
        this.parent = parent;
    }

    public BVHNode<E> getRight() {
        return right;
    }

    public void setRight(BVHNode<E> right) {
        this.right = right;
    }


    public void setData(ArrayList<E> data) {
        this.data = data;
    }

    Vector3f getCenter() {
        return boundingBox.getCenter();
    }

    Axis getLongestAxis() {
        return boundingBox.getLongestAxis();
    }

    boolean hasLeft() {
        return left != null;
    }

    boolean hasRight() {
        return right != null;
    }

    ArrayList<E> getData() {
        if (data != null) {
            return new ArrayList<>(data);
        }
        return new ArrayList<>();
    }

    boolean isLeaf() {
        return left == null && right == null;
    }

    AABB getBoundingBox() {
        return boundingBox;
    }



}
