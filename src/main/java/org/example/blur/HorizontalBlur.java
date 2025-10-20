package org.example.blur;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11C.*;

public class HorizontalBlur extends PostProcessEffect {
    private HorizontalBlurShader shader;


    public HorizontalBlur(int targetFboWidth, int targetFboHeight) {
        super(new Fbo(targetFboWidth, targetFboHeight, Fbo.NONE));
        shader = new HorizontalBlurShader();
        shader.start();
        shader.connectTextureUnits();
        shader.loadTargetWidth(targetFboWidth);
        shader.stop();
    }

    @Override
    public void render(Model fullScreenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }

        shader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE10);
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
