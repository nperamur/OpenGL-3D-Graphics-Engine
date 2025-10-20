package org.example.blur;

import org.example.Main;
import org.example.Model;
import org.example.PostProcessEffect;

public class GaussianBlur {
    private HorizontalBlur hBlur;
    private VerticalBlur vBlur;
    
    public GaussianBlur(int strength) {
        hBlur = new HorizontalBlur(Main.getDisplayManager().getWidth()/strength, Main.getDisplayManager().getHeight()/strength);
        vBlur = new VerticalBlur(Main.getDisplayManager().getWidth()/strength, Main.getDisplayManager().getHeight()/strength);
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
