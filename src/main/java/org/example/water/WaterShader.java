package org.example.water;

import org.example.GameMath;
import org.example.Light;
import org.example.Player;
import org.example.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class WaterShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/waterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/waterFragmentShader.fsh";
    private int locationTransformationMatrix;
    private int locationViewMatrix;
    private int locationProjectionMatrix;
    private int locationLightPosition;
    private int locationLightColor;
    private int locationReflectionTexture;
    private int locationRefractionTexture;
    private int locationDudvMap;
    private int locationMoveFactor;
    private int locationCameraPosition;
    private int locationNormalMap;
    private int locationDepthMap;


    public WaterShader() {
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
        this.locationLightPosition = super.getUniformLocation("lightPosition");
        this.locationLightColor = super.getUniformLocation("lightColor");
        this.locationReflectionTexture = super.getUniformLocation("reflectionTexture");
        this.locationRefractionTexture = super.getUniformLocation("refractionTexture");
        this.locationDudvMap = super.getUniformLocation("dudvMap");
        this.locationMoveFactor = super.getUniformLocation("moveFactor");
        this.locationCameraPosition = super.getUniformLocation("cameraPosition");
        this.locationNormalMap = super.getUniformLocation("normalMap");
        this.locationDepthMap = super.getUniformLocation("depthMap");
    }



    public void loadViewMatrix(Player player) {
        super.loadMatrix(locationViewMatrix, GameMath.createViewMatrix(player));
        super.loadVector(locationCameraPosition, new Vector3f(player.getPosition().x, player.getPosition().y + 1, player.getPosition().z));
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

    public void connectTextureUnits() {
        super.loadInt(locationReflectionTexture, 0);
        super.loadInt(locationRefractionTexture, 1);
        super.loadInt(locationDudvMap, 2);
        super.loadInt(locationNormalMap, 3);
        super.loadInt(locationDepthMap, 4);
    }

    public void loadMoveFactor(float moveFactor) {
        super.loadFloat(locationMoveFactor, moveFactor);
    }


}
