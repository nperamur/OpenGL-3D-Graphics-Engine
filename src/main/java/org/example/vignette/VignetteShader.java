package org.example.vignette;

import org.example.ShaderProgram;

public class VignetteShader extends ShaderProgram {
    private int locationTexture;
    private static final String VERTEX_PATH = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/vignetteFragmentShader.fsh";

    public VignetteShader() {
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
        locationTexture = getUniformLocation("textureSampler");
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);

    }
}
