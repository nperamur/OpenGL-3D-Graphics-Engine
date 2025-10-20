package org.example;

import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Fbo {
    private int width;
    private int height;
    private int id;
    private int attachment;
    private int depthTexture;
    private int texture;
    private int lowResTexture;
    private int depthId;

    public static final int NONE = 0;
    public static final int DEPTH_TEXTURE = 1;

    private int lowResFbo;


    public Fbo(int width, int height, int attachment) {
        this.attachment = attachment;
        this.width = width;
        this.height = height;
        init();
        unbindCurrentFrameBuffer();
    }

    public void init() {
        createLowResFrameBuffersAndTextureAttachments();
        id = createFrameBuffer();
        this.texture = createTextureAttachment();
        if (attachment == DEPTH_TEXTURE) {
            depthTexture = createDepthTextureAttachment();
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


    private int createDepthTextureAttachment(){
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
                0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                texture, 0);
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
        GL11.glViewport(0, 0, width, height);
    }

    public int getId() {
        return this.id;
    }



    protected int createTextureAttachment() {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                texture, 0);
        return texture;
    }

    public int getDepthTexture() {
        return this.depthTexture;
    }

    public int getTexture() {
        return this.texture;
    }


    public void resize(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        cleanUp();



        this.init();
        unbindCurrentFrameBuffer();

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
        glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo);
        downSample(GL_COLOR_ATTACHMENT0, this.lowResFbo);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    protected void downSample(int colorAttachment, int lowResFbo) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.id);
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
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }


    protected void createLowResFrameBuffersAndTextureAttachments() {
        lowResFbo = createFrameBuffer();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lowResFbo);
        this.width = Main.getDisplayManager().getWidth() / 6;
        this.height = Main.getDisplayManager().getHeight() / 6;
        this.lowResTexture = this.createTextureAttachment();
        this.width = Main.getDisplayManager().getWidth();
        this.height = Main.getDisplayManager().getHeight();



        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

    }


    public int getLowResTexture() {
        return this.lowResTexture;
    }

    public void cleanUp() {//call when closing the game
        GL30.glDeleteFramebuffers(lowResFbo);
        GL11.glDeleteTextures(lowResTexture);
        GL30.glDeleteFramebuffers(id);
        glDeleteRenderbuffers(depthId);
        if (attachment == DEPTH_TEXTURE) {
            GL11.glDeleteTextures(depthTexture);
        }
        GL11.glDeleteTextures(texture);
    }

}
