package org.example.blur.gaussianblur;

import org.example.Main;
import org.example.Model;

public class GaussianBlur {
    private HorizontalBlur hBlur;
    private VerticalBlur vBlur;
    
    public GaussianBlur(int strength) {
        hBlur = new HorizontalBlur(strength);
        vBlur = new VerticalBlur(strength);
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

    public VerticalBlur getVBlur() {
        return vBlur;
    }

    public HorizontalBlur getHBlur() {
        return hBlur;
    }

    public void cleanUp() {
        hBlur.cleanUp();
        vBlur.cleanUp();
    }
}
