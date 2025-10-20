package org.example;

import org.joml.Matrix4f;

public class LightingPassShader extends ShaderProgram {
    public static final String VERTEX_FILE = "src/main/resources/lightingPassVertexShader.vsh";
    public static final String FRAGMENT_FILE = "src/main/resources/lightingPassFragmentShader.fsh";


    private int locationShadowMap;
    private int locationPosition;
    private int locationTexture;

    private int locationInversePlayerViewMatrix;

    private int locationToShadowMapSpace;


    private int locationLightPosition;
    private int locationLightColor;
    private int locationNormals;
    private int locationViewMatrix;

    public LightingPassShader() {
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


    public void loadToShadowMapSpace(Matrix4f viewMatrix) {
        super.loadMatrix(locationToShadowMapSpace, viewMatrix);
    }

    public void loadLight(Light light) {
        super.loadVector(locationLightPosition, light.getPosition());
        super.loadVector(locationLightColor, light.getColor());
    }


}
