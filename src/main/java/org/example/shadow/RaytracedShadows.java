package org.example.shadow;

import org.example.*;
import org.example.blur.bilateralblur.BilateralBlur;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL45.*;

public class RaytracedShadows extends PostProcessEffect {
    private RaytracedShadowsShader shader;
    private int texture;
    private Light light;

    private Matrix4f inverseViewMatrix;

    private SSBO triangleSSBO;
    private SSBO modelBVHSSBO;
    private SSBO triangleBVHSSBO;
    private Gbuffer gbuffer;

    private int trianglesLength;
    private int triangleBVHLength;
    private int modelBVHLength;

    private int gPositionTexture;
    private int gNormalTexture;
    private BilateralBlur bilateralBlur;

    public RaytracedShadows(Light light, Gbuffer gbuffer) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        texture = createTexture(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), 2);
        shader = new RaytracedShadowsShader();
        shader.init();
        this.light = light;
        bilateralBlur = new BilateralBlur(gbuffer, 4);
        this.gPositionTexture = gbuffer.getPositionTexture();
        this.gNormalTexture = gbuffer.getNormalTexture();
        this.gbuffer = gbuffer;

    }

    @Override
    public void render(Model screenQuad) {
        shader.start();
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }

        shader.loadLight(light);
        shader.loadInverseViewMatrix(inverseViewMatrix);
        shader.loadArrayLengths(trianglesLength, triangleBVHLength, modelBVHLength);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL11.glBindTexture(GL_TEXTURE_2D, texture);
        GL11.glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, gPositionTexture);
        glActiveTexture(GL_TEXTURE1);
        GL11.glBindTexture(GL_TEXTURE_2D, gNormalTexture);

        Renderer.renderCompute(
                Main.getDisplayManager().getWidth(),
                Main.getDisplayManager().getHeight(),
                texture,
                new SSBO[] {triangleSSBO, triangleBVHSSBO, modelBVHSSBO},
                GL_RGBA16F, 2
        );

        shader.stop();
        bilateralBlur.getHBlur().setTexture(texture);
        bilateralBlur.getVBlur().bindFrameBuffer();
        bilateralBlur.getHBlur().render(screenQuad);
        bilateralBlur.getVBlur().unbindFrameBuffer();
        super.getFbo().bindFrameBuffer();
        bilateralBlur.getVBlur().render(screenQuad);
        super.getFbo().unbindCurrentFrameBuffer();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
        bilateralBlur.cleanUp();
        glDeleteTextures(texture);
    }
    private int createTexture(int width, int height, int downScale) {
        int texture = glCreateTextures(GL_TEXTURE_2D);

        glTextureStorage2D(texture, 1, GL_RGBA16F, width / downScale, height / downScale);

        GL11.glBindTexture(GL_TEXTURE_2D, texture);
        glClearTexImage(texture, 0, GL_RGBA, GL_HALF_FLOAT, (ByteBuffer)null);
        GL11.glBindTexture(GL_TEXTURE_2D, 0);

        glTextureParameteri(texture, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTextureParameteri(texture, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(texture, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTextureParameteri(texture, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        return texture;
    }
    public void setInverseViewMatrix(Matrix4f inverseViewMatrix) {
        this.inverseViewMatrix = inverseViewMatrix;
    }

    public void setSSBOs(SSBO triangleSSBO, SSBO triangleBVHSSBO, SSBO modelBVHSSBO) {
        this.triangleBVHSSBO = triangleBVHSSBO;
        this.triangleSSBO = triangleSSBO;
        this.modelBVHSSBO = modelBVHSSBO;
    }

    public void setArrayLengths(int trianglesLength, int triangleBVHLength, int modelBVHLength) {
        this.trianglesLength = trianglesLength;
        this.triangleBVHLength = triangleBVHLength;
        this.modelBVHLength = modelBVHLength;
    }



    public int getRaytracedShadowMap() {
        return super.getFbo().getTexture();
    }


}
