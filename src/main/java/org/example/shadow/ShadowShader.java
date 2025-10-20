package org.example.shadow;

import org.example.ShaderProgram;
import org.joml.Matrix4f;

public class ShadowShader extends ShaderProgram {

    private static final String VERTEX_PATH = "src/main/resources/shadowVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/shadowFragmentShader.fsh";


    private int locationMvpMatrix;

    public ShadowShader() {
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
        locationMvpMatrix = super.getUniformLocation("mvpMatrix");
    }

    public void loadMvpMatrix(Matrix4f mvpMatrix) {
        super.loadMatrix(locationMvpMatrix, mvpMatrix);
    }


}
