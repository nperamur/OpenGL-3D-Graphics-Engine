package org.example.tonemapping;

import org.example.ShaderProgram;

public class ToneMappingShader extends ShaderProgram {
    public static final String VERTEX_FILE = "src/main/resources/lightingPassVertexShader.vsh";
    public static final String FRAGMENT_FILE = "src/main/resources/toneMappingShader.fsh";

    private int locationTexture;
    private int locationExposure;


    public ToneMappingShader() {
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
        locationTexture = super.getUniformLocation("textureSampler");
        locationExposure = super.getUniformLocation("exposure");
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
    }

    public void loadExposure(float exposure) {
        super.loadFloat(locationExposure, exposure);
    }
}