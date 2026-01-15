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

    private int targetHeight;
    private int numSamples;


    public BilateralVerticalBlurShader(int targetHeight, int numSamples) {
        super(BLUR_VERTEX, BLUR_FRAGMENT);
        this.numSamples = numSamples;
        this.targetHeight = targetHeight;
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


    private void loadNumSamples(int numSamples) {
        super.loadInt(locationNumSamples, numSamples);
    }

    @Override
    public void init() {
        this.start();
        this.connectTextureUnits();
        this.loadNumSamples(5);
        this.loadTargetHeight(targetHeight);
        this.stop();
    }
}
