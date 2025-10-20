package org.example.bloom;

import org.example.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11C.*;

public class CombineTextures extends PostProcessEffect {
    private CombineShader combineShader;
    private int secondTexture;

    public CombineTextures(boolean add) {
        super(new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE));
        combineShader = new CombineShader();
        combineShader.start();
        combineShader.connectTextureUnits();
        combineShader.loadAdd(add);
        combineShader.stop();
    }

    @Override
    public void render(Model screenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        this.combineShader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, super.getFbo().getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, secondTexture);
        Renderer.renderModel(screenQuad);
        this.combineShader.stop();
    }

    public void setSecondTexture(int texture) {
        this.secondTexture = texture;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        combineShader.cleanUp();
        glDeleteTextures(secondTexture);

    }
}
