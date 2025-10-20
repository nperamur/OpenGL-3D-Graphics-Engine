package org.example.bloom;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class BrightFilter extends PostProcessEffect {
    private float threshold;
    private BrightFilterShader shader;

    public BrightFilter(float threshold) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        this.threshold = threshold;
        this.shader = new BrightFilterShader();
        shader.start();
        shader.connectTextureUnits();
        shader.stop();
    }

    @Override
    public void render(Model screenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        this.shader.start();
        this.shader.loadThreshold(threshold);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getFbo().getLowResTexture());
        Renderer.renderModel(screenQuad);
        this.shader.stop();
    }

    public void setThreshold(int threshold) {
        this.shader.loadThreshold(threshold);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }
}
