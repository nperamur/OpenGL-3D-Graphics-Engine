package org.example.blur.gaussianblur;

import org.example.*;
import org.example.fbo.Fbo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class HorizontalBlur extends PostProcessEffect {
    private HorizontalBlurShader shader;
    private int strength;


    public HorizontalBlur(int strength) {
        super(new Fbo(Main.getDisplayManager().getWidth() / strength,  Main.getDisplayManager().getHeight() / strength, Fbo.NONE));
        this.strength = strength;
        shader = new HorizontalBlurShader(Main.getDisplayManager().getWidth() / strength, 11);
        shader.init();
    }

    @Override
    public void render(Model fullScreenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() / strength != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() / strength != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth() / strength, Main.getDisplayManager().getHeight() / strength);
        }
        shader.start();
        shader.loadTargetWidth((float) Main.getDisplayManager().getWidth() / strength);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getFbo().getTexture());
        Renderer.renderModel(fullScreenQuad);
        shader.stop();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }
}
