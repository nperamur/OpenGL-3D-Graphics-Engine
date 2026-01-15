package org.example.blur.bilateralblur;

import org.example.ShaderProgram;

public class BilateralHorizontalBlurShader extends ShaderProgram {
    private static final String BLUR_VERTEX = "src/main/resources/horizontalBlurVertexShader.vsh";
    private static final String BLUR_FRAGMENT = "src/main/resources/bilateralBlur.fsh";
    private int locationTexture;
    private int locationPosition;
    private int locationNormal;
    private int locationTargetWidth;
    private int locationNumSamples;

    private int targetWidth;
    private int numSamples;


    public BilateralHorizontalBlurShader(int targetWidth, int numSamples) {
        super(BLUR_VERTEX, BLUR_FRAGMENT);
        this.targetWidth = targetWidth;
        this.numSamples = numSamples;
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
        this.locationTargetWidth = super.getUniformLocation("targetWidth");
        this.locationNumSamples = super.getUniformLocation("numSamples");
    }


    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
        super.loadInt(locationPosition, 1);
        super.loadInt(locationNormal, 2);
    }

    public void loadTargetWidth(float width) {
        super.loadFloat(locationTargetWidth, width);
    }

    private void loadNumSamples(int numSamples) {
        super.loadInt(locationNumSamples, numSamples);
    }


    @Override
    public void init() {
        this.start();
        this.connectTextureUnits();
        this.loadTargetWidth(targetWidth);
        this.loadNumSamples(numSamples);
        this.stop();
    }
}
