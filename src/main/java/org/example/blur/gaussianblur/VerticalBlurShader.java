package org.example.blur.gaussianblur;

import org.example.ShaderProgram;

public class VerticalBlurShader extends ShaderProgram {
    private static final String VERTICAL_BLUR_VERTEX = "src/main/resources/verticalBlurVertexShader.vsh";
    private static final String VERTICAL_BLUR_FRAGMENT = "src/main/resources/blurFragment.fsh";
    private int locationTargetHeight;
    private int locationTexture;
    private int locationNumSamples;

    private int targetFboHeight;
    private int numSamples;


    public VerticalBlurShader(int targetFboHeight, int numSamples) {
        super(VERTICAL_BLUR_VERTEX, VERTICAL_BLUR_FRAGMENT);
        this.targetFboHeight = targetFboHeight;
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
        this.locationTargetHeight = super.getUniformLocation("targetHeight");
        this.locationTexture = super.getUniformLocation("originalTexture");
        this.locationNumSamples = super.getUniformLocation("numSamples");
    }

    public void loadTargetHeight(float height) {
        super.loadFloat(locationTargetHeight, height);
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
    }

    private void loadNumSamples(int numSamples) {
        super.loadInt(locationNumSamples, numSamples);
    }

    @Override
    public void init() {
        this.start();
        this.connectTextureUnits();
        this.loadTargetHeight(targetFboHeight);
        this.loadNumSamples(numSamples);
        this.stop();
    }
}
