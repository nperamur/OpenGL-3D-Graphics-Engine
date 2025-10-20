package org.example.terrain;

import org.example.ModelTexture;

public class TerrainTexturePack {
    private ModelTexture backgroundTexture;
    private ModelTexture rTexture;
    private ModelTexture gTexture;
    private ModelTexture bTexture;
    private float shineDamper;
    private float reflectivity;

    public TerrainTexturePack(ModelTexture backgroundTexture, ModelTexture rTexture, ModelTexture gTexture, ModelTexture bTexture, float shineDamper, float reflectivity) {
        this.backgroundTexture = backgroundTexture;
        this.rTexture = rTexture;
        this.gTexture = gTexture;
        this.bTexture = bTexture;
        this.shineDamper = shineDamper;
        this.reflectivity = reflectivity;
    }

    public ModelTexture getBackgroundTexture() {
        return backgroundTexture;
    }

    public void setBackgroundTexture(ModelTexture backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
    }

    public ModelTexture getrTexture() {
        return rTexture;
    }

    public void setrTexture(ModelTexture rTexture) {
        this.rTexture = rTexture;
    }

    public ModelTexture getgTexture() {
        return gTexture;
    }

    public void setgTexture(ModelTexture gTexture) {
        this.gTexture = gTexture;
    }

    public ModelTexture getbTexture() {
        return bTexture;
    }

    public void setbTexture(ModelTexture bTexture) {
        this.bTexture = bTexture;
    }

    public float getShineDamper() {
        return this.shineDamper;
    }

    public float getReflectivity() {
        return this.reflectivity;
    }
}
