package org.example.bvh;

import org.example.WorldObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModelBVH extends AbstractBVH<WorldObject> {

    private static final int NUM_LEAF_MODELS = 4;

    public ModelBVH() {
        super(NUM_LEAF_MODELS);
    }


    public void addData(ArrayList<WorldObject> models) {
        if (super.getRoot() != null) {
            models.addAll(getData());
        }
        buildBVH(models);
    }

    @Override
    protected AABB createAABB(List<WorldObject> models) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        for (WorldObject model : models) {
            AABB aabb = model.getAABB();
            Matrix4f mat = model.getTransformationMatrix();

            Vector3f lMin = aabb.getMin();
            Vector3f lMax = aabb.getMax();
            Vector3f[] corners = {
                    new Vector3f(lMin.x, lMin.y, lMin.z), new Vector3f(lMax.x, lMin.y, lMin.z),
                    new Vector3f(lMin.x, lMax.y, lMin.z), new Vector3f(lMin.x, lMin.y, lMax.z),
                    new Vector3f(lMax.x, lMax.y, lMin.z), new Vector3f(lMax.x, lMin.y, lMax.z),
                    new Vector3f(lMin.x, lMax.y, lMax.z), new Vector3f(lMax.x, lMax.y, lMax.z)
            };

            for (Vector3f c : corners) {
                c.mulPosition(mat);
                min.min(c);
                max.max(c);
            }
        }



        return new AABB(min, max);
    }

//    @Override
//    protected ArrayList<WorldObject>[] partition(BVHNode<WorldObject> node, ArrayList<WorldObject> models) {
//        ArrayList<WorldObject>[] partition = (ArrayList<WorldObject>[]) new ArrayList[2];
//        Axis longestAxis = node.getLongestAxis();
//
//        models.sort((a, b) -> {
//            float aCenter = switch (longestAxis) {
//                case X -> a.getAABB().getMin().x + a.getPosition().x + (a.getAABB().getMax().x - a.getAABB().getMin().x) / 2;
//                case Y -> a.getAABB().getMin().y + a.getPosition().y + (a.getAABB().getMax().y - a.getAABB().getMin().y) / 2;
//                case Z -> a.getAABB().getMin().z + a.getPosition().z + (a.getAABB().getMax().z - a.getAABB().getMin().z) / 2;
//            };
//            float bCenter = switch (longestAxis) {
//                case X -> b.getAABB().getMin().x + b.getPosition().x + (b.getAABB().getMax().x - b.getAABB().getMin().x) / 2;
//                case Y -> b.getAABB().getMin().y + b.getPosition().y + (b.getAABB().getMax().y - b.getAABB().getMin().y) / 2;
//                case Z -> b.getAABB().getMin().z + b.getPosition().z + (b.getAABB().getMax().z - b.getAABB().getMin().z) / 2;
//            };
//            return Float.compare(aCenter, bCenter);
//        });
//
//        int mid = models.size() / 2;
//        partition[0] = new ArrayList<>(models.subList(0, mid));
//        partition[1] = new ArrayList<>(models.subList(mid, models.size()));
//        return partition;
//    }


//    @Override
//    protected ArrayList<WorldObject>[] partition(BVHNode<WorldObject> node, ArrayList<WorldObject> models) {
//        ArrayList<WorldObject>[] partition = (ArrayList<WorldObject>[]) new ArrayList[2];
//        partition[0] = new ArrayList<>();
//        partition[1] = new ArrayList<>();
//        Axis longestAxis = node.getLongestAxis();
//        Vector3f center = node.getCenter();
//        for (WorldObject model : models) {
//            Vector3f worldPos = model.getPosition();
//            AABB aabb = model.getAABB();
//            float modelMin = 0;
//            float modelMax = 0;
//            float split = 0;
//            switch (longestAxis) {
//                case X -> {
//                    modelMin = aabb.getMin().x + worldPos.x;
//                    modelMax = aabb.getMax().x + worldPos.x;
//                    split = center.x;
//                }
//                case Y -> {
//                    modelMin = aabb.getMin().y + worldPos.y;
//                    modelMax = aabb.getMax().y + worldPos.y;
//                    split = center.y;
//                }
//                case Z -> {
//                    modelMin = aabb.getMin().z + worldPos.z;
//                    modelMax = aabb.getMax().z + worldPos.z;
//                    split = center.z;
//                }
//            }
//
//            if ((modelMax + modelMin) / 2 <= split) {
//                partition[0].add(model);
//            } else {
//                partition[1].add(model);
//            }
//        }
//        if (partition[0].isEmpty() || partition[1].isEmpty()) {
//            return null;
//        }
//        return partition;
//    }




    @Override
    protected ArrayList<WorldObject>[] partition(BVHNode<WorldObject> node, ArrayList<WorldObject> worldObjects) {
        float numBins = 32;
        ArrayList<WorldObject>[] finalPartition;
        sortObjects(worldObjects, node);
        if (worldObjects.size() > numBins) {
            int stepSize = (int) (worldObjects.size() / numBins);
            finalPartition = testSplits(worldObjects, stepSize, node);
        } else {
            finalPartition = testSplits(worldObjects, 1, node);
        }
        if (finalPartition[0].isEmpty() || finalPartition[1].isEmpty()) {
            return null;
        }
        return finalPartition;
    }


    private ArrayList<WorldObject>[] testSplits(ArrayList<WorldObject> worldObjects, int stepSize, BVHNode<WorldObject> node) {
        ArrayList<WorldObject>[] partition = (ArrayList<WorldObject>[]) new ArrayList[2];
        partition[0] = new ArrayList<>();
        partition[1] = new ArrayList<>();
        int minCostIndex = 0;
        float minCost = Float.POSITIVE_INFINITY;
        for (int i = 0; i < worldObjects.size() - 1; i += stepSize) {
            float cost = evaluateSplit(worldObjects, i, node);
            if (cost <= minCost) {
                minCost = cost;
                minCostIndex = i;
            }
        }
        partition[0] = new ArrayList<>(worldObjects.subList(0, minCostIndex + 1));
        partition[1] = new ArrayList<>(worldObjects.subList(minCostIndex + 1, worldObjects.size()));
        return partition;
    }

    private void sortObjects(ArrayList<WorldObject> worldObjects, BVHNode<WorldObject> node) {
        Axis axis = node.getBoundingBox().getLongestAxis();

        worldObjects.sort((a, b) -> {
            float centerA = getVectorOnAxis(new Vector3f(a.getAABB().getCenter()).mulPosition(new Matrix4f(a.getTransformationMatrix())), axis);
            float centerB = getVectorOnAxis(new Vector3f(b.getAABB().getCenter()).mulPosition(new Matrix4f(b.getTransformationMatrix())), axis);
            return Float.compare(centerA, centerB);
        });
    }

    private float getVectorOnAxis(Vector3f vector, Axis axis) {
        switch (axis) {
            case X -> {
                return vector.x;
            }
            case Y -> {
                return vector.y;
            }
            case Z -> {
                return vector.z;
            }
        }
        return -1;
    }


    public AABB getAABB() {
        return super.getRoot().getBoundingBox();
    }

    private float evaluateSplit(ArrayList<WorldObject> worldObjects, int i, BVHNode<WorldObject> parent) {
        List<WorldObject> leftArray = worldObjects.subList(0, i + 1);
        AABB leftAABB = createAABB(leftArray);
        List<WorldObject> rightArray = worldObjects.subList(i + 1, worldObjects.size());
        AABB rightAABB = createAABB(rightArray);
        return evaluateSplit(leftAABB.getSurfaceArea(),
                rightAABB.getSurfaceArea(),
                parent.getBoundingBox().getSurfaceArea(),
                leftArray.size(),
                rightArray.size());
    }



    private float evaluateSplit(float surfaceAreaLeft, float surfaceAreaRight, float surfaceAreaParent,
                                int numLeft, int numRight) {

        if (surfaceAreaParent <= 0) return Float.POSITIVE_INFINITY;

        float costTraversal = 1.0f;
        float costIntersection = 1.5f;

        return costTraversal +
                (surfaceAreaLeft / surfaceAreaParent) * numLeft * costIntersection +
                (surfaceAreaRight / surfaceAreaParent) * numRight * costIntersection;
    }




}
