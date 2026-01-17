package org.example.volumetrics;

import org.example.*;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.example.noise.CloudNoiseGenerator;
import org.example.shadow.ShadowRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30C.GL_R32F;

public class VolumetricLighting extends PostProcessEffect {
    private VolumetricLightingShader shader;
    private Sunlight light;
    private Gbuffer gbuffer;
    private ShadowRenderer shadowRenderer;


    private Vector3f lightColor;
    private float fogAnisotropy;
    private float fogDensity;
    private Matrix4f inverseProjectionMatrix;
    private float albedo;
    private float stepSize;
    private int cloudNoiseTexture;

    private float moveFactor;

    private int frameCount;

    private Vector3f skyColor;

    public VolumetricLighting(Sunlight light, Gbuffer gbuffer, ShadowRenderer shadowRenderer, CloudNoiseGenerator cloudNoiseGenerator) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        shader = new VolumetricLightingShader();
        shader.init();
        this.light = light;
        this.gbuffer = gbuffer;
        this.shadowRenderer = shadowRenderer;
        this.cloudNoiseTexture = generateCloudNoise(cloudNoiseGenerator);
    }

    @Override
    public void render(Model screenQuad) {
        frameCount++;
        if (super.getFbo().getWidth() != Main.getDisplayManager().getWidth() || super.getFbo().getHeight() != Main.getDisplayManager().getHeight()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        GL11C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.start();
        shader.loadLight(light);
        shader.loadRandomNumber((int) (Math.random() * 10000));
        shader.loadMoveFactor(this.moveFactor);
        shader.loadLightColor(this.lightColor);
        shader.loadFogDensity(this.fogDensity);
        shader.loadSkyColor(Main.getDisplayManager().getSkyColor());
        shader.loadInverseProjectionMatrix(inverseProjectionMatrix);
        shader.loadAnisotropy(this.fogAnisotropy);
        shader.loadAlbedo(this.albedo);
        shader.loadStepSize(this.stepSize);
        shader.loadViewMatrix(GameMath.createViewMatrix(Main.getDisplayManager().getPlayer()));
        shader.loadToShadowMapSpace(shadowRenderer.getToShadowMapSpaceMatrix());
        shader.loadInversePlayerViewMatrix(GameMath.createViewMatrix(Main.getDisplayManager().getPlayer()).invert());
        shader.loadToLightSpace(shadowRenderer.getLightViewMatrix());
        shader.loadLightProjectionMatrix(shadowRenderer.getLightProjectionMatrix());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, super.getFbo().getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL_TEXTURE_2D, gbuffer.getPositionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL_TEXTURE_2D, shadowRenderer.getShadowMap());

        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL_TEXTURE_3D, cloudNoiseTexture);

        Renderer.renderModel(screenQuad);
        shader.stop();
    }

    public void setInverseProjectionMatrix(Matrix4f inverseProjectionMatrix) {
        this.inverseProjectionMatrix = inverseProjectionMatrix;
    }

    
    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }

    public void setVolumetricParams(Vector3f lightColor, float stepSize, float fogAnisotropy, float fogDensity, float albedo) {
        this.stepSize = stepSize;
        this.lightColor = lightColor;
        this.fogAnisotropy = fogAnisotropy;
        this.fogDensity = fogDensity;
        this.albedo = albedo;
    }

    public void setAlbedo(float albedo) {
        this.albedo = albedo;
    }

    public void setFogDensity(float fogDensity) {
        this.fogDensity = fogDensity;
    }

    public void setStepSize(float stepSize) {
        this.stepSize = stepSize;
    }


    private int generateCloudNoise(CloudNoiseGenerator cloudNoiseGenerator) {
        float[][][] noiseArray = cloudNoiseGenerator.getNoiseMap();

        float[] flatArray = new float[noiseArray[0].length * noiseArray[0][0].length * noiseArray.length];
        for (int i = 0; i < noiseArray.length; i++) {
            for (int j = 0; j < noiseArray[0].length; j++) {
                for (int k = 0; k < noiseArray[0][0].length; k++) {
                    flatArray[i * (noiseArray[0].length * noiseArray[0][0].length) + j * noiseArray[0][0].length + k] = cloudNoiseGenerator.sample(i, j, k);
                }
            }
        }
        FloatBuffer buffer = ByteBuffer.allocateDirect(flatArray.length << 2)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(flatArray).flip();

        int noise3DTextureID = GL11C.glGenTextures();

        GL11C.glBindTexture(GL_TEXTURE_3D, noise3DTextureID);


        GL11C.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL11C.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GL11C.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GL11C.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GL11C.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);


        glTexImage3D(GL_TEXTURE_3D, 0, GL_R32F, noiseArray[0][0].length, noiseArray[0].length, noiseArray.length,
                0, GL_RED, GL_FLOAT, buffer);


        GL11C.glBindTexture(GL_TEXTURE_3D, 0);
        return noise3DTextureID;
    }


    public void setCloudMoveFactor(float moveFactor) {
        this.moveFactor = moveFactor;
    }

}
