package org.example.volumetrics;

import org.example.Main;
import org.example.Model;
import org.example.PostProcessEffect;
import org.example.Renderer;
import org.example.fbo.Fbo;
import org.example.fbo.Gbuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;

public class BilateralUpsample extends PostProcessEffect {

    private BilateralUpsampleShader shader;

    private float scaleFactor;

    private Gbuffer gbuffer;

    public BilateralUpsample(float scaleFactor, Gbuffer gbuffer) {
        super(new VolumetricFrameBuffer((int) (Main.getDisplayManager().getWidth() * scaleFactor), (int) (Main.getDisplayManager().getHeight() * scaleFactor), Fbo.NONE));
        shader = new BilateralUpsampleShader();
        shader.init();
        this.scaleFactor = scaleFactor;
        this.gbuffer = gbuffer;
    }

    @Override
    public void render(Model screenQuad) {
        GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (Main.getDisplayManager().getHeight() * scaleFactor != super.getFbo().getHeight() || Main.getDisplayManager().getWidth() * scaleFactor != super.getFbo().getWidth()) {
            super.getFbo().resize((int) (Main.getDisplayManager().getWidth() * scaleFactor), (int) (Main.getDisplayManager().getHeight() * scaleFactor));
        }

        shader.start();
        shader.loadScaleFactor(scaleFactor);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, super.getFbo().getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL_TEXTURE_2D, ((VolumetricFrameBuffer) super.getFbo()).getTransmittanceTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL_TEXTURE_2D, gbuffer.getPositionTexture());

        Renderer.renderModel(screenQuad);
        shader.stop();
    }
}
