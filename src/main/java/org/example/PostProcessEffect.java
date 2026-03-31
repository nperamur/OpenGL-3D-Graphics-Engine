package org.example;

import org.example.fbo.Fbo;
import org.example.volumetrics.VolumetricFrameBuffer;

public abstract class PostProcessEffect {
    private Fbo fbo;

    public PostProcessEffect(Fbo fbo) {
        this.fbo = fbo;
    }

    public abstract void render(Model screenQuad);

    public void bindFrameBuffer() {
        fbo.bindFrameBuffer();
    }


    public void unbindFrameBuffer() {
        fbo.unbindCurrentFrameBuffer();
    }

    public Fbo getFbo() {
        return fbo;
    }

    public void cleanUp() {
        fbo.cleanUp();
    }

    protected void setFbo(Fbo fbo) {
        this.fbo = fbo;
    }
}
