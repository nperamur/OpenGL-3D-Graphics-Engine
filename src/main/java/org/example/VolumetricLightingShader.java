package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class VolumetricLightingShader extends ShaderProgram {
    public static final String VERTEX_FILE = "src/main/resources/lightingPassVertexShader.vsh";
    public static final String FRAGMENT_FILE = "src/main/resources/volumetricLightingFragmentShader.fsh";


    private int locationShadowMap;
    private int locationPosition;
    private int locationTexture;
    private int locationToShadowMapSpace;

    private int locationInversePlayerViewMatrix;

    private int locationToLightSpace;
    private int locationLightProjectionMatrix;


    private int locationLightPosition;
    private int locationLightColor;
    private int locationNormals;
    private int locationViewMatrix;

    private int locationFogDensity;
    private int locationAnisotropy;

    public VolumetricLightingShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");

    }

    @Override
    protected void getAllUniformLocations() {
        locationShadowMap = super.getUniformLocation("shadowMap");
        locationPosition = super.getUniformLocation("gPosition");
        locationNormals = super.getUniformLocation("gNormal");
        locationTexture = super.getUniformLocation("textureSampler");
        locationInversePlayerViewMatrix = super.getUniformLocation("inversePlayerViewMatrix");

        locationToShadowMapSpace = super.getUniformLocation("toShadowMapSpace");
        this.locationLightPosition = super.getUniformLocation("lightPosition");
        this.locationLightColor = super.getUniformLocation("lightColor");
        this.locationViewMatrix = super.getUniformLocation("viewMatrix");


        this.locationToLightSpace = super.getUniformLocation("toLightSpace");
        this.locationLightProjectionMatrix = super.getUniformLocation("lightProjectionMatrix");

        this.locationFogDensity = super.getUniformLocation("density");
        this.locationAnisotropy = super.getUniformLocation("anisotropy");

    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
        super.loadInt(locationPosition, 1);
        super.loadInt(locationShadowMap, 2);
        super.loadInt(locationNormals, 3);

    }


    public void loadInversePlayerViewMatrix(Matrix4f viewMatrix) {
        super.loadMatrix(locationInversePlayerViewMatrix, viewMatrix);
    }

    public void loadViewMatrix(Matrix4f viewMatrix) {
        super.loadMatrix(locationViewMatrix, viewMatrix);
    }




    public void loadLight(Light light) {
        super.loadVector(locationLightPosition, light.getPosition());
    }

    public void loadLightColor(Vector3f color) {
        super.loadVector(locationLightColor, color);
    }


    public void loadToShadowMapSpace(Matrix4f viewMatrix) {
        super.loadMatrix(locationToShadowMapSpace, viewMatrix);
    }

    public void loadToLightSpace(Matrix4f lightSpaceMatrix) {
        super.loadMatrix(locationToLightSpace, lightSpaceMatrix);
    }

    public void loadLightProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(locationLightProjectionMatrix, matrix);
    }

    public void loadFogDensity(float density) {
        super.loadFloat(locationFogDensity, density);
    }

    public void loadAnisotropy(float anisotropy) {
        super.loadFloat(locationAnisotropy, anisotropy);
    }






}
