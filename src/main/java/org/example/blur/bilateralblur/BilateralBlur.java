package org.example.blur.bilateralblur;

import org.example.*;
import org.example.fbo.Gbuffer;

public class BilateralBlur {
    private BilateralHorizontalBlur hBlur;
    private BilateralVerticalBlur vBlur;
    private Gbuffer gbuffer;

    public BilateralBlur(Gbuffer gbuffer, int strength) {
        this.gbuffer = gbuffer;
        hBlur = new BilateralHorizontalBlur(gbuffer, Main.getDisplayManager().getWidth() / strength);
        vBlur = new BilateralVerticalBlur(gbuffer, Main.getDisplayManager().getHeight() / strength);
    }


    public void render(Model fullScreenQuad) {
        this.vBlur.bindFrameBuffer();
        this.hBlur.render(fullScreenQuad);
        this.vBlur.unbindFrameBuffer();
        this.vBlur.render(fullScreenQuad);
    }


    public void bindFrameBuffer() {
        this.hBlur.bindFrameBuffer();
    }

    public void unbindFrameBuffer() {
        this.hBlur.unbindFrameBuffer();
    }

    public BilateralVerticalBlur getVBlur() {
        return vBlur;
    }

    public BilateralHorizontalBlur getHBlur() {
        return hBlur;
    }

    public Gbuffer getGbuffer() {
        return this.gbuffer;
    }

    public void cleanUp() {
        hBlur.cleanUp();
        vBlur.cleanUp();
    }
}

