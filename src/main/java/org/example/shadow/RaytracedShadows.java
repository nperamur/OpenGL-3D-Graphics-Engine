package org.example.shadow;

import org.example.*;
import org.example.blur.bilateralblur.BilateralBlur;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.example.temporal.TemporalAccumulation;
import org.example.temporal.TemporalFrameBuffer;
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

    private int trianglesLength;
    private int triangleBVHLength;
    private int modelBVHLength;

    private static final float DOWNSCALE = 2.5f;

    private int frameCount;

    private int gPositionTexture;
    private int gNormalTexture;


    private Fbo finalFbo;

    private TemporalAccumulation temporal;
    public RaytracedShadows(Light light, Gbuffer gbuffer) {
        super(new TemporalFrameBuffer(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        ((TemporalFrameBuffer) super.getFbo()).initializeHistoryFbo();
        finalFbo = new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE);
        texture = createTexture(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(),  DOWNSCALE);
        shader = new RaytracedShadowsShader();
        shader.init();
        this.light = light;
        this.gPositionTexture = gbuffer.getPositionTexture();
        this.gNormalTexture = gbuffer.getNormalTexture();
        temporal = new TemporalAccumulation((TemporalFrameBuffer) super.getFbo(), gbuffer);

    }

    @Override
    public void render(Model screenQuad) {
        shader.start();
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
            glDeleteTextures(texture);
            texture = createTexture(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(),  DOWNSCALE);

        }

        shader.loadLight(light);
        shader.loadInverseViewMatrix(inverseViewMatrix);
        shader.loadArrayLengths(trianglesLength, triangleBVHLength, modelBVHLength);
        shader.loadFrameCount(frameCount);
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
                GL_RGBA16F, DOWNSCALE
        );

        shader.stop();

        super.getFbo().bindFrameBuffer();
        temporal.setTexture(texture);
        temporal.render(screenQuad);
        super.getFbo().unbindCurrentFrameBuffer();
        ((TemporalFrameBuffer) super.getFbo()).updateHistoryBuffer();
        frameCount++;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
        finalFbo.cleanUp();
        temporal.cleanUp();
        glDeleteTextures(texture);
    }
    private int createTexture(int width, int height, float downScale) {
        int texture = glCreateTextures(GL_TEXTURE_2D);

        glTextureStorage2D(texture, 1, GL_RGBA16F, (int) (width / downScale), (int) (height / downScale));

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

    public void setModelBVHLength(int length) {
        this.modelBVHLength = length;
    }



    public int getRaytracedShadowMap() {
        return super.getFbo().getTexture();
    }


    public void setMatrices(Matrix4f prevViewMatrix, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        this.temporal.setMatrices(prevViewMatrix, viewMatrix, projectionMatrix);
    }

}
