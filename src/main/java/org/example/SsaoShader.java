package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SsaoShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/ssaoVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/ssaoFragmentShader.fsh";
    private int locationProjectionMatrix;

    private int locationNormal;
    private int locationPosition;
    private int locationNoise;

    private int locationSamples;
    private int locationScreenWidth;
    private int locationScreenHeight;

    private Vector3f[] randomSamplingKernels;

    public SsaoShader(Vector3f[] randomSamplingKernels) {
        super(VERTEX_PATH, FRAGMENT_PATH);
        this.randomSamplingKernels = randomSamplingKernels;
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        locationProjectionMatrix = super.getUniformLocation("projectionMatrix");

        locationNormal = super.getUniformLocation("gNormal");
        locationPosition = super.getUniformLocation("gPosition");
        locationSamples = super.getUniformLocation("samples");
        locationNoise = super.getUniformLocation("noise");

        locationScreenWidth = super.getUniformLocation("screenWidth");
        locationScreenHeight = super.getUniformLocation("screenHeight");
    }

    public void connectTextureUnits() {
        super.loadInt(locationNoise, 0);
        super.loadInt(locationNormal, 1);
        super.loadInt(locationPosition, 2);

    }

    private void loadSamplingKernels() {
        super.loadVectorArray(locationSamples, this.randomSamplingKernels);
    }

    public void loadProjectionMatrix(Matrix4f projection) {
        super.loadMatrix(locationProjectionMatrix, projection);
    }

    public void loadScreenDimensions(int width, int height) {
        super.loadInt(locationScreenWidth, width);
        super.loadInt(locationScreenHeight, height);
    }

    @Override
    public void init() {
        this.start();
        this.connectTextureUnits();
        this.loadSamplingKernels();
        this.stop();
    }
}
