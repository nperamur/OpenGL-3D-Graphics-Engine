package org.example.bloom;

import org.example.ShaderProgram;

public class BrightFilterShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/brightFilterFragmentShader.fsh";

    private int locationThreshold;
    private int locationTexture;

    public BrightFilterShader() {
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
        this.locationTexture = super.getUniformLocation("originalTexture");
        this.locationThreshold = super.getUniformLocation("threshold");

    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
    }

    public void loadThreshold(float threshold) {
        super.loadFloat(locationThreshold, threshold);
    }
}
