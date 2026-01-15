package org.example.volumetrics;

import org.example.Main;
import org.example.Model;
import org.example.PostProcessEffect;
import org.example.Renderer;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11C.*;

public class VolumetricBlend extends PostProcessEffect {
    private VolumetricBlendShader shader;
    private int originalTexture;
    public VolumetricBlend(VolumetricFrameBuffer volumetricFbo) {
        super(volumetricFbo);
        this.shader = new VolumetricBlendShader();
        this.shader.init();
        volumetricFbo.initializeHistoryFbo();
    }

    @Override
    public void render(Model screenQuad) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() != super.getFbo().getWidth()) {
            super.getFbo().resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        shader.start();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, originalTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL_TEXTURE_2D, super.getFbo().getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL_TEXTURE_2D, ((VolumetricFrameBuffer) super.getFbo()).getTransmittanceTexture());
        Renderer.renderModel(screenQuad);
        shader.stop();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        shader.cleanUp();
    }

    public void setOriginalTexture(int texture) {
        this.originalTexture = texture;
    }
}
