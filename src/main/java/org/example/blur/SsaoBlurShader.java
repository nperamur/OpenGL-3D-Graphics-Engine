package org.example.blur;

import org.example.ShaderProgram;

public class SsaoBlurShader extends ShaderProgram {
    private static final String SSAO_BLUR_VERTEX = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String SSAO_BLUR_FRAGMENT = "src/main/resources/ssaoBlur.fsh";
    private int locationTexture;



    public SsaoBlurShader() {
        super(SSAO_BLUR_VERTEX, SSAO_BLUR_FRAGMENT);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        this.locationTexture = super.getUniformLocation("ssaoInput");
    }


    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);

    }
}
