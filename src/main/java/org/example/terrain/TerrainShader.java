package org.example.terrain;

import org.example.GameMath;
import org.example.Light;
import org.example.Player;
import org.example.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class TerrainShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/terrainVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/terrainFragmentShader.fsh";
    private int locationTransformationMatrix;
    private int locationViewMatrix;
    private int locationProjectionMatrix;
    private int locationLightPosition;
    private int locationLightColor;
    private int locationBackgroundTexture;
    private int locationRTexture;
    private int locationGTexture;
    private int locationBTexture;
    private int locationBlendMap;
    private int locationPlane;
    private int locationShineDamper;
    private int locationReflectivity;

    public TerrainShader() {
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
        this.locationBackgroundTexture = super.getUniformLocation("backgroundTexture");
        this.locationRTexture = super.getUniformLocation("rTexture");
        this.locationGTexture = super.getUniformLocation("gTexture");
        this.locationBTexture = super.getUniformLocation("bTexture");
        this.locationBlendMap = super.getUniformLocation("blendMap");
        this.locationPlane = super.getUniformLocation("plane");
        this.locationShineDamper = super.getUniformLocation("shineDamper");
        this.locationReflectivity = super.getUniformLocation("reflectivity");
    }



    public void loadViewMatrix(Player player) {
        super.loadMatrix(locationViewMatrix, GameMath.createViewMatrix(player));

    }

    public void loadViewMatrix(Matrix4f viewMatrix) {
        super.loadMatrix(locationViewMatrix, viewMatrix);

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
        super.loadInt(locationBackgroundTexture, 1);
        super.loadInt(locationRTexture, 2);
        super.loadInt(locationGTexture, 3);
        super.loadInt(locationBTexture, 4);
        super.loadInt(locationBlendMap, 5);
    }

    public void loadClipPlane(Vector4f vector) {
        super.loadVector(locationPlane, vector);
    }

    public void loadShineVariables(float damper, float reflectivity) {
        super.loadFloat(locationReflectivity, reflectivity);
        super.loadFloat(locationShineDamper, damper);
    }
}
