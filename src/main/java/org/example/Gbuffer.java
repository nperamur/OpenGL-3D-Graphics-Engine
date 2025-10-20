package org.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Gbuffer extends Fbo {
    private int positionTexture;
    private int normalTexture;
    private int lowResFbo;


    private int lowResTexture;
    private int lowResPositionTexture;
    private int lowResNormalTexture;

    public Gbuffer() {
        super(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.DEPTH_TEXTURE);
        unbindCurrentFrameBuffer();
    }

    @Override
    public void init() {
        super.init();
        this.positionTexture = createPositionTextureAttachment();
        this.normalTexture = createNormalTextureAttachment();
//        this.createLowResFrameBuffersAndTextureAttachments();
    }



    private int createNormalTextureAttachment() {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F, super.getWidth(), super.getHeight(),
                0, GL11.GL_RGB, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1,
                texture, 0);
        return texture;
    }

    private int createPositionTextureAttachment() {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F, super.getWidth(), super.getHeight(),
                0, GL11.GL_RGB, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2,
                texture, 0);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
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
        downSample(GL_COLOR_ATTACHMENT0, lowResFbo);
        downSample(GL_COLOR_ATTACHMENT1, lowResFbo);
        downSample(GL_COLOR_ATTACHMENT2, lowResFbo);
    }


    @Override
    protected void createLowResFrameBuffersAndTextureAttachments() {
        lowResFbo = createFrameBuffer();

        super.setWidth(Main.getDisplayManager().getWidth() / 6);
        super.setHeight(Main.getDisplayManager().getHeight() / 6);
        this.lowResTexture = this.createTextureAttachment();
        this.lowResNormalTexture = this.createNormalTextureAttachment();
        this.lowResPositionTexture = this.createPositionTextureAttachment();
        super.setWidth(Main.getDisplayManager().getWidth());
        super.setHeight(Main.getDisplayManager().getHeight());




        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

    }


    @Override
    protected void downSample(int colorAttachment, int lowResFbo) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, super.getId());
        glReadBuffer(colorAttachment);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, lowResFbo);
        glDrawBuffer(colorAttachment);
        glBlitFramebuffer(
                0, 0, Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(),
                0, 0, Main.getDisplayManager().getWidth() / 6, Main.getDisplayManager().getHeight() / 6,
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





    public int getLowResTexture() {
        return this.lowResTexture;
    }

    public int getLowResPositionTexture() {
        return this.lowResPositionTexture;
    }

    public int getLowResNormalTexture() {
        return this.lowResNormalTexture;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        glDeleteTextures(positionTexture);
        glDeleteTextures(normalTexture);
        glDeleteTextures(lowResTexture);
        glDeleteFramebuffers(lowResFbo);
        glDeleteTextures(lowResPositionTexture);
        glDeleteTextures(lowResNormalTexture);
    }
}
