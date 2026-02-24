package org.example.bvh;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBVH<E> {
    private BVHNode<E> root;

    private int numLeafNodes;

    private static boolean DEBUG_MODE = false;

    private int recursionCounter = 0;



    public AbstractBVH(int numLeafNodes) {
        this.numLeafNodes = numLeafNodes;
    }

    protected void buildBVH(ArrayList<E> data) {
        root = new BVHNode<E>(createAABB(data), null);
        buildBVH(data, root);
    }

    @SuppressWarnings("unchecked")
    protected void buildBVH(ArrayList<E> data, BVHNode<E> node) {
        if (data.size() <= numLeafNodes) {
            node.setData(data);
            if (DEBUG_MODE) {
                System.out.println("LEAF:" + data.size());
            }
            return;
        }

        ArrayList<E>[] partition = partition(node, data);
        if (partition == null || partition[0] == null || partition[1] == null) {
            node.setData(data);
            if (DEBUG_MODE) {
                System.out.println("LEAF:" + data.size());
            }
            return;
        }
        ArrayList<E> leftPartition = partition[0];
        ArrayList<E> rightPartition = partition[1];


        node.setLeft(new BVHNode<>(createAABB(leftPartition), null));
        node.setRight(new BVHNode<>(createAABB(rightPartition), null));
        node.getLeft().setParent(node);
        node.getRight().setParent(node);

        buildBVH(leftPartition, node.getLeft());
        buildBVH(rightPartition, node.getRight());


    }


    protected abstract AABB createAABB(List<E> data);

    protected abstract ArrayList<E>[] partition(BVHNode<E> node, ArrayList<E> data);


    ArrayList<E> getData() {
        return getData(root, new ArrayList<>());
    }

    protected ArrayList<E> getData(BVHNode<E> node, ArrayList<E> leaves) {
        if (node.isLeaf()) {
            leaves.addAll(node.getData());
            return leaves;
        }

        getData(node.getLeft(), leaves);
        getData(node.getRight(), leaves);
        return leaves;
    }

    protected BVHNode<E> getRoot() {
        return root;
    }


    AABB getRootBoundingBox() {
        return root.getBoundingBox();
    }


    public ArrayList<E> getIntersectingNodeData(Vector3f position) {
        recursionCounter = 0;
        return getIntersectingNodeData(position, root, new ArrayList<>());
    }

    private ArrayList<E> getIntersectingNodeData(Vector3f position, BVHNode<E> node, ArrayList<E> data) {
        recursionCounter++;
        if (DEBUG_MODE) {
            System.out.println(recursionCounter);
        }
        boolean inBox = node.getBoundingBox().inBox(position);
        if (inBox) {
            if (node.isLeaf()) {
                data.addAll(node.getData());
                return data;

            }
            if (node.hasLeft()) {
                getIntersectingNodeData(position, node.getLeft(), data);
            }
            if (node.hasRight()) {
                getIntersectingNodeData(position, node.getRight(), data);;
            }
        }
        return data;
    }


    public ArrayList<E> getRayCastedNodeData(Vector3f position, Vector3f direction) {
        if (DEBUG_MODE) {
            System.out.println(recursionCounter);
        }
        return getRayCastedNodeData(position, direction, root, new ArrayList<>());
    }

    private ArrayList<E> getRayCastedNodeData(Vector3f position, Vector3f direction, BVHNode<E> node, ArrayList<E> data) {
        if (direction.x == 0 && direction.y == 0 && direction.z == 0) {
            return data;
        }
        AABB aabb = node.getBoundingBox();

        Vector3f t1 = aabb.getMin();
        Vector3f t2 = aabb.getMax();
        if (direction.x < 0) {
            float temp = t1.x;
            t1.x = t2.x;
            t2.x = temp;
        }

        if (direction.y < 0) {
            float temp = t1.y;
            t1.y = t2.y;
            t2.y = temp;
        }

        if (direction.z < 0) {
            float temp = t1.z;
            t1.z = t2.z;
            t2.z = temp;
        }


        Vector3f tMin = new Vector3f(new Vector3f(t1).sub(position)).div(direction);
        Vector3f tMax = new Vector3f(new Vector3f(t2).sub(position)).div(direction);


        if (Float.isNaN(tMin.x) || direction.x == 0) {
            if (position.x < aabb.getMin().x || position.x > aabb.getMax().x) {
                tMin.x = Float.POSITIVE_INFINITY;
                tMax.x = Float.NEGATIVE_INFINITY;
            } else {
                tMin.x = Float.NEGATIVE_INFINITY;
                tMax.x = Float.POSITIVE_INFINITY;
            }
        }

        if (Float.isNaN(tMin.y) || direction.y == 0) {
            if (position.y < aabb.getMin().y || position.y > aabb.getMax().y) {
                tMin.y = Float.POSITIVE_INFINITY;
                tMax.y = Float.NEGATIVE_INFINITY;
            } else {
                tMin.y = Float.NEGATIVE_INFINITY;
                tMax.y = Float.POSITIVE_INFINITY;
            }
        }


        if (Float.isNaN(tMin.z) || direction.z == 0) {
            if (position.z < aabb.getMin().z || position.z > aabb.getMax().z) {
                tMin.z = Float.POSITIVE_INFINITY;
                tMax.z = Float.NEGATIVE_INFINITY;
            } else {
                tMin.z = Float.NEGATIVE_INFINITY;
                tMax.z = Float.POSITIVE_INFINITY;
            }
        }


        float tEntry = Math.max(tMin.x, Math.max(tMin.y, tMin.z));
        float tExit = Math.min(tMax.x, Math.min(tMax.y, tMax.z));
        if (tEntry > tExit || tExit < 0) {
            return data;
        }

        if (node.isLeaf()) {
            data.addAll(node.getData());
            return data;
        }
        if (node.hasLeft()) {
            getRayCastedNodeData(position, direction, node.getLeft(), data);
        }
        if (node.hasRight()) {
            getRayCastedNodeData(position, direction, node.getRight(), data);
        }

        return data;

    }

    public ArrayList<E> preorder() {
        ArrayList<E> array = new ArrayList<>();
        preorder(root, array);
        return array;
    }

    private void preorder(BVHNode<E> node, ArrayList<E> array) {
        if (node == null) return;
        array.addAll(node.getData());
        preorder(node.getLeft(), array);
        preorder(node.getRight(), array);

    }

    public ArrayList<BVHNodeData<E>> preorderNodeData() {
        ArrayList<BVHNodeData<E>> array = new ArrayList<>();
        preorderNodeData(root, array);
        return array;
    }

    private void preorderNodeData(BVHNode<E> node, ArrayList<BVHNodeData<E>> array) {
        if (node == null) return;
        int index = array.size();
        array.add(null);
        preorderNodeData(node.getLeft(), array);
        preorderNodeData(node.getRight(), array);
        array.set(index, buildNodeData(node, array.size(), node.isLeaf(), node.getData().size(),
                node.getData() == null || node.getData().isEmpty() ? null : node.getData().getFirst()));
    }

    private BVHNodeData<E> buildNodeData(BVHNode<E> node, int index, boolean isLeaf, int nodeCount, E firstNode) {
        return new BVHNodeData<E>(node.getBoundingBox(), index, isLeaf, nodeCount, firstNode);
    }



}
