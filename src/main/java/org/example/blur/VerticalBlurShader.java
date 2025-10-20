package org.example.blur;

import org.example.ShaderProgram;

public class VerticalBlurShader extends ShaderProgram {
    private static final String VERTICAL_BLUR_VERTEX = "src/main/resources/verticalBlurVertexShader.vsh";
    private static final String VERTICAL_BLUR_FRAGMENT = "src/main/resources/blurFragment.fsh";
    private int locationTargetHeight;
    private int locationTexture;


    public VerticalBlurShader() {
        super(VERTICAL_BLUR_VERTEX, VERTICAL_BLUR_FRAGMENT);
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
    }

    public void loadTargetHeight(float height) {
        super.loadFloat(locationTargetHeight, height);
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 11);

    }
}
