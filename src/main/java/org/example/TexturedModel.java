package org.example;

public class TexturedModel {
    private Model rawModel;
    private ModelTexture texture;

    public TexturedModel(Model model, ModelTexture texture) {
        this.rawModel = model;
        this.texture = texture;
    }

    public Model getRawModel() {
        return rawModel;
    }

    public void setRawModel(Model rawModel) {
        this.rawModel = rawModel;
    }

    public ModelTexture getTexture() {
        return texture;
    }

    public void setTexture(ModelTexture texture) {
        this.texture = texture;
    }
}
