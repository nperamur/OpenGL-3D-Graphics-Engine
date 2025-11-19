package org.example.tonemapping;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.*;

public class ToneMapping extends PostProcessEffect {
    private ToneMappingShader shader;
    private float exposure;

    public ToneMapping(float exposure) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        shader = new ToneMappingShader();
        shader.start();
        shader.connectTextureUnits();
        shader.stop();
        this.exposure = exposure;
    }

    @Override
    public void render(Model screenQuad) {
        if (super.getFbo().getWidth() != Main.getDisplayManager().getWidth() || super.getFbo().getHeight() != Main.getDisplayManager().getHeight()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.start();
        shader.loadExposure(exposure);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, super.getFbo().getTexture());
        Renderer.renderModel(screenQuad);
        shader.stop();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }

    public void setExposure(float exposure) {
        this.exposure = exposure;
    }
}
