package org.example.blur.bilateralblur;

import org.example.ShaderProgram;

public class BilateralVerticalBlurShader extends ShaderProgram {
    private static final String BLUR_VERTEX = "src/main/resources/verticalBlurVertexShader.vsh";
    private static final String BLUR_FRAGMENT = "src/main/resources/bilateralBlur.fsh";
    private int locationTexture;
    private int locationPosition;
    private int locationNormal;
    private int locationTargetHeight;
    private int locationNumSamples;


    public BilateralVerticalBlurShader() {
        super(BLUR_VERTEX, BLUR_FRAGMENT);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        this.locationTexture = super.getUniformLocation("textureSampler");
        this.locationPosition = super.getUniformLocation("gPosition");
        this.locationNormal = super.getUniformLocation("gNormal");
        this.locationTargetHeight = super.getUniformLocation("targetHeight");
        this.locationNumSamples = super.getUniformLocation("numSamples");
    }


    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
        super.loadInt(locationPosition, 1);
        super.loadInt(locationNormal, 2);
    }

    public void loadTargetHeight(float height) {
        super.loadFloat(locationTargetHeight, height);
    }


    public void loadNumSamples(int numSamples) {
        super.loadInt(locationNumSamples, numSamples);
    }
}
