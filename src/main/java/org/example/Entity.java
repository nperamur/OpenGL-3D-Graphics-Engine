package org.example;

import org.example.bvh.AABB;
import org.example.bvh.Triangle;
import org.example.bvh.TriangleBVH;
import org.example.bvh.TriangleGroup;
import org.example.player.PlayerCapsule;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Collection;

public class Entity implements WorldObject {
    private TexturedModel model;
    private Vector3f position;
    private float rotX, rotY, rotZ;
    private float scale;
    private Matrix4f prevTransform;

    private float finalRotX, finalRotY, finalRotZ;
    private Vector3f finalPosition = new Vector3f();
    private float finalScale;

    private Vector3f velocityRotation = new Vector3f();


    private Vector3f velocityPosition = new Vector3f();


    private PlayerCapsule playerCapsule;





    public Entity(TexturedModel model, Vector3f position, float rotZ, float rotY, float rotX, float scale) {
        this.model = model;
        this.position = position;
        this.rotZ = rotZ;
        this.rotY = rotY;
        this.rotX = rotX;
        this.scale = scale;
        savePrevTransform();
        playerCapsule = Main.getDisplayManager().getPlayer().getCollisionCapsule();
    }

    public void increasePosition(float deltaX, float deltaY, float deltaZ) {
        savePrevTransform();
        this.position.x += deltaX;
        this.position.y += deltaY;
        this.position.z += deltaZ;
        this.velocityPosition.x = deltaX;
        this.velocityPosition.y = deltaY;
        this.velocityPosition.z = deltaZ;
    }

    public void increaseRotation(float deltaX, float deltaY, float deltaZ) {
        savePrevTransform();
        this.rotX += deltaX;
        this.rotY += deltaY;
        this.rotZ += deltaZ;
        this.velocityRotation.x = deltaX;
        this.velocityRotation.y = deltaY;
        this.velocityRotation.z = deltaZ;
    }


    public TexturedModel getModel() {
        return model;
    }

    public Vector3f getPosition() {
        return position;
    }

    @Override
    public TriangleBVH getBVH() {
        return model.getBVH();
    }

    @Override
    public Matrix4f getInverseTransformationMatrix() {
        return GameMath.createTransformationMatrix(this.getPosition(), this.getRotX(), this.getRotY(), this.getRotZ(), this.getScale()).invert();
    }

    @Override
    public boolean hasCollision() {
        return model.hasCollision();
    }

    @Override
    public boolean hasRaytracedShadows() {
        return model.hasRaytracedShadows();
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return GameMath.createTransformationMatrix(this.getPosition(), this.getRotX(), this.getRotY(), this.getRotZ(), this.getScale());
    }

    public void setPosition(Vector3f position) {
        savePrevTransform();
        this.position = position;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        savePrevTransform();
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        savePrevTransform();
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        savePrevTransform();
        this.rotZ = rotZ;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        savePrevTransform();
        this.scale = scale;
    }

    public void render(GeneralShader shader) {
        Matrix4f transformationMatrix = GameMath.createTransformationMatrix(position, rotX, rotY, rotZ, scale);
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadPrevTransform(prevTransform);
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.model.getTexture().getTextureID());
        Renderer.renderModel(this.model.getRawModel());
    }

    private void savePrevTransform() {
        prevTransform = GameMath.createTransformationMatrix(position, rotX, rotY, rotZ, scale);
    }

    public void setRotation(Vector3f vec) {
        savePrevTransform();
        this.rotX = vec.x;
        this.rotY = vec.y;
        this.rotZ = vec.z;

    }

    @Override
    public AABB getAABB() {
        return model.getAABB();
    }


    public void tick(float dt, int numIterations) {
        updatePositions();
        if (!hasCollision()) {
            move(dt);
        } else {
            for (int i = 0; i < numIterations; i++) {
                move((1.0f / numIterations) * dt);
                boolean intersectsAABB = this.getBVH().getAABB().intersectsCapsule(playerCapsule.getBase(),
                        playerCapsule.getTip(), playerCapsule.getRadius());
                if (intersectsAABB) {
                    ArrayList<Triangle> triangles = getBVH().getIntersectingCapsuleNodeData(playerCapsule.getBase(),
                            playerCapsule.getTip(), playerCapsule.getRadius(), new Vector3f(0, 0, 0));
                    TriangleGroup group = new TriangleGroup(triangles, getAABB(), this.getTransformationMatrix(), this.getName());
                    ArrayList<TriangleGroup> groups = new ArrayList<>();
                    groups.add(group);
                    playerCapsule.resolveCollisions(groups, playerCapsule.getBase(), new Vector3f(0, 0, 0), 20);
                }
            }
        }
    }

    private void move(float moveAmount) {
        this.finalPosition = this.finalPosition.add(velocityPosition.mul(moveAmount));
        this.finalRotX += velocityRotation.x * moveAmount;
        this.finalRotY += velocityRotation.y * moveAmount;
        this.finalRotZ += velocityRotation.z * moveAmount;

    }

    //make sure to call this after processing the physics frame...
    public void resetVelocities() {
        velocityPosition.set(0, 0, 0);
        velocityRotation.set(0, 0, 0);
    }

    private void updatePositions() {
        this.finalPosition = new Vector3f(position);
        this.finalRotX = rotX;
        this.finalRotY = rotY;
        this.finalRotZ = rotZ;
        this.finalScale = scale;
    }


}
