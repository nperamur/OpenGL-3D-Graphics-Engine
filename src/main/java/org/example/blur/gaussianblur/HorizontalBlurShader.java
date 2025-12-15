package org.example.blur.gaussianblur;

import org.example.ShaderProgram;

public class HorizontalBlurShader extends ShaderProgram {
    private static final String HORIZONTAL_BLUR_VERTEX = "src/main/resources/horizontalBlurVertexShader.vsh";
    private static final String HORIZONTAL_BLUR_FRAGMENT = "src/main/resources/blurFragment.fsh";
    private int locationTargetWidth;
    private int locationTexture;
    private int locationNumSamples;


    public HorizontalBlurShader() {
        super(HORIZONTAL_BLUR_VERTEX, HORIZONTAL_BLUR_FRAGMENT);
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

    public void loadNumSamples(int numSamples) {
        super.loadInt(locationNumSamples, numSamples);
    }
}
