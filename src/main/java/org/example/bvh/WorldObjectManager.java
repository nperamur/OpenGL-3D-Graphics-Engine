package org.example.bvh;

import org.example.WorldObject;
import org.example.player.PlayerCapsule;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class WorldObjectManager implements WorldObjectData {
    private ModelBVH modelBVH;


    private float[] modelBVHResult;


    public WorldObjectManager() {
        modelBVH = new ModelBVH();
    }

    public WorldObjectManager(ArrayList<WorldObject> worldObjects) {
        modelBVH = new ModelBVH();
        modelBVH.addData(worldObjects);
    }


    public ArrayList<TriangleGroup> getNearbyTrianglesWithCollisionsEnabled(Vector3f position, PlayerCapsule capsule, Vector3f movement) {
        return getNearbyTriangles(true, capsule, movement);
    }

    public ArrayList<TriangleGroup> getNearbyTriangles(Vector3f position, PlayerCapsule capsule, Vector3f movement) {
        return getNearbyTriangles(false, capsule, movement);
    }

    @Override
    public ArrayList<TriangleGroup> rayCastNearbyTrianglesWithCollisionsEnabled(Vector3f position, Vector3f direction) {
        ArrayList<WorldObject> worldObjects = modelBVH.getRayCastedNodeData(position, direction);
        ArrayList<TriangleGroup> nearbyTriangles = new ArrayList<>();
        for (WorldObject worldObject : worldObjects) {
            if (!worldObject.hasCollision()) continue;

            Vector3f localPos = new Vector3f(position).mulPosition(worldObject.getInverseTransformationMatrix());
            Vector3f localDir = new Vector3f(direction).mulDirection(worldObject.getInverseTransformationMatrix());

            TriangleGroup triangleGroup = new TriangleGroup(
                    worldObject.getBVH().getRayCastedNodeData(localPos, localDir),
                    worldObject.getAABB(),
                    worldObject.getTransformationMatrix(),
                    worldObject.getName()
            );
            nearbyTriangles.add(triangleGroup);
        }
        return nearbyTriangles;
    }

    private ArrayList<TriangleGroup> getNearbyTriangles(boolean onlyWithCollisions, PlayerCapsule capsule, Vector3f movementVector) {
        ArrayList<WorldObject> worldObjects = modelBVH.getIntersectingCapsuleNodeData(capsule.getBase(), capsule.getTip(), capsule.getRadius(), movementVector);
        ArrayList<TriangleGroup> nearbyTriangles = new ArrayList<>();
        for (WorldObject worldObject : worldObjects) {
            if (onlyWithCollisions && !worldObject.hasCollision()) continue;

            Matrix4f inverseTransform = worldObject.getTransformationMatrix().invert(new Matrix4f());

            Vector3f localBase   = inverseTransform.transformPosition(new Vector3f(capsule.getBase()));
            Vector3f localTip    = inverseTransform.transformPosition(new Vector3f(capsule.getTip()));
            Vector3f scale       = worldObject.getTransformationMatrix().getScale(new Vector3f());
            float    localRadius = capsule.getRadius() / Math.max(scale.x, Math.max(scale.y, scale.z));

            TriangleGroup triangleGroup = new TriangleGroup(
                    worldObject.getBVH().getIntersectingCapsuleNodeData(localBase, localTip, localRadius, movementVector),
                    worldObject.getAABB(),
                    worldObject.getTransformationMatrix(),
                    worldObject.getName());
            nearbyTriangles.add(triangleGroup);
        }
        return nearbyTriangles;
    }

    public void syncWorldObjects(ArrayList<WorldObject> worldObjects) {
        this.modelBVH.buildBVH(worldObjects);
    }


    public BVHFlattenedData getAllFlattenedBVHData(HashMap<WorldObject, Integer> transformationIndices) {
        ArrayList<WorldObject> worldObjects = modelBVH.preorder();
        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Float> triangleBVH = new ArrayList<>();
        HashMap<WorldObject, Integer> modelStartIndices = new HashMap<>();
        HashMap<Integer, Integer> modelStartToWorldObjectIndices = new HashMap<>();
        HashMap<Integer, Integer> worldObjectIndicesToNodeCount = new HashMap<>();
        int worldObjectIndex = 0;
        for (WorldObject worldObject : worldObjects) {
            HashMap<Triangle, Integer> triangleStartIndices = new HashMap<>();
            if (worldObject.hasRaytracedShadows()) {
                for (Triangle triangle : worldObject.getBVH().preorder()) {
                    triangleStartIndices.put(triangle, vertices.size() / 9);
                    for (Vector3f vertex : triangle.getPoints()) {
//                        Vector3f newVertex = new Vector3f(vertex).mulPosition(worldObject.getTransformationMatrix());
//                        vertices.add(newVertex.x);
//                        vertices.add(newVertex.y);
//                        vertices.add(newVertex.z);
                        vertices.add(vertex.x);
                        vertices.add(vertex.y);
                        vertices.add(vertex.z);
                    }
                }
            }
            int transformationIndex = transformationIndices.get(worldObject);
            int modelGlobalStartNodeIndex = triangleBVH.size() / 11;
            modelStartIndices.put(worldObject, modelGlobalStartNodeIndex);
            modelStartToWorldObjectIndices.put(modelGlobalStartNodeIndex, worldObjectIndex);
            int nodeCount = 0;
            if (worldObject.hasRaytracedShadows()) {
                ArrayList<BVHNodeData<Triangle>> nodeData = worldObject.getBVH().preorderNodeData();
                for (BVHNodeData<Triangle> data : nodeData) {
                    Vector3f lMin = data.aabb().getMin();
                    Vector3f lMax = data.aabb().getMax();

//                    Vector3f[] corners = {
//                            new Vector3f(lMin.x, lMin.y, lMin.z),
//                            new Vector3f(lMax.x, lMin.y, lMin.z),
//                            new Vector3f(lMin.x, lMax.y, lMin.z),
//                            new Vector3f(lMin.x, lMin.y, lMax.z),
//                            new Vector3f(lMax.x, lMax.y, lMin.z),
//                            new Vector3f(lMax.x, lMin.y, lMax.z),
//                            new Vector3f(lMin.x, lMax.y, lMax.z),
//                            new Vector3f(lMax.x, lMax.y, lMax.z)
//                    };

//                    Vector3f wMin = new Vector3f(Float.POSITIVE_INFINITY);
//                    Vector3f wMax = new Vector3f(Float.NEGATIVE_INFINITY);

//                    Matrix4f mat = worldObject.getTransformationMatrix();

//                    for (Vector3f corner : corners) {
////                        corner.mulPosition(mat);
//                        wMin.x = Math.min(wMin.x, corner.x);
//                        wMin.y = Math.min(wMin.y, corner.y);
//                        wMin.z = Math.min(wMin.z, corner.z);
//
//                        wMax.x = Math.max(wMax.x, corner.x);
//                        wMax.y = Math.max(wMax.y, corner.y);
//                        wMax.z = Math.max(wMax.z, corner.z);
//                    }

                    triangleBVH.add(lMin.x);
                    triangleBVH.add(lMin.y);
                    triangleBVH.add(lMin.z);
                    triangleBVH.add(lMax.x);
                    triangleBVH.add(lMax.y);
                    triangleBVH.add(lMax.z);
                    triangleBVH.add((float) (data.isLeaf() ? 1 : 0));
                    if (data.equals(nodeData.getLast())) {
                        triangleBVH.add((float) (triangleBVH.size() / 11 + 1));
                    } else {
                        triangleBVH.add(modelGlobalStartNodeIndex + (float) data.index());
                    }
                    triangleBVH.add((float) data.nodeCount());
                    if (data.firstNode() == null) {
                        triangleBVH.add((float) -1);
                    } else {
                        triangleBVH.add(Float.valueOf(triangleStartIndices.get(data.firstNode())));
                    }
                    triangleBVH.add((float) transformationIndex);
                    nodeCount++;
                }
            }
            worldObjectIndicesToNodeCount.put(worldObjectIndex, nodeCount);
            worldObjectIndex++;
        }
        ArrayList<BVHNodeData<WorldObject>> nodeData = modelBVH.preorderNodeData();
        ArrayList<Float> modelBVH = new ArrayList<>();
        for (BVHNodeData<WorldObject> data : nodeData) {
            modelBVH.add(data.aabb().getMin().x);
            modelBVH.add(data.aabb().getMin().y);
            modelBVH.add(data.aabb().getMin().z);
            modelBVH.add(data.aabb().getMax().x);
            modelBVH.add(data.aabb().getMax().y);
            modelBVH.add(data.aabb().getMax().z);
            modelBVH.add((float) (data.isLeaf() ? 1 : 0));
            if (data.equals(nodeData.getLast())) {
                modelBVH.add((float) (modelBVH.size() / 10 + 1));
            } else {
                modelBVH.add((float) data.index());
            }
            int sum = 0;
            if (data.firstNode() != null) {
                int startWorldObjectIndex = modelStartToWorldObjectIndices.get(modelStartIndices.get(data.firstNode()));
                for (int i = 0; i < data.nodeCount(); i++) {
                    Integer count = worldObjectIndicesToNodeCount.get(startWorldObjectIndex + i);
                    if (count != null) sum += count;
                }
            }
            modelBVH.add((float) sum);
            if (data.firstNode() == null) {
                modelBVH.add((float) -1);
            } else {
                modelBVH.add(Float.valueOf(modelStartIndices.get(data.firstNode())));
            }
        }
        float[] modelBVHResult = new float[modelBVH.size()];
        for (int i = 0; i < modelBVH.size(); i++) modelBVHResult[i] = modelBVH.get(i);
        float[] vertexResult = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) vertexResult[i] = vertices.get(i);
        float[] triangleBVHResult = new float[triangleBVH.size()];
        for (int i = 0; i < triangleBVH.size(); i++) triangleBVHResult[i] = triangleBVH.get(i);

        this.modelBVHResult = modelBVHResult;


        return new BVHFlattenedData(vertexResult, triangleBVHResult, modelBVHResult);
    }


    public float[] refitModelBVH() {
        if (modelBVHResult == null) {
            throw new IllegalStateException("Call getAllFlattenedBVHData at least once before using this method");
        }
        modelBVH.refit();
        ArrayList<BVHNodeData<WorldObject>> data = modelBVH.preorderNodeData();
        for (int i = 0; i < modelBVHResult.length; i += 10) {
            AABB aabb = data.get(i / 10).aabb();
            modelBVHResult[i] = aabb.getMin().x;
            modelBVHResult[i + 1] = aabb.getMin().y;
            modelBVHResult[i + 2] = aabb.getMin().z;
            modelBVHResult[i + 3] = aabb.getMax().x;
            modelBVHResult[i + 4] = aabb.getMax().y;
            modelBVHResult[i + 5] = aabb.getMax().z;
        }
        return modelBVHResult;

    }


}
