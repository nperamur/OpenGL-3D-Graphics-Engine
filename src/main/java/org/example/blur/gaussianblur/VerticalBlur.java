package org.example.blur.gaussianblur;

import org.example.*;
import org.example.fbo.Fbo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class VerticalBlur extends PostProcessEffect {
    private VerticalBlurShader shader;

    private int strength;


    public VerticalBlur(int strength) {
        super(new Fbo(Main.getDisplayManager().getWidth() / strength, Main.getDisplayManager().getHeight() / strength, Fbo.NONE));
        shader = new VerticalBlurShader(Main.getDisplayManager().getHeight() / strength, 11);
        shader.init();
        this.strength = strength;

    }

    @Override
    public void render(Model fullScreenQuad) {
        if (Main.getDisplayManager().getHeight() / strength != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() / strength != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth() / strength, Main.getDisplayManager().getHeight() / strength);
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.start();
        shader.loadTargetHeight((float) Main.getDisplayManager().getHeight() / strength);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getFbo().getTexture());

        Renderer.renderModel(fullScreenQuad);

        shader.stop();
    }


    public VerticalBlurShader getShader() {
        return shader;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }
}
