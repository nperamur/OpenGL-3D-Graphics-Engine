package org.example.blur;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11C.*;

public class VerticalBlur extends PostProcessEffect {
    private VerticalBlurShader shader;


    public VerticalBlur(int targetFboWidth, int targetFboHeight) {
        super(new Fbo(targetFboWidth, targetFboHeight, Fbo.NONE));
        shader = new VerticalBlurShader();
        shader.start();
        shader.connectTextureUnits();
        shader.loadTargetHeight(targetFboHeight);
        shader.stop();
    }

    @Override
    public void render(Model fullScreenQuad) {
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE11);
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
