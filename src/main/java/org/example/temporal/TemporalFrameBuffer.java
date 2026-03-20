package org.example.temporal;

import org.example.fbo.Fbo;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class TemporalFrameBuffer extends Fbo {


    private TemporalFrameBuffer historyFbo;

    public TemporalFrameBuffer(int width, int height, int attachment) {
        super(width, height, attachment);
        unbindCurrentFrameBuffer();
    }

    @Override
    public void init() {
        super.init();
    }

    public void initializeHistoryFbo() {
        historyFbo = new TemporalFrameBuffer(getWidth(), getHeight(), Fbo.NONE);
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
    public int getHistoryTexture() {
        return this.historyFbo.getTexture();
    }




    @Override
    public void cleanUp() {
        super.cleanUp();
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

}
