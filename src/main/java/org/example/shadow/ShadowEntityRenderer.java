package org.example.shadow;

import org.example.Entity;
import org.example.GameMath;
import org.example.Main;
import org.example.Renderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_FRONT;

public class ShadowEntityRenderer {
    private ShadowShader shader;
    private Matrix4f projectionViewMatrix;

    public ShadowEntityRenderer(ShadowShader shader, Matrix4f projectionViewMatrix) {
        this.shader = shader;
        this.projectionViewMatrix = projectionViewMatrix;
    }

    public void render(ArrayList<Entity> entities) {
        GL11.glCullFace(GL_FRONT);
        for (Entity entity : entities) {

            Matrix4f modelMatrix = GameMath.createTransformationMatrix(entity.getPosition(),
                    entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
            Matrix4f mvpMatrix = new Matrix4f(projectionViewMatrix).mul(modelMatrix);

            shader.loadMvpMatrix(mvpMatrix);
            Renderer.renderModel(entity.getModel().getRawModel());
        }
        GL11.glCullFace(GL_BACK);
    }

}
