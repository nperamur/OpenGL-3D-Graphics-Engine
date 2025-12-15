package org.example;

import com.sun.jna.platform.win32.Kernel32;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private static DisplayManager displayManager;
    public static void main(String[] args) {
        Kernel32.INSTANCE.SetPriorityClass(
                Kernel32.INSTANCE.GetCurrentProcess(),
                Kernel32.HIGH_PRIORITY_CLASS
        );
        displayManager = new DisplayManager();
        displayManager.start();
    }

    public static DisplayManager getDisplayManager() {
        return displayManager;
    }

}