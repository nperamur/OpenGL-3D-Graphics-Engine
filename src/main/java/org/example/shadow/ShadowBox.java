package org.example.shadow;


import org.example.Main;
import org.example.Player;
import org.example.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;


public class ShadowBox {


    private static final float OFFSET = 150;
    private static final Vector4f UP = new Vector4f(0, 1, 0, 0);
    private static final Vector4f FORWARD = new Vector4f(0, 0, -1, 0);
    private float shadowDistance = 150;


    private float minX, maxX;
    private float minY, maxY;
    private float minZ, maxZ;
    private Matrix4f lightViewMatrix;
    private Player player;


    private float farHeight;
    private float farWidth;
    private float nearHeight;
    private float nearWidth;



    public ShadowBox(Matrix4f lightViewMatrix, Player player) {
        this.lightViewMatrix = lightViewMatrix;
        this.player = player;
        calculateWidthsAndHeights();
    }

    public ShadowBox(Matrix4f lightViewMatrix, Player player, int distance) {
        this.shadowDistance = distance;
        this.lightViewMatrix = lightViewMatrix;
        this.player = player;
        calculateWidthsAndHeights();
    }


    public void updateShadowBox() {
        Matrix4f rotation = calculateCameraRotationMatrix();
        Vector4f forwardCopy = new Vector4f(FORWARD);
        Vector4f transformed = new Matrix4f(rotation).transform(forwardCopy);
        Vector3f forwardVector = new Vector3f(new Vector3f(transformed.x, transformed.y, transformed.z));


        Vector3f toFar = new Vector3f(forwardVector);
        toFar.mul(shadowDistance);
        Vector3f toNear = new Vector3f(forwardVector);
        toNear.mul(Renderer.NEAR_PLANE);
        Vector3f centerNear = new Vector3f(toNear).add(player.getPosition());
        Vector3f centerFar = new Vector3f(toFar).add(player.getPosition());


        Vector4f[] points = calculateFrustumVertices(rotation, forwardVector, centerNear,
                centerFar);

        computeBounds(points);


    }


    private void computeBounds(Vector4f[] points) {
        boolean first = true;
        for (Vector4f pt : points) {
            if (first) {
                minX = pt.x;
                maxX = pt.x;
                minY = pt.y;
                maxY = pt.y;
                minZ = pt.z;
                maxZ = pt.z;
                first = false;
                continue;
            }

            maxX = Math.max(pt.x, maxX);
            minX = Math.min(pt.x, minX);

            maxY = Math.max(pt.y, maxY);
            minY = Math.min(pt.y, minY);

            maxZ = Math.max(pt.z, maxZ);
            minZ = Math.min(pt.z, minZ);
        }
        this.maxZ += OFFSET;
    }



    public Vector3f getCenter() {
        float x = (minX + maxX) / 2f;
        float y = (minY + maxY) / 2f;
        float z = (minZ + maxZ) / 2f;

        Vector4f cen = new Vector4f(x, y, z, 1f);


        Matrix4f invertedLight = new Matrix4f();
        new Matrix4f(this.lightViewMatrix).invert(invertedLight);

        Vector4f transformed = new Vector4f();
        invertedLight.transform(cen, transformed);

        if (transformed.w != 0 && transformed.w != 1) {
            transformed.div(transformed.w);
        }

        return new Vector3f(transformed.x, transformed.y, transformed.z);
    }



    public float getWidth() {
        return maxX - minX;
    }



    public float getHeight() {
        return maxY - minY;
    }


    public float getLength() {
        return maxZ - minZ;
    }



    private Vector4f[] calculateFrustumVertices(Matrix4f rotation, Vector3f forwardVector,
                                                Vector3f centerNear, Vector3f centerFar) {
        Vector4f upCopy = new Vector4f(UP);
        Vector4f transformed = new Matrix4f(rotation).transform(upCopy);
        Vector3f upVector = new Vector3f(new Vector3f(transformed.x, transformed.y, transformed.z));
        Vector3f rightVector = new Vector3f(forwardVector).cross(upVector);
        Vector3f downVector = new Vector3f(-upVector.x, -upVector.y, -upVector.z);
        Vector3f leftVector = new Vector3f(-rightVector.x, -rightVector.y, -rightVector.z);
        Vector3f farTop = new Vector3f(centerFar).add(new Vector3f(upVector.x * farHeight,
                upVector.y * farHeight, upVector.z * farHeight));
        Vector3f farBottom = new Vector3f(centerFar).add( new Vector3f(downVector.x * farHeight,
                downVector.y * farHeight, downVector.z * farHeight));
        Vector3f nearTop = new Vector3f(centerNear).add( new Vector3f(upVector.x * nearHeight,
                upVector.y * nearHeight, upVector.z * nearHeight));
        Vector3f nearBottom = new Vector3f(centerNear).add( new Vector3f(downVector.x * nearHeight,
                downVector.y * nearHeight, downVector.z * nearHeight));
        Vector4f[] points = new Vector4f[8];
        points[0] = calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth);
        points[1] = calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth);
        points[2] = calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth);
        points[3] = calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth);
        points[4] = calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth);
        points[5] = calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth);
        points[6] = calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth);
        points[7] = calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth);
        return points;
    }



    private Vector4f calculateLightSpaceFrustumCorner(Vector3f startPoint, Vector3f direction,
                                                      float width) {
        Vector3f pt = new Vector3f(startPoint).add(
                new Vector3f(direction.x * width, direction.y * width, direction.z * width));
        Vector4f point4f = new Vector4f(pt.x, pt.y, pt.z, 1f);
        this.lightViewMatrix.transform(point4f, point4f);
        return point4f;
    }



    private Matrix4f calculateCameraRotationMatrix() {
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.rotate((float) Math.toRadians(-this.player.getYaw()), new Vector3f(0, 1, 0));
        rotationMatrix.rotate((float) Math.toRadians(-this.player.getPitch()), new Vector3f(1, 0, 0));
        return rotationMatrix;
    }


    private void calculateWidthsAndHeights() {
        int fov = Main.getDisplayManager().getRenderer().getFov();
        this.farWidth = (float) (shadowDistance * Math.tan(Math.toRadians(fov)));
        this.nearWidth = (float) (Renderer.NEAR_PLANE * Math.tan(Math.toRadians(fov)));
        this.farHeight = this.farWidth / getAspectRatio();
        this.nearHeight = this.nearWidth / getAspectRatio();
    }


    private float getAspectRatio() {
        return (float) Main.getDisplayManager().getWidth() / (float) Main.getDisplayManager().getHeight();
    }


}
