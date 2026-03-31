package org.example.ssbo;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;

public class SSBO {
    private int id;
    FloatBuffer buffer;

    public SSBO() {
        id = glGenBuffers();
    }


    public void bind(int slot) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, slot, id);

    }
    public void upload(float[] floats) {
        glBufferData(GL_SHADER_STORAGE_BUFFER, floats, GL_STATIC_DRAW);
    }


    public void allocateFloatBuffer(int matrixCount) {
        buffer = BufferUtils.createFloatBuffer(matrixCount * 16);

    }
    public void upload(Matrix4f[] matrices) {
        if (buffer == null || matrices.length * 16 > buffer.capacity()) {
            allocateFloatBuffer(matrices.length);
        }
        buffer.clear();
        for (int i = 0; i < matrices.length; i++) {
            matrices[i].get(i * 16, buffer);
        }
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
    }


    public void unbind() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }


    public void cleanUp() {
        glDeleteBuffers(id);
    }

    public int getId() {
        return this.id;
    }



}
