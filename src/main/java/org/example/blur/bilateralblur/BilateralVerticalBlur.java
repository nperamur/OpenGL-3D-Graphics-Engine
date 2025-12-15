package org.example.blur.bilateralblur;

import org.example.*;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class BilateralVerticalBlur extends PostProcessEffect {
    private BilateralVerticalBlurShader shader;
    private Gbuffer gbuffer;
    private int locationTargetHeight;


    public BilateralVerticalBlur(Gbuffer gbuffer, int targetHeight) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        shader = new BilateralVerticalBlurShader();
        shader.start();
        shader.connectTextureUnits();
        shader.loadNumSamples(5);
        shader.loadTargetHeight(targetHeight);
        shader.stop();
        this.gbuffer = gbuffer;
    }

    @Override
    public void render(Model fullScreenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }

        shader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getFbo().getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, gbuffer.getPositionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, gbuffer.getNormalTexture());

        Renderer.renderModel(fullScreenQuad);

        shader.stop();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }


}