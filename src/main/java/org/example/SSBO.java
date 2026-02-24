package org.example;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;

public class SSBO {
    private int id;
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
