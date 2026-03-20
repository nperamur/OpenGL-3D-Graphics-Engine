package org.example.shadow;

import org.example.Light;
import org.example.ShaderProgram;
import org.joml.Matrix4f;

public class RaytracedShadowsShader extends ShaderProgram {
    private static final String COMPUTE_FILE = "src/main/resources/raytracedShadowsShader.csh";

    private int locationLightPosition;
    private int locationTrianglesLength;
    private int locationTriangleBVHLength;
    private int locationModelBVHLength;
    private int locationPosition;

    private int locationInverseViewMatrix;
    private int locationNormal;
    private int locationFrameCount;

    public RaytracedShadowsShader() {
        super(COMPUTE_FILE);
    }

    @Override
    protected void bindAttributes() {}


    @Override
    protected void getAllUniformLocations() {
        locationLightPosition = super.getUniformLocation("lightPosition");
        locationTrianglesLength = super.getUniformLocation("trianglesLength");
        locationTriangleBVHLength = super.getUniformLocation("triangleBVHLength");
        locationPosition = super.getUniformLocation("gPosition");
        locationModelBVHLength = super.getUniformLocation("modelBVHLength");
        locationInverseViewMatrix = super.getUniformLocation("inverseViewMatrix");
        locationNormal = super.getUniformLocation("gNormal");
        locationFrameCount = super.getUniformLocation("frameCount");
    }

    public void loadLight(Light light) {
        super.loadVector(locationLightPosition, light.getPosition());
    }

    public void loadArrayLengths(int trianglesLength, int triangleBVHLength, int modelBVHLength) {
        super.loadInt(locationTriangleBVHLength, triangleBVHLength);
        super.loadInt(locationTrianglesLength, trianglesLength);
        super.loadInt(locationModelBVHLength, modelBVHLength);
    }

    public void loadInverseViewMatrix(Matrix4f inverseViewMatrix) {
        super.loadMatrix(locationInverseViewMatrix, inverseViewMatrix);
    }

    @Override
    protected void connectTextureUnits() {
        super.loadInt(locationPosition, 0);
        super.loadInt(locationNormal, 1);
    }

    public void loadFrameCount(int frameCount) {
        super.loadInt(locationFrameCount, frameCount);
    }
}
