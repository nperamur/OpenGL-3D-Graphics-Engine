package org.example.fbo;

import org.example.Main;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class Fbo {
    private int width;
    private int height;
    private int id;
    private int attachment;
    private int depthTexture;
    private int texture;
    private int depthId;

    public static final int NONE = 0;
    public static final int DEPTH_TEXTURE = 1;
    private ArrayList<Fbo> lowResFbos = new ArrayList<>();


    public Fbo(int width, int height, int attachment) {
        this.attachment = attachment;
        this.width = width;
        this.height = height;
        init();
        unbindCurrentFrameBuffer();
    }

    public void init() {
        // Call lowres framebuffers from outside FBO
        id = createFrameBuffer();
        this.texture = createTextureAttachment(width, height);
        if (attachment == DEPTH_TEXTURE) {
            depthTexture = createDepthTextureAttachment(width, height);
        } else {
            depthId = createDepthBufferAttachment();
        }
    }


    public void bindFrameBuffer() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);//To make sure the texture isn't bound
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
        GL11.glViewport(0, 0, width, height);
    }


    protected int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        //generate name for frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        //create the framebuffer
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        //indicate that we will always render to color attachment 0
        return frameBuffer;
    }


    private int createDepthTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
                0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                texture, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,0);
        return texture;
    }



    protected int createDepthBufferAttachment() {
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width,
                height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                GL30.GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }

    public void unbindCurrentFrameBuffer() {//call to switch to default frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
    }

    public int getId() {
        return this.id;
    }



    protected int createTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_RGBA, width, height,
                0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                texture, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,0);
        return texture;
    }

    public int getDepthTexture() {
        return this.depthTexture;
    }

    public int getTexture() {
        return this.texture;
    }


    public void resize(int newWidth, int newHeight) {
        int prevWidth = this.width;
        int prevHeight = this.height;
        this.width = newWidth;
        this.height = newHeight;
        cleanUp();
        bindFrameBuffer();
        this.init();
        unbindCurrentFrameBuffer();
        for (Fbo fbo : lowResFbos) {
            int childNewWidth = (int)((float) newWidth * fbo.width / prevWidth);
            int childNewHeight = (int)((float) newHeight * fbo.height / prevHeight);
            fbo.resize(childNewWidth, childNewHeight);
        }

    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public void downSampleAll() {
        for (Fbo lowResFbo : lowResFbos) {
            glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo.getId());
            downSample(GL_COLOR_ATTACHMENT0, lowResFbo.getId(), lowResFbo.getWidth(), lowResFbo.getHeight());
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }


    protected void downSample(int colorAttachment, int lowResFbo, int newWidth, int newHeight) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.id);
        glReadBuffer(colorAttachment);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, lowResFbo);
        glDrawBuffer(colorAttachment);
        glBlitFramebuffer(
                0, 0, width, height,
                0, 0, newWidth, newHeight,
                GL_COLOR_BUFFER_BIT,
                GL_LINEAR
        );
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }


    public int addLowResFrameBuffer(float scaleFactor) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int index = lowResFbos.size();
        Fbo lowResFbo = new Fbo((int) (width / scaleFactor), (int) (height / scaleFactor), Fbo.NONE);
        lowResFbos.add(lowResFbo);
        return index;
    }


    public int getLowResTexture(int index) {
        if (index >= lowResFbos.size() || index < 0) {
            throw new IndexOutOfBoundsException("Cannot have low res fbo index out of bounds");
        }
        return lowResFbos.get(index).texture;
    }

    public void cleanUp() {//call when closing the game
        for (Fbo lowResFbo : lowResFbos) {
            lowResFbo.cleanUp();
        }
        GL30.glDeleteFramebuffers(id);
        glDeleteRenderbuffers(depthId);
        if (attachment == DEPTH_TEXTURE) {
            GL11.glDeleteTextures(depthTexture);
        }
        GL11.glDeleteTextures(texture);
    }

    protected ArrayList<Fbo> getLowResFbos() {
        return lowResFbos;
    }

}
