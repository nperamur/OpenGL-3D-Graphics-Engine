package org.example;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class TestShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/vertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/fragmentShader.fsh";
    private int locationTransformationMatrix;
    private int locationViewMatrix;
    private int locationProjectionMatrix;
    private int locationPlane;
    private int locationLightPosition;
    private int locationLightColor;
    private int locationIsItem;

    private int locationTexture;

    public TestShader() {
        super(VERTEX_PATH, FRAGMENT_PATH);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        this.locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
        this.locationViewMatrix = super.getUniformLocation("viewMatrix");
        this.locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
        this.locationPlane = super.getUniformLocation("plane");
        this.locationLightPosition = super.getUniformLocation("lightPosition");
        this.locationLightColor = super.getUniformLocation("lightColor");
        this.locationIsItem = super.getUniformLocation("isItem");
        this.locationTexture = super.getUniformLocation("textureSampler");
    }



    public void loadViewMatrix(Player player) {
        super.loadMatrix(locationViewMatrix, GameMath.createViewMatrix(player));

    }

    public void loadViewMatrix(Matrix4f matrix) {
        super.loadMatrix(locationViewMatrix, matrix);

    }

    public void unloadViewMatrix() {
        super.loadMatrix(locationViewMatrix, new Matrix4f());
    }


    public void loadClipPlane(Vector4f vector) {
        super.loadVector(locationPlane, vector);
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(locationTransformationMatrix, matrix);
    }

    public void loadProjectionMatrix(Matrix4f projection) {
        super.loadMatrix(locationProjectionMatrix, projection);
    }


    public void loadLight(Light light) {
        super.loadVector(locationLightPosition, light.getPosition());
        super.loadVector(locationLightColor, light.getColor());
    }

    public void loadIsItem(boolean bool) {
        super.loadBoolean(locationIsItem, bool);
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 1);
    }
}
