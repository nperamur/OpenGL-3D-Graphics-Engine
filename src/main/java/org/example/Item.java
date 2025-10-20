package org.example;

import org.joml.*;

import java.lang.Math;

public class Item extends Entity {
    private float rotX;
    private float rotY;
    private float rotZ;

    public Item(TexturedModel model, float rotZ, float rotY, float rotX, float scale) {
        super(model, new Vector3f(2, -0.5f, -3.5f), rotZ, rotY, rotX, scale);
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }

    public void updatePosition() {
//        //view

        // 1) Compute view and its inverse:
        Matrix4f view    = GameMath.createViewMatrix(Main.getDisplayManager().getPlayer());
        Matrix4f invView = new Matrix4f(view).invert();

        // 2) Build your hand's offset from the camera in view-space:
        Matrix4f handOffset = new Matrix4f()
                .translation(2f, -0.5f, -3.5f);
        // .rotateX(...).rotateY(...).rotateZ(...) if you want local tilt
        // .scale(...)                  if you want to scale it

        // 3) Compose the “model” matrix that fully cancels camera and then applies your offset:
        Matrix4f handModel = new Matrix4f(invView).mul(handOffset);

        // 4) Extract world-space position:
        Vector3f worldPos = handModel.getTranslation(new Vector3f());

        // 5) Extract world-space rotation as a quaternion:
        Quaternionf worldRotQuat = handModel.getNormalizedRotation(new Quaternionf());

        // 6) Convert that quaternion to Euler angles in the right order:
        Vector3f handEuler = new Vector3f();
        worldRotQuat.getEulerAnglesXYZ(handEuler);         // try XYZ, YXZ or ZYX if this wiggles
        handEuler.mul((float)(180.0/Math.PI));             // to degrees if your engine needs it

        // 7) Finally, apply both to your hand:
        super.setPosition(worldPos);
        super.setRotation(handEuler);




    }



}

