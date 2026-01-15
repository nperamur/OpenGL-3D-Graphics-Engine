package org.example.bloom;

import org.example.ShaderProgram;

public class CombineShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/combineFragment.fsh";
    private int locationTexture;
    private int locationTexture2;
    private int locationAdd;

    private boolean add;



    public CombineShader(boolean add) {
        super(VERTEX_PATH, FRAGMENT_PATH);
        this.add = add;
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        this.locationTexture = super.getUniformLocation("textureOne");
        this.locationTexture2 = super.getUniformLocation("textureTwo");
        this.locationAdd = super.getUniformLocation("add");
    }

    public void connectTextureUnits() {
        super.loadInt(locationTexture, 0);
        super.loadInt(locationTexture2, 1);
    }

    private void loadAdd(boolean add) {
        super.loadBoolean(locationAdd, add);
    }

    @Override
    public void init() {
        this.start();
        this.connectTextureUnits();
        this.loadAdd(add);
        this.stop();
    }
}
