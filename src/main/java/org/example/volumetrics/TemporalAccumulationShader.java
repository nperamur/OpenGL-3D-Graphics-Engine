package org.example.volumetrics;

import org.example.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class TemporalAccumulationShader extends ShaderProgram {
    private static final String VERTEX_PATH = "src/main/resources/brightFilterVertexShader.vsh";
    private static final String FRAGMENT_PATH = "src/main/resources/temporalAccumulation.fsh";


    private int locationVolumetricTexture;
    private int locationTransmittanceMap;
    private int locationHistoryColor;
    private int locationHistoryTransmittance;

    private int locationPrevViewMatrix;
    private int locationCurrentViewMatrix;

    private int locationPosition;

    private int locationProjectionMatrix;

    private int locationMoveFactor;
    private int locationPrevMoveFactor;

    private int locationCurrPosition;

    private int locationInverseViewMatrix;
    private int locationInverseProjectionMatrix;
    public TemporalAccumulationShader() {
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
        locationTransmittanceMap = super.getUniformLocation("transmittanceMap");
        locationHistoryColor = super.getUniformLocation("historyColor");
        locationHistoryTransmittance = super.getUniformLocation("historyTransmittance");
        locationPrevViewMatrix = super.getUniformLocation("prevViewMatrix");
        locationCurrentViewMatrix = super.getUniformLocation("currViewMatrix");
        locationPosition = super.getUniformLocation("gPosition");
        locationProjectionMatrix = super.getUniformLocation("projectionMatrix");
        locationPrevMoveFactor = super.getUniformLocation("prevMoveFactor");
        locationMoveFactor = super.getUniformLocation("moveFactor");
        locationCurrPosition = super.getUniformLocation("currPosition");
        locationInverseViewMatrix = super.getUniformLocation("inverseViewMatrix");
        locationInverseProjectionMatrix = super.getUniformLocation("inverseProjectionMatrix");
    }

    @Override
    protected void connectTextureUnits() {
        super.loadInt(locationVolumetricTexture, 0);
        super.loadInt(locationTransmittanceMap, 1);
        super.loadInt(locationHistoryColor, 2);
        super.loadInt(locationHistoryTransmittance, 3);
        super.loadInt(locationPosition, 4);
        super.loadInt(locationCurrPosition, 5);

    }

    public void loadViewMatrices(Matrix4f prevViewMatrix, Matrix4f viewMatrix, Matrix4f projectionMatrix, Matrix4f inverseViewMatrix, Matrix4f inverseProjectionMatrix) {
        super.loadMatrix(locationPrevViewMatrix, prevViewMatrix);
        super.loadMatrix(locationCurrentViewMatrix, viewMatrix);
        super.loadMatrix(locationInverseViewMatrix, inverseViewMatrix);
        super.loadMatrix(locationInverseProjectionMatrix, inverseProjectionMatrix);
        super.loadMatrix(locationProjectionMatrix, projectionMatrix);
    }

    public void loadCloudMoveFactors(float moveFactor, float prevMoveFactor) {
        super.loadFloat(locationMoveFactor, moveFactor);
        super.loadFloat(locationPrevMoveFactor, prevMoveFactor);
    }


}
