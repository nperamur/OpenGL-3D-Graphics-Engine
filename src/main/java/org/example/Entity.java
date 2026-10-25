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
        this.position.x += deltaX;
        this.position.y += deltaY;
        this.position.z += deltaZ;
        this.velocityPosition.x = deltaX;
        this.velocityPosition.y = deltaY;
        this.velocityPosition.z = deltaZ;
    }

    public void increaseRotation(float deltaX, float deltaY, float deltaZ) {
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
        this.position = position;
    }

    public float getRotX() {
        return finalRotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return finalRotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return finalRotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public float getScale() {
        return finalScale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void render(GeneralShader shader) {
        Matrix4f transformationMatrix = GameMath.createTransformationMatrix(finalPosition, finalRotX, finalRotY, finalRotZ, finalScale);
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadPrevTransform(prevTransform);
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.model.getTexture().getTextureID());
        Renderer.renderModel(this.model.getRawModel());
    }

    private void savePrevTransform() {
        prevTransform = GameMath.createTransformationMatrix(finalPosition, finalRotX, finalRotY, finalRotZ, finalScale);
    }

    public void setRotation(Vector3f vec) {
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
        savePrevTransform();
        boolean isMoving = !this.velocityPosition.equals(new Vector3f(0, 0, 0))
                || !this.velocityRotation.equals(new Vector3f(0, 0, 0));

        if (!hasCollision()) {
            move(dt);
        } else if (isMoving) {
            for (int i = 0; i < numIterations; i++) {
                move((1.0f / numIterations) * dt);
                Matrix4f transform = GameMath.createTransformationMatrix(finalPosition, finalRotX, finalRotY, finalRotZ, finalScale);
                Matrix4f inverseTransform = new Matrix4f(transform).invert();
                Vector3f playerPos = new Vector3f(Main.getDisplayManager().getPlayer().getPosition());
                playerCapsule.setPosition(playerPos);
                Vector3f localBase = inverseTransform.transformPosition(new Vector3f(this.playerCapsule.getBase()));
                Vector3f localTip = inverseTransform.transformPosition(new Vector3f(this.playerCapsule.getTip()));
                Vector3f scale = transform.getScale(new Vector3f());
                float localRadius = this.playerCapsule.getRadius() / Math.max(scale.x, Math.max(scale.y, scale.z));

                boolean intersectsAABB = this.getAABB().intersectsCapsule(localBase,
                        localTip, localRadius);
                if (intersectsAABB) {
                    Vector3f r = new Vector3f(playerPos).sub(finalPosition);
                    Vector3f angularVel = new Vector3f(velocityRotation).mul((float)Math.PI / 180f);
                    Vector3f tangentialVelocity = new Vector3f(angularVel).cross(r);
                    Vector3f effectiveVelocity = new Vector3f(velocityPosition).add(tangentialVelocity).mul((1.0f / numIterations) * dt).negate();

                    Vector3f localVelocity = inverseTransform.transformDirection(effectiveVelocity);
                    ArrayList<Triangle> triangles = getBVH().getIntersectingCapsuleNodeData(localBase,
                            localTip, localRadius, localVelocity);
                    TriangleGroup group = new TriangleGroup(triangles, getAABB(), transform, this.getName());
                    ArrayList<TriangleGroup> groups = new ArrayList<>();
                    groups.add(group);

                    if (playerCapsule.isCollided(groups, playerPos)) {
                        playerCapsule.resolveCollisions(groups, playerPos, effectiveVelocity, 20);
                        Main.getDisplayManager().getPlayer().setPosition(playerPos);
                        this.playerCapsule.setPosition(playerPos);
                    }

                }
            }
        }
    }

    private void move(float moveAmount) {
        this.finalPosition = new Vector3f(this.finalPosition).add(new Vector3f(velocityPosition).mul(moveAmount));
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
