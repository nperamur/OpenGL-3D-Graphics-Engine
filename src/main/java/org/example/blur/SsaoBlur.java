package org.example.blur;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class SsaoBlur extends PostProcessEffect {
    private SsaoBlurShader shader;

    public SsaoBlur() {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        this.shader = new SsaoBlurShader();
        this.shader.start();
        this.shader.connectTextureUnits();
        this.shader.stop();
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
}
