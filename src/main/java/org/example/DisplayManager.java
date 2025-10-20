package org.example;

import org.example.terrain.Terrain;
import org.example.terrain.TerrainShader;
import org.example.terrain.TerrainTexturePack;
import org.example.water.WaterShader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DisplayManager {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private long window;
    private double frameTime;
    private double lastTime;
    private Renderer renderer;
    private Player player;


    private int windowPosX = 100;
    private int windowPosY = 100;


    private boolean fullscreen = false;
    private boolean f11PressedLastFrame;

    public void start() {
        createWindow();
        loop();
        close();
    }

    public void createWindow() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(WIDTH, HEIGHT, "Game", NULL, NULL);



        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(0);
        glfwShowWindow(window);
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }

    }

    public void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        Loader loader = new Loader();
        TestShader shader = new TestShader();
        TerrainShader terrainShader = new TerrainShader();
        ArrayList<Terrain> terrains = new ArrayList<>();
        Terrain[][] terrainsArray = new Terrain[4][4];
        Sunlight light = new Sunlight(8000, new Vector3f(1.0f, 0.95f, 0.8f), loader);
        TerrainTexturePack texturePack = new TerrainTexturePack(
                new ModelTexture(loader.loadTexture("grass")),
                new ModelTexture(loader.loadTexture("dirt")),
                new ModelTexture(loader.loadTexture("pinkFlowers")),
                new ModelTexture(loader.loadTexture("path")), 20f, 0.8f);
        ModelTexture blendMap = new ModelTexture(loader.loadTexture("blendMap"));
        for (int i = -2; i < 2; i++) {
            for (int x = -2; x < 2; x++) {
                //ModelTexture texture = new ModelTexture(loader.loadTexture("terrain_texture"));
                Terrain terrain = new Terrain(i, x, loader, "heightmap", texturePack, blendMap);
                terrains.add(terrain);
                terrainsArray[i + 2][x + 2] = terrain;
            }
        }
        WaterShader waterShader = new WaterShader();
        // Positions of the 4 vertices (a square on the XZ plane)
        float[] positions = {
                0.0f, 0.0f, 0.0f,  // Vertex 0 (Bottom-left)
                1.0f, 0.0f, 0.0f,  // Vertex 1 (Bottom-right)
                0.0f, 0.0f, 1.0f,  // Vertex 2 (Top-left)
                1.0f, 0.0f, 1.0f   // Vertex 3 (Top-right)
        };

// Texture coordinates (UVs for the 4 vertices)
        float[] textureCoords = {
                0.0f, 0.0f,  // Vertex 0 (Bottom-left)
                1.0f, 0.0f,  // Vertex 1 (Bottom-right)
                0.0f, 1.0f,  // Vertex 2 (Top-left)
                1.0f, 1.0f   // Vertex 3 (Top-right)
        };

// Indices to form two triangles
        int[] waterIndices = {
                0, 2, 1,  // Triangle 1 (Vertex 0, 2, 1)
                1, 2, 3   // Triangle 2 (Vertex 1, 2, 3)
        };
        ModelTexture texture = new ModelTexture(loader.loadTexture("water"));
        TexturedModel waterModel = new TexturedModel(loader.loadToVao(positions, textureCoords, waterIndices, new float[0]), texture);
        FrameBuffers fbos = new FrameBuffers();
        PostProcessShader postProcessShader = new PostProcessShader();
        Gbuffer gbuffer = new Gbuffer();
        renderer = new Renderer(shader, terrainShader, waterShader, postProcessShader, terrains, light, waterModel, fbos, loader.loadTexture("dudv"), loader.loadTexture("normals"), gbuffer);
        float[] vertices = {
                // Front face
                -0.5f, -0.5f,  0.5f,  // Bottom-left
                0.5f, -0.5f,  0.5f,  // Bottom-right
                0.5f,  0.5f,  0.5f,  // Top-right
                -0.5f,  0.5f,  0.5f,  // Top-left

                // Back face
                -0.5f, -0.5f, -0.5f,  // Bottom-left
                0.5f, -0.5f, -0.5f,  // Bottom-right
                0.5f,  0.5f, -0.5f,  // Top-right
                -0.5f,  0.5f, -0.5f   // Top-left
        };
        int[] indices = {
                // Front face
                0, 1, 2,
                2, 3, 0,

                // Right face
                1, 5, 6,
                6, 2, 1,

                // Back face
                5, 4, 7,
                7, 6, 5,

                // Left face
                4, 0, 3,
                3, 7, 4,

                // Top face
                3, 2, 6,
                6, 7, 3,

                // Bottom face
                4, 5, 1,
                1, 0, 4
        };





        TexturedModel model = new TexturedModel(ModelLoader.load("FirstPine", loader), new ModelTexture(loader.loadTexture("pinesTexture")));

        Item entity = new Item(new TexturedModel(ModelLoader.load("soccer_ball", loader), new ModelTexture(loader.loadTexture("soccer_texture"))), 0, 0, 0, 1f);

        Entity entity2 = new Entity(model, new Vector3f(0, 9, 0f), 0,0, 0, 1f);

        Entity sunEntity = new Entity(new TexturedModel(light.getSunModel(), new ModelTexture(loader.loadTexture("sun"))), light.getPosition(), 0, 0, 0, 800);
        for (int i = 0; i < 1000; i++) {
            float worldX = (float) Math.random() * Terrain.SIZE * 4 - 2 * Terrain.SIZE;
            float worldZ = (float) Math.random() * Terrain.SIZE * 4 - 2 * Terrain.SIZE;
            float height = terrainsArray[(int) (Math.ceil(worldX / Terrain.SIZE)) + 1][(int) (Math.ceil(worldZ / Terrain.SIZE)) + 1].getHeightOfTerrain(worldX, worldZ);
            if (height > Renderer.WATER_Y) {
                renderer.addEntity(new Entity(model, new Vector3f(worldX, height - 3, worldZ), 0,0, 0, 1f));
            }
        }
        renderer.addEntity(entity2);
        renderer.addEntity(new Entity(new TexturedModel(loader.loadToVao(vertices, textureCoords, indices, new float[0]), new ModelTexture(loader.loadTexture("sun"))), new Vector3f(0, 50, 0), 0, 30, 0, 50));



        renderer.addEntity(sunEntity);
        //renderer.addEntity(new Entity(model, new Vector3f(-1, -5000.7f, -4), 0, 0, 0, 10000));
//        float[] vertices2 = {
//                0, 0, 0,
//                0, 0, 1000,
//                1000, 0, 0,
//                1000, 0, 1000
//        };
//        int[] indices2 = {
//                0, 2, 1, 1, 2, 3
//        };
//        Model model2 = loader.loadToVao(vertices2, indices2);
//        Entity entity2 = new Entity(model2, new Vector3f(-500, -3, -500), 0, 0, 0, 1);
        this.player = new Player(0, 0, 0, window);
//        player.holdItem(entity, renderer);
        Model fullScreenQuad = loader.loadToVao(new float[] {
                -1.0f,  1.0f, 0.0f, // Top-left
                -1.0f, -1.0f, 0.0f, // Bottom-left
                1.0f, -1.0f, 0.0f, // Bottom-right
                1.0f,  1.0f, 0.0f  // Top-right
        }, new float[] {
            0.0f, 1.0f, // Top-left
                    0.0f, 0.0f, // Bottom-left
                    1.0f, 0.0f, // Bottom-right
                    1.0f, 1.0f  // Top-right
        }, new int[] {
                0, 1, 2, // First triangle
                0, 2, 3  // Second triangle
        }, new float[]  {
                0.0f, 0.0f, 1.0f, // Top-left
                0.0f, 0.0f, 1.0f, // Bottom-left
                0.0f, 0.0f, 1.0f, // Bottom-right
                0.0f, 0.0f, 1.0f  // Top-right
        });

        renderer.initShadowsAndVolumetricLighting();
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }

        double previousTime = glfwGetTime();

        while ( !glfwWindowShouldClose(window) ) {
            if (glfwGetKey(window, GLFW_KEY_F11) == GLFW_PRESS && !f11PressedLastFrame) {
                fullscreen = !fullscreen;

                if (fullscreen) {
                    // Switch to fullscreen
                    long monitor = glfwGetPrimaryMonitor();
                    GLFWVidMode vidmode = glfwGetVideoMode(monitor);

                    glfwGetWindowPos(window, new int[] { windowPosX }, new int[] { windowPosY });
                    glfwGetWindowSize(window, new int[] { WIDTH }, new int[] { HEIGHT });

                    glfwSetWindowMonitor(window, monitor,
                            0, 0,
                            vidmode.width(), vidmode.height(),
                            vidmode.refreshRate());
                } else {
                    // Switch back to windowed
                    glfwSetWindowMonitor(window, 0,
                            windowPosX, windowPosY,
                            WIDTH, HEIGHT,
                            0); // refreshRate = 0 → don't change it
                }
            }
            f11PressedLastFrame = glfwGetKey(window, GLFW_KEY_F11) == GLFW_PRESS;


            double currentTime = glfwGetTime();


//            if (currentTime - previousTime >= 1.0) {
//                glfwSetWindowTitle(window, String.format("FPS: %d", (int) (1 / frameTime)));
//                previousTime = currentTime;
//            }



            if (getHeight() != gbuffer.getHeight() && getWidth() != gbuffer.getWidth()) {
                gbuffer.resize(getWidth(), getHeight());
            }
            float skyBrightness = (float) Math.sin(light.getAngle());
            sunEntity.setPosition(light.getPosition());
            renderer.init(0.53f * skyBrightness, 0.81f * skyBrightness, 0.92f * skyBrightness, 0);
            //renderer.render(entity2, shader, camera);

            //reflection

            fbos.bindReflectionFrameBuffer();
            float distance = 2 * (player.getPosition().y + 0.5f - Renderer.WATER_Y);
            player.getPosition().y -= distance;
            player.invertPitch();
            renderer.render(player, new Vector4f(0, 1, 0, -Renderer.WATER_Y));
            player.getPosition().y += distance;
            player.invertPitch();
            fbos.unbindCurrentFrameBuffer();


            //refraction
            fbos.bindRefractionFrameBuffer();
            renderer.render(player, new Vector4f(0, -1, 0, Renderer.WATER_Y));
            fbos.unbindCurrentFrameBuffer();

            //regular scene
            gbuffer.bindFrameBuffer();
            renderer.render(player, new Vector4f(0, -1, 0, 100));
            gbuffer.unbindCurrentFrameBuffer();

            //postProcessing

            renderer.doPostProcessing(fullScreenQuad);





            player.move(terrainsArray[(int) (Math.ceil(player.getPosition().x / Terrain.SIZE)) + 1][(int) (Math.ceil(player.getPosition().z / Terrain.SIZE)) + 1]);
            light.updatePosition();
            glfwSwapBuffers(window); // swap the color buffers
            frameTime = GLFW.glfwGetTime() - lastTime;
            lastTime = GLFW.glfwGetTime();
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

        }
        loader.cleanUp();
        shader.cleanUp();
        terrainShader.cleanUp();
        waterShader.cleanUp();
        fbos.cleanUp();
        renderer.cleanUp();

    }

    public void close() {
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        GL.setCapabilities(null);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public int getWidth() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(window, width, height);
        return width[0];
    }

    public int getHeight() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(window, width, height);
        return height[0];
    }

    public long getWindow() {
        return window;
    }

    public double getFrameTimeInSeconds() {
        return frameTime;
    }


    public Renderer getRenderer() {
        return this.renderer;
    }

    public Player getPlayer() {
        return this.player;
    }
}
