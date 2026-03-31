package org.example.volumetrics;

import org.example.ShaderProgram;

public class BilateralUpsampleShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/bilateralUpsample.fsh";

    private int locationLowResFog;
    private int locationLowResTransmittance;
    private int locationFullResMask;
    private int locationLowResScaleFactor;

    public BilateralUpsampleShader() {
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
        locationLowResFog = super.getUniformLocation("lowResFog");
        locationFullResMask = super.getUniformLocation("fullResMask");
        locationLowResScaleFactor = super.getUniformLocation("lowResScaleFactor");
        locationLowResTransmittance = super.getUniformLocation("lowResTransmittance");
    }

    @Override
    protected void connectTextureUnits() {
        super.loadInt(locationLowResFog, 0);
        super.loadInt(locationLowResTransmittance, 1);
        super.loadInt(locationFullResMask, 2);
    }

    public void loadScaleFactor(float scaleFactor) {
        super.loadFloat(locationLowResScaleFactor, scaleFactor);
    }


}