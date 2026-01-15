package org.example.volumetrics;

import org.example.Main;
import org.example.fbo.Fbo;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class VolumetricFrameBuffer extends Fbo {
    private int transmittanceTexture;


    private VolumetricFrameBuffer historyFbo;



    public VolumetricFrameBuffer(int width, int height, int attachment) {
        super(width, height, attachment);
        unbindCurrentFrameBuffer();
    }

    @Override
    public void init() {
        super.init();
        this.transmittanceTexture = createTransmittanceTextureAttachment(this.getWidth(), this.getHeight());
    }

    public void initializeHistoryFbo() {
        historyFbo = new VolumetricFrameBuffer(getWidth(), getHeight(), Fbo.NONE);
    }



    private int createTransmittanceTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F, width, height,
                0, GL11.GL_RGB, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1,
                texture, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,0);
        return texture;
    }




    @Override
    protected int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);

        drawBuffers();
        return frameBuffer;
    }


    private void drawBuffers() {
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(2);
        drawBuffers.put(GL_COLOR_ATTACHMENT0);
        drawBuffers.put(GL_COLOR_ATTACHMENT1);
        drawBuffers.flip();
        glDrawBuffers(drawBuffers);
    }


    public int getTransmittanceTexture() {
        return this.transmittanceTexture;
    }

    public int getHistoryTexture() {
        return this.historyFbo.getTexture();
    }







    @Override
    public void cleanUp() {
        super.cleanUp();
        glDeleteTextures(transmittanceTexture);
        for (Fbo gbuffer : getLowResFbos()) {
            gbuffer.cleanUp();
        }
        if (historyFbo != null) {
            historyFbo.cleanUp();
        }
    }

    public void updateHistoryBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, historyFbo.getId());
        drawHistoryBuffer(GL_COLOR_ATTACHMENT0, historyFbo.getId(), historyFbo.getWidth(), historyFbo.getHeight());
        drawHistoryBuffer(GL_COLOR_ATTACHMENT1, historyFbo.getId(), historyFbo.getWidth(), historyFbo.getHeight());
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void drawHistoryBuffer(int colorAttachment, int historyFbo, int newWidth, int newHeight) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, super.getId());
        glReadBuffer(colorAttachment);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, historyFbo);
        glDrawBuffer(colorAttachment);
        glBlitFramebuffer(
                0, 0, super.getWidth(), super.getHeight(),
                0, 0, newWidth, newHeight,
                GL_COLOR_BUFFER_BIT,
                GL_LINEAR
        );
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }


    @Override
    public void resize(int newWidth, int newHeight) {
        int prevWidth = super.getWidth();
        int prevHeight = super.getHeight();
        super.setWidth(newWidth);
        super.setHeight(newHeight);
        cleanUp();
        bindFrameBuffer();
        this.init();
        unbindCurrentFrameBuffer();
        for (Fbo fbo : super.getLowResFbos()) {
            int childNewWidth = (int)((float) newWidth * fbo.getWidth() / prevWidth);
            int childNewHeight = (int)((float) newHeight * fbo.getHeight() / prevHeight);
            fbo.resize(childNewWidth, childNewHeight);
        }
        if (historyFbo != null) {
            historyFbo.resize(newWidth, newHeight);
        }

    }

    public int getHistoryTransmittanceTexture() {
        return historyFbo.getTransmittanceTexture();
    }

}
