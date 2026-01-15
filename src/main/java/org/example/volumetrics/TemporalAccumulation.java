package org.example.volumetrics;

import org.example.Main;
import org.example.Model;
import org.example.PostProcessEffect;
import org.example.Renderer;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class TemporalAccumulation extends PostProcessEffect {

    private VolumetricFrameBuffer historyFbo;
    private TemporalAccumulationShader shader;

    private Matrix4f prevViewMatrix;
    private Matrix4f viewMatrix;

    private Gbuffer gbuffer;

    private Gbuffer historyGbuffer;

    private Matrix4f projectionMatrix = new Matrix4f();
    private float prevMoveFactor;
    private float moveFactor;


    public TemporalAccumulation(VolumetricFrameBuffer volumetricFbo, Gbuffer gbuffer) {
        super(new VolumetricFrameBuffer(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        this.historyFbo = volumetricFbo;
        shader = new TemporalAccumulationShader();
        this.gbuffer = gbuffer;
        shader.init();
        historyGbuffer = new Gbuffer();
    }

    @Override
    public void render(Model screenQuad) {
        GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
            gbuffer.resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());

        }
        shader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, super.getFbo().getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL_TEXTURE_2D, ((VolumetricFrameBuffer) super.getFbo()).getTransmittanceTexture());
        if (historyFbo != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            GL11.glBindTexture(GL_TEXTURE_2D, historyFbo.getHistoryTexture());
            GL13.glActiveTexture(GL13.GL_TEXTURE3);
            GL11.glBindTexture(GL_TEXTURE_2D, historyFbo.getHistoryTransmittanceTexture());
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL_TEXTURE_2D, historyGbuffer.getPositionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE5);
        GL11.glBindTexture(GL_TEXTURE_2D, gbuffer.getPositionTexture());
        Matrix4f inverseViewMatrix = new Matrix4f();
        viewMatrix.invert(inverseViewMatrix);
        Matrix4f inverseProjMatrix = new Matrix4f();
        projectionMatrix.invert(inverseProjMatrix);
        shader.loadViewMatrices(prevViewMatrix, viewMatrix, projectionMatrix, inverseViewMatrix, inverseProjMatrix);
        shader.loadCloudMoveFactors(moveFactor, prevMoveFactor);
        Renderer.renderModel(screenQuad);
        shader.stop();

        updateHistoryGBuffer();

        this.prevMoveFactor = moveFactor;


    }


    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
        historyFbo.cleanUp();
        historyGbuffer.cleanUp();
    }

    public void setViewMatrices(Matrix4f prevViewMatrix, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        this.prevViewMatrix = prevViewMatrix;
        this.viewMatrix = viewMatrix;
        this.projectionMatrix = projectionMatrix;
    }

    public void updateHistoryGBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, gbuffer.getId());
        drawHistoryGBuffer(GL_COLOR_ATTACHMENT1, gbuffer.getId());
        drawHistoryGBuffer(GL_COLOR_ATTACHMENT2, gbuffer.getId());
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    private void drawHistoryGBuffer(int colorAttachment, int fbo) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
        GL11C.glReadBuffer(colorAttachment);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.historyGbuffer.getId());
        GL11C.glDrawBuffer(colorAttachment);
        glBlitFramebuffer(
                0, 0, gbuffer.getWidth(), gbuffer.getHeight(),
                0, 0, historyGbuffer.getWidth(), historyGbuffer.getHeight(),
                GL_COLOR_BUFFER_BIT,
                GL_LINEAR
        );
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    public void setCloudMoveFactor(float moveFactor) {
        this.moveFactor = moveFactor;
    }
}
