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

    private int strength;


    public BilateralVerticalBlur(Gbuffer gbuffer, int strength) {
        super(new Fbo(Main.getDisplayManager().getWidth() / strength, Main.getDisplayManager().getHeight() / strength, Fbo.NONE));
        shader = new BilateralVerticalBlurShader(Main.getDisplayManager().getHeight() / strength, 5);
        shader.init();
        this.strength = strength;
        this.gbuffer = gbuffer;
    }

    @Override
    public void render(Model fullScreenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() / strength != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() / strength != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth() / strength, Main.getDisplayManager().getHeight() / strength);
        }

        shader.start();
        shader.loadTargetHeight((float) Main.getDisplayManager().getHeight() / strength);
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