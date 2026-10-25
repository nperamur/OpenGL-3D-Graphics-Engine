package org.example.player;

import org.example.*;
import org.example.bvh.*;
import org.example.terrain.Terrain;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Player {
    private Vector3f position = new Vector3f(0, 100, 0);

    private Vector3f finalPosition = new Vector3f(0, 100, 0);
    private double pitch;
    private double yaw;
    private double roll;
    private long window;
    private double prevX;
    private double prevY;
    private boolean locked;
    private double jumpStartTime = 0;
    private boolean isJumping;
    private float jumpHeight;
    private float fallHeight;
    private float currentHeightOfTerrain;
    private boolean crouching;
    private boolean isCollided;
    private static final float GRAVITY = 1f;
    private float movementSpeed = 0.2f;
    private boolean sprinting;
    private double fallStartTime;
    private Item heldItem;


    private boolean wPressed, aPressed, sPressed, dPressed;
    private boolean leftPressed, rightPressed, upArrowPressed, downArrowPressed;
    private boolean spacePressed, leftShiftPressed, leftControlPressed;


    private float yOffset;


    private Vector3f prevFramePos = new Vector3f();

    private PlayerCapsule playerCapsule;


    public Player(double pitch, double yaw, double roll, long window) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.window = window;
        this.playerCapsule = new PlayerCapsule(1f, new Vector3f(this.position), 2.5f);
        GLFW.glfwSetCursorPosCallback(window, (n, xpos, ypos) -> {
            if (locked) {
                this.yaw += (xpos - prevX) * 0.1;
                if (Math.toRadians(this.pitch + (ypos - prevY) * 0.1) >= -Math.PI / 2 && Math.toRadians(this.pitch + (ypos - prevY) * 0.1) <= Math.PI / 2) {
                    this.pitch += (ypos - prevY) * 0.1;
                }
            }

            Main.getDisplayManager().getImGuiImplGlfw().cursorPosCallback(window, xpos, ypos);
            this.prevX = xpos;
            this.prevY = ypos;
        });
    }



    public Vector3f getPosition() {
        return new Vector3f(finalPosition);
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        this.finalPosition = new Vector3f(position);
    }

    public double getPitch() {
        return pitch;
    }

    public void invertPitch() {
        pitch = -pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }

    public void jump() {
        if (fallStartTime != -1) {
            return;
        }
        jumpStartTime = System.nanoTime();
//        if (!isCollided) {
//            jumpHeight = currentHeightOfTerrain;
//        } else {
//            jumpHeight = position.y;
//        }
        jumpHeight = position.y;
        this.isJumping = true;
    }

    public void handleInputs() {
        this.wPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        this.dPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
        this.aPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
        this.sPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
        this.leftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
        this.rightPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
        this.upArrowPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS && Math.toRadians(pitch) >= -Math.PI / 2;
        this.downArrowPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS && Math.toRadians(pitch) <= Math.PI / 2;
        boolean enterPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS;
        boolean escapePressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
        this.spacePressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        boolean jPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_J) == GLFW.GLFW_PRESS;
        boolean leftShiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        boolean leftControlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

        if (enterPressed) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            locked = true;
        }
        if (escapePressed) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            locked = false;
        }
        if (jPressed) {
            ShaderProgram.reloadAllShaders();
        }

        crouching = leftShiftPressed;
        sprinting = leftControlPressed;





    }


    public void tick(float dt, int numIterations, Terrain terrain) {
        if (leftPressed) {
            yaw -= 200f * dt;
        }
        if (rightPressed) {
            yaw += 200f * dt;
        }
        if (upArrowPressed) {
            pitch -= 200f * dt;
        }
        if (downArrowPressed) {
            pitch += 200f * dt;
        }
        prevFramePos = new Vector3f(position);
        float heightOfTerrain = terrain.getHeightOfTerrainNotInterpolated(this.position.x, this.position.z);
        currentHeightOfTerrain = heightOfTerrain;
        if (isJumping || position.y <= heightOfTerrain) {
            fallStartTime = -1;
        }
        if (isJumping) {
            double t = System.nanoTime() - jumpStartTime;
            t /= 200000000;
            this.position.y = (float) (((double) -1 / 2 * GRAVITY * Math.pow(t, 2) + (t * 1.7f) + jumpHeight));
        } else if (position.y > heightOfTerrain && !isCollided) {
            if (fallStartTime == -1) {
                fallStartTime = 0;
                fallHeight = position.y;
            }
            this.position.y = (float) (-GRAVITY * Math.pow(fallStartTime, 2) * 13) + fallHeight;
            fallStartTime += dt;
        }
        WorldObjectData worldObjectData = Main.getDisplayManager().getRenderer().getWorldObjectData();
        boolean hasCollided = false;
        if (wPressed) {
            position.z -= (float) (movementSpeed * Math.cos(Math.toRadians(yaw)) * dt);
            position.x += (float) (movementSpeed * Math.sin(Math.toRadians(yaw)) * dt);
        }
        if (dPressed) {
            position.z -= (float) (movementSpeed * Math.cos(Math.toRadians(yaw + 90)) * dt);
            position.x += (float) (movementSpeed * Math.sin(Math.toRadians(yaw + 90)) * dt);
        }
        if (aPressed) {
            position.z += (float) (movementSpeed * Math.cos(Math.toRadians(yaw + 90)) * dt);
            position.x -= (float) (movementSpeed * Math.sin(Math.toRadians(yaw + 90)) * dt);
        }
        if (sPressed) {
            position.z += (float) (movementSpeed * Math.cos(Math.toRadians(yaw)) * dt);
            position.x -= (float) (movementSpeed * Math.sin(Math.toRadians(yaw)) * dt);
        }
        if (spacePressed && !isJumping) {
            jump();
        }
        Vector3f movementVector = new Vector3f(position).sub(prevFramePos).div(numIterations);
        position = new Vector3f(prevFramePos);
        ArrayList<TriangleGroup> triangleGroups = worldObjectData.getNearbyTrianglesWithCollisionsEnabled(position, playerCapsule, movementVector);
        boolean collided = false;
        Vector3f candidate = null;
        for (int i = 0; i < numIterations; i++) {
            if (!playerCapsule.isCollided(triangleGroups, position) && collided) {
                candidate = new Vector3f(position);
            }
            position.add(movementVector);
            hasCollided = playerCapsule.resolveCollisions(triangleGroups, position, movementVector, 20);
            if (hasCollided) {
                collided = true;
            }
            triangleGroups = worldObjectData.getNearbyTrianglesWithCollisionsEnabled(position, playerCapsule, movementVector);

        }
//                if (candidate != null && playerCapsule.isCollided(triangleGroups, position)) {
//                    position = candidate;
//                }

//                Vector3f movementVector = new Vector3f(position).sub(prevFramePos);
//                position = new Vector3f(prevFramePos);
//                if (movementVector.length() > 0.0001f) {
//                    ArrayList<TriangleGroup> triangleGroups = worldObjectData.rayCastNearbyTrianglesWithCollisionsEnabled(position, new Vector3f(movementVector));
//                    CollisionHit hit = playerCapsule.sweepCapsule(triangleGroups, movementVector, position);
//                    position.add(new Vector3f(movementVector).mul(hit.t()));
//                    Vector3f remaining = new Vector3f(movementVector).mul(1.0f - hit.t() / movementVector.length());
//                    Vector3f slide = remaining.sub(new Vector3f(hit.normal()).mul(remaining.dot(hit.normal())));
//                    position.add(slide);
//                    hasCollided = playerCapsule.resolveCollisions(triangleGroups, position, movementVector, 20);
//                }
        if (position.y < heightOfTerrain - 7) {
            isJumping = false;
            position.y = heightOfTerrain;
        }
        finalPosition = new Vector3f(position);

        if (!isCollided && hasCollided) {
            fallStartTime = -1;
            isJumping = false;
            isCollided = true;
        } else if (!hasCollided) {
            isCollided = false;
        }

        if (crouching) {
            this.playerCapsule.setCapsuleHeight(2f);
            movementSpeed = 5f;
        } else if (sprinting) {
            this.playerCapsule.setCapsuleHeight(2.5f);
            movementSpeed = 20f;
        } else {
            this.playerCapsule.setCapsuleHeight(2.5f);
            movementSpeed = 15f;
        }
    }

    public boolean isCrouching() {
        return crouching;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public void holdItem(Item item, Renderer renderer) {
        this.heldItem = item;
        renderer.setHeldItem(item);
    }

    public float getYOffset() {
        return yOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    public boolean isHoldingItem() {
        return this.heldItem != null;
    }

    public PlayerCapsule getCollisionCapsule() {
        return playerCapsule;
    }

}
