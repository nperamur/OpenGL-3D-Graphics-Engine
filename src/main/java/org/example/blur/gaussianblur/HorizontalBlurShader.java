package org.example.blur.gaussianblur;

import org.example.ShaderProgram;

public class HorizontalBlurShader extends ShaderProgram {
    private static final String HORIZONTAL_BLUR_VERTEX = "src/main/resources/horizontalBlurVertexShader.vsh";
    private static final String HORIZONTAL_BLUR_FRAGMENT = "src/main/resources/blurFragment.fsh";
    private int locationTargetWidth;
    private int locationTexture;
    private int locationNumSamples;

    private int targetFboWidth;
    private int numSamples;

    public HorizontalBlurShader(int targetFboWidth, int numSamples) {
        super(HORIZONTAL_BLUR_VERTEX, HORIZONTAL_BLUR_FRAGMENT);
        this.targetFboWidth = targetFboWidth;
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
        this.locationTargetWidth = super.getUniformLocation("targetWidth");
        this.locationTexture = super.getUniformLocation("originalTexture");
        this.locationNumSamples = super.getUniformLocation("numSamples");
    }

    public void loadTargetWidth(float width) {
        super.loadFloat(locationTargetWidth, width);
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);

    }

    @Override
    public void init() {
        this.start();
        this.connectTextureUnits();
        this.loadTargetWidth(targetFboWidth);
        this.loadNumSamples(numSamples);
        this.stop();
    }

    private void loadNumSamples(int numSamples) {
        super.loadInt(locationNumSamples, numSamples);
    }
}
