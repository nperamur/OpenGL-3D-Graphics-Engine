package org.example.bvh;

import org.example.WorldObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class WorldObjectManager implements WorldObjectData {
    private ModelBVH modelBVH;



    public WorldObjectManager() {
        modelBVH = new ModelBVH();
    }

    public WorldObjectManager(ArrayList<WorldObject> worldObjects) {
        modelBVH = new ModelBVH();
        modelBVH.addData(worldObjects);
    }


    public ArrayList<TriangleGroup> getNearbyTrianglesWithCollisionsEnabled(Vector3f position) {
        return getNearbyTriangles(position, true);
    }

    public ArrayList<TriangleGroup> getNearbyTriangles(Vector3f position) {
        return getNearbyTriangles(position, false);
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

    private ArrayList<TriangleGroup> getNearbyTriangles(Vector3f position, boolean onlyWithCollisions) {
        ArrayList<WorldObject> worldObjects = modelBVH.getIntersectingNodeData(position);
        ArrayList<TriangleGroup> nearbyTriangles = new ArrayList<>();
        for (WorldObject worldObject : worldObjects) {
            if (onlyWithCollisions && !worldObject.hasCollision()) continue;

            Vector3f localPos = new Vector3f(position).mulPosition(worldObject.getInverseTransformationMatrix());
            TriangleGroup triangleGroup = new TriangleGroup(worldObject.getBVH().getIntersectingNodeData(localPos),
                                                    worldObject.getAABB(),
                    worldObject.getTransformationMatrix(), worldObject.getName());
            nearbyTriangles.add(triangleGroup);
        }
        return nearbyTriangles;
    }

    public void syncWorldObjects(ArrayList<WorldObject> worldObjects) {
        this.modelBVH.buildBVH(worldObjects);
    }


    public BVHFlattenedData getAllFlattenedBVHData() {
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
                        Vector3f newVertex = new Vector3f(vertex).mulPosition(worldObject.getTransformationMatrix());
                        vertices.add(newVertex.x);
                        vertices.add(newVertex.y);
                        vertices.add(newVertex.z);
                    }
                }
            }
            int modelGlobalStartNodeIndex = triangleBVH.size() / 10;
            modelStartIndices.put(worldObject, modelGlobalStartNodeIndex);
            modelStartToWorldObjectIndices.put(modelGlobalStartNodeIndex, worldObjectIndex);
            int nodeCount = 0;
            if (worldObject.hasRaytracedShadows()) {
                ArrayList<BVHNodeData<Triangle>> nodeData = worldObject.getBVH().preorderNodeData();
                for (BVHNodeData<Triangle> data : nodeData) {
                    Vector3f lMin = data.aabb().getMin();
                    Vector3f lMax = data.aabb().getMax();

                    Vector3f[] corners = {
                            new Vector3f(lMin.x, lMin.y, lMin.z),
                            new Vector3f(lMax.x, lMin.y, lMin.z),
                            new Vector3f(lMin.x, lMax.y, lMin.z),
                            new Vector3f(lMin.x, lMin.y, lMax.z),
                            new Vector3f(lMax.x, lMax.y, lMin.z),
                            new Vector3f(lMax.x, lMin.y, lMax.z),
                            new Vector3f(lMin.x, lMax.y, lMax.z),
                            new Vector3f(lMax.x, lMax.y, lMax.z)
                    };

                    Vector3f wMin = new Vector3f(Float.POSITIVE_INFINITY);
                    Vector3f wMax = new Vector3f(Float.NEGATIVE_INFINITY);

                    Matrix4f mat = worldObject.getTransformationMatrix();

                    for (Vector3f corner : corners) {
                        corner.mulPosition(mat);
                        wMin.x = Math.min(wMin.x, corner.x);
                        wMin.y = Math.min(wMin.y, corner.y);
                        wMin.z = Math.min(wMin.z, corner.z);

                        wMax.x = Math.max(wMax.x, corner.x);
                        wMax.y = Math.max(wMax.y, corner.y);
                        wMax.z = Math.max(wMax.z, corner.z);
                    }

                    triangleBVH.add(wMin.x);
                    triangleBVH.add(wMin.y);
                    triangleBVH.add(wMin.z);
                    triangleBVH.add(wMax.x);
                    triangleBVH.add(wMax.y);
                    triangleBVH.add(wMax.z);
                    triangleBVH.add((float) (data.isLeaf() ? 1 : 0));
                    if (data.equals(nodeData.getLast())) {
                        triangleBVH.add((float) (triangleBVH.size() / 10 + 1));
                    } else {
                        triangleBVH.add(modelGlobalStartNodeIndex + (float) data.index());
                    }
                    triangleBVH.add((float) data.nodeCount());
                    if (data.firstNode() == null) {
                        triangleBVH.add((float) -1);
                    } else {
                        triangleBVH.add(Float.valueOf(triangleStartIndices.get(data.firstNode())));
                    }
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




        return new BVHFlattenedData(vertexResult, triangleBVHResult, modelBVHResult);
    }


}
