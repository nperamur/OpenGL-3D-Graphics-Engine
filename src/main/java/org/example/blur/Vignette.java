package org.example.blur;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class Vignette extends PostProcessEffect {
    private VignetteShader shader;

    public Vignette() {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        this.shader = new VignetteShader();
        shader.connectTextureUnits();
    }

    @Override
    public void render(Model screenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        this.shader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getFbo().getTexture());
        Renderer.renderModel(screenQuad);
        this.shader.stop();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }

    public void bindFrameBuffer() {
        super.getFbo().bindFrameBuffer();
    }

    public void unbindFrameBuffer() {
        super.getFbo().unbindCurrentFrameBuffer();
    }

}
