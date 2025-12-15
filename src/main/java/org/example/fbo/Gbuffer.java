package org.example.fbo;

import org.example.Main;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Gbuffer extends Fbo {
    private int positionTexture;
    private int normalTexture;

    public Gbuffer() {
        super(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.DEPTH_TEXTURE);
        unbindCurrentFrameBuffer();
    }

    public Gbuffer(int width, int height, int attachment) {
        super(width, height, attachment);
        unbindCurrentFrameBuffer();
    }

    @Override
    public void init() {
        super.init();
        this.positionTexture = createPositionTextureAttachment(this.getWidth(), this.getHeight());
        this.normalTexture = createNormalTextureAttachment(this.getWidth(), this.getHeight());
    }



    private int createNormalTextureAttachment(int width, int height) {
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

    private int createPositionTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F, width, height,
                0, GL11.GL_RGB, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2,
                texture, 0);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
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
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(3);
        drawBuffers.put(GL_COLOR_ATTACHMENT0);
        drawBuffers.put(GL_COLOR_ATTACHMENT1);
        drawBuffers.put(GL_COLOR_ATTACHMENT2);
        drawBuffers.flip();
        glDrawBuffers(drawBuffers);
    }


    @Override
    public void downSampleAll() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        for (Fbo gbuffer : getLowResFbos()) {
            downSample(GL_COLOR_ATTACHMENT0, gbuffer.getId(), gbuffer.getWidth(), gbuffer.getHeight());
            downSample(GL_COLOR_ATTACHMENT1, gbuffer.getId(), gbuffer.getWidth(), gbuffer.getHeight());
            downSample(GL_COLOR_ATTACHMENT2, gbuffer.getId(), gbuffer.getWidth(), gbuffer.getHeight());
        }
    }

    @Override
    public int addLowResFrameBuffer(float scaleFactor) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int index = this.getLowResFbos().size();
        Gbuffer lowResFbo = new Gbuffer((int) (getWidth() / scaleFactor), (int) (getHeight() / scaleFactor), Fbo.NONE);
        getLowResFbos().add(lowResFbo);
        return index;
    }

    @Override
    protected void downSample(int colorAttachment, int lowResFbo, int newWidth, int newHeight) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, super.getId());
        glReadBuffer(colorAttachment);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, lowResFbo);
        glDrawBuffer(colorAttachment);
        glBlitFramebuffer(
                0, 0, getWidth(), getHeight(),
                0, 0, newWidth, newHeight,
                GL_COLOR_BUFFER_BIT,
                GL_LINEAR
        );
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }


    public int getPositionTexture() {
        return this.positionTexture;
    }

    public int getNormalTexture() {
        return this.normalTexture;
    }






    public int getLowResTexture(int index) {
        if (index >= getLowResFbos().size() || index < 0) {
            throw new IndexOutOfBoundsException("Cannot have low res fbo index out of bounds");
        }
        return getLowResFbos().get(index).getTexture();
    }

    public int getLowResPositionTexture(int index) {
        if (index >= getLowResFbos().size() || index < 0) {
            throw new IndexOutOfBoundsException("Cannot have low res fbo index out of bounds");
        }
        return ((Gbuffer) getLowResFbos().get(index)).positionTexture;
    }

    public int getLowResNormalTexture(int index) {
        if (index >= getLowResFbos().size() || index < 0) {
            throw new IndexOutOfBoundsException("Cannot have low res fbo index out of bounds");
        }
        return ((Gbuffer) getLowResFbos().get(index)).normalTexture;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        glDeleteTextures(positionTexture);
        glDeleteTextures(normalTexture);
        for (Fbo gbuffer : getLowResFbos()) {
            gbuffer.cleanUp();
        }
    }
}
