package org.example.bloom;

import org.example.Model;
import org.example.blur.GaussianBlur;
import org.example.blur.HorizontalBlur;
import org.example.blur.VerticalBlur;

public class Bloom {
    private BrightFilter brightFilter;
    private GaussianBlur gaussianBlur;
    private CombineTextures combineTextures;


    public Bloom() {
        this.brightFilter = new BrightFilter(0.5f);
        this.gaussianBlur = new GaussianBlur(5);
        this.combineTextures = new CombineTextures(true);

    }

    public void bindFrameBuffer() {
        brightFilter.bindFrameBuffer();
    }

    public void unbindFrameBuffer() {
        brightFilter.unbindFrameBuffer();
    }

    public void prepareRender(Model screenQuad) {
        brightFilter.getFbo().downSampleAll();
        gaussianBlur.bindFrameBuffer();
        brightFilter.render(screenQuad);
        gaussianBlur.unbindFrameBuffer();


        VerticalBlur vBlur = gaussianBlur.getVBlur();
        HorizontalBlur hBlur = gaussianBlur.getHBlur();

        vBlur.bindFrameBuffer();
        hBlur.render(screenQuad);
        vBlur.unbindFrameBuffer();

        combineTextures.bindFrameBuffer();
        vBlur.render(screenQuad);
        combineTextures.unbindFrameBuffer();

    }

    public void render(Model screenQuad) {
        combineTextures.setSecondTexture(brightFilter.getFbo().getTexture());
        combineTextures.render(screenQuad);
    }

    public void cleanUp() {
        brightFilter.cleanUp();
        gaussianBlur.cleanUp();
        combineTextures.cleanUp();
    }
}
