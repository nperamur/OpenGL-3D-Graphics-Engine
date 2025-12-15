package org.example.volumetrics;

import org.example.*;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.example.shadow.ShadowRenderer;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.*;

public class VolumetricLighting extends PostProcessEffect {
    private VolumetricLightingShader shader;
    private Sunlight light;
    private Gbuffer gbuffer;
    private ShadowRenderer shadowRenderer;


    private Vector3f lightColor;
    private float fogAnisotropy;
    private float fogDensity;
    private float albedo;
    private float stepSize;


    public VolumetricLighting(Sunlight light, Gbuffer gbuffer, ShadowRenderer shadowRenderer) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        shader = new VolumetricLightingShader();
        shader.start();
        shader.connectTextureUnits();
        shader.stop();
        this.light = light;
        this.gbuffer = gbuffer;
        this.shadowRenderer = shadowRenderer;
    }

    @Override
    public void render(Model screenQuad) {
        if (super.getFbo().getWidth() != Main.getDisplayManager().getWidth() || super.getFbo().getHeight() != Main.getDisplayManager().getHeight()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.start();
        shader.loadLight(light);
        shader.loadLightColor(this.lightColor);
        shader.loadFogDensity(this.fogDensity);
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

        Renderer.renderModel(screenQuad);
        shader.stop();
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
}
