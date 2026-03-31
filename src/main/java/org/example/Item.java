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

        Matrix4f view    = GameMath.createViewMatrix(Main.getDisplayManager().getPlayer());
        Matrix4f invView = new Matrix4f(view).invert();

        Matrix4f handOffset = new Matrix4f()
                .translation(2f, -0.5f, -3.5f);
               Matrix4f handModel = new Matrix4f(invView).mul(handOffset);

        Vector3f worldPos = handModel.getTranslation(new Vector3f());

        Quaternionf worldRotQuat = handModel.getNormalizedRotation(new Quaternionf());

        Vector3f handEuler = new Vector3f();
        worldRotQuat.getEulerAnglesXYZ(handEuler);
        handEuler.mul((float)(180.0/Math.PI));

        super.setPosition(worldPos);
        super.setRotation(handEuler);




    }



}

