package org.example;

import org.example.terrain.Terrain;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Player {
    private Vector3f position = new Vector3f(0, 0, 0);
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
    private static final float GRAVITY = 1f;
    private float movementSpeed = 0.1f;
    private boolean sprinting;
    private double fallStartTime;
    private Item heldItem;


    public Player(double pitch, double yaw, double roll, long window) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.window = window;
        GLFW.glfwSetCursorPosCallback(window, (n, xpos, ypos) -> {
            if (locked) {
                this.yaw += (xpos - prevX) * 0.1;
                if (Math.toRadians(this.pitch + (ypos - prevY) * 0.1) >= -Math.PI / 2 && Math.toRadians(this.pitch + (ypos - prevY) * 0.1) <= Math.PI / 2) {
                    this.pitch += (ypos - prevY) * 0.1;
                }
            }

            this.prevX = xpos;
            this.prevY = ypos;
        });
    }

    public void move(Terrain terrain) {
        float heightOfTerrain = terrain.getHeightOfTerrain(this.position.x, this.position.z);
        currentHeightOfTerrain = heightOfTerrain;
        if (isJumping || position.y <= heightOfTerrain) {
            fallStartTime = -1;
        }
        if (position.y < heightOfTerrain) {
            isJumping = false;
            position.y = heightOfTerrain;
        }
        if (isJumping) {
            double t = System.nanoTime() - jumpStartTime;
            t /= 200000000;
            this.position.y = (float) (((double) -1 / 2 * GRAVITY * Math.pow(t, 2) + t * 1.7f) + jumpHeight);
        } else if (position.y > heightOfTerrain) {
            if (fallStartTime == -1) {
                fallStartTime = 0;
                fallHeight = position.y;
            }
            this.position.y = Math.max(heightOfTerrain, (float) (-GRAVITY * Math.pow(fallStartTime, 2) * 13) + fallHeight);
            fallStartTime += Main.getDisplayManager().getFrameTimeInSeconds();
        }
        handleInputs();
    }

    public Vector3f getPosition() {
        return position;
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
        jumpHeight = currentHeightOfTerrain;
        this.isJumping = true;
    }

    private void handleInputs() {
        double frameTime = Main.getDisplayManager().getFrameTimeInSeconds();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            position.z -= (float) (movementSpeed * Math.cos(Math.toRadians(yaw)) * frameTime);
            position.x += (float) (movementSpeed * Math.sin(Math.toRadians(yaw)) * frameTime);
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            position.z -= (float) (movementSpeed * Math.cos(Math.toRadians(yaw + 90)) * frameTime);
            position.x += (float) (movementSpeed * Math.sin(Math.toRadians(yaw + 90)) * frameTime);
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            position.z += (float) (movementSpeed * Math.cos(Math.toRadians(yaw + 90)) * frameTime);
            position.x -= (float) (movementSpeed * Math.sin(Math.toRadians(yaw + 90)) * frameTime);
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            position.z += (float) (movementSpeed * Math.cos(Math.toRadians(yaw)) * frameTime);
            position.x -= (float) (movementSpeed * Math.sin(Math.toRadians(yaw)) * frameTime);
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            yaw -= 200f * frameTime;
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            yaw += 200f * frameTime;
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS && Math.toRadians(pitch) >= -Math.PI / 2) {
            pitch -= 200f * frameTime;
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS && Math.toRadians(pitch) <= Math.PI / 2) {
            pitch += 200f * frameTime;
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            locked = true;
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            locked = false;
        } if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS && !isJumping) {
            jump();
        }

        crouching = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        sprinting = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
        if (crouching) {
            movementSpeed = 5f;
        } else if (sprinting) {
            movementSpeed = 20f;
        } else {
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


    public boolean isHoldingItem() {
        return this.heldItem != null;
    }

}
