package org.example.volumetrics;

import org.example.ShaderProgram;

public class VolumetricBlendShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/volumetricBlend.fsh";


    private int locationVolumetricTexture;
    private int locationOriginalTexture;
    private int locationTransmittanceMap;

    public VolumetricBlendShader() {
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
        locationVolumetricTexture = super.getUniformLocation("volumetricTexture");
        locationOriginalTexture = super.getUniformLocation("originalTexture");
        locationTransmittanceMap = super.getUniformLocation("transmittanceMap");
    }

    @Override
    protected void connectTextureUnits() {
        super.loadInt(locationOriginalTexture, 0);
        super.loadInt(locationVolumetricTexture, 1);
        super.loadInt(locationTransmittanceMap, 2);

    }
}
