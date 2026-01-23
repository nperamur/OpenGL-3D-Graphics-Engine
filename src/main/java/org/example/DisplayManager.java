package org.example;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;
import org.example.fbo.Gbuffer;
import org.example.terrain.Terrain;
import org.example.terrain.TerrainShader;
import org.example.terrain.TerrainTexturePack;
import org.example.water.FrameBuffers;
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
import static org.lwjgl.system.MemoryUtil.NULL;

public class DisplayManager {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private long window;
    private double frameTime;
    private double lastTime;
    private Renderer renderer;
    private Player player;
    private final ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();


    private int windowPosX = 100;
    private int windowPosY = 100;

    private Vector3f skyColor;


    private boolean fullscreen = false;
    private boolean f11PressedLastFrame;
    private boolean inDebugMode = true; //Hide with f3
    private boolean f3PressedLastFrame;

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
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(WIDTH, HEIGHT, "OpenGL Graphics Engine", NULL, NULL);

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

        ImGuiContext context = ImGui.createContext();
        ImGui.setCurrentContext(context);
        imGuiImplGlfw.init(window, true);
        imGuiImplGl3.init();

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
        SsaoShader ssaoShader = new SsaoShader(Renderer.generateRandomSampleKernels());
        Gbuffer gbuffer = new Gbuffer();
        gbuffer.addLowResFrameBuffer(2);
        renderer = new Renderer(shader, terrainShader, waterShader, ssaoShader, terrains, light, waterModel, fbos, loader.loadTexture("dudv"), loader.loadTexture("normals"), gbuffer);
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
                renderer.addEntity(new Entity(model, new Vector3f(worldX, height - 5, worldZ), 0,0, 0, 1f));
            }
        }
//        renderer.setHeldItem(entity);
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
        float[] fogDensity = new float[1];
        fogDensity[0] = 0.000498f;

        float[] fogAlbedo = new float[1];
        fogAlbedo[0] = 0.151f;

        float[] exposure = new float[1];
        exposure[0] = 1.8f;

        float[] stepSize = new float[1];
        stepSize[0] = 3f;

        while ( !glfwWindowShouldClose(window) ) {
            boolean f3Pressed = glfwGetKey(window, GLFW_KEY_F3) == GLFW_PRESS;

            if (f3Pressed && !f3PressedLastFrame) {
                inDebugMode = !inDebugMode;
            }

            f3PressedLastFrame = f3Pressed;
            if (inDebugMode) {
                imGuiImplGlfw.newFrame();
                imGuiImplGl3.newFrame();
                ImGui.newFrame();
                ImGui.setNextWindowSize(350, 0);
                ImGui.setNextWindowSizeConstraints(350, 0, 350, Float.MAX_VALUE);
                if (ImGui.begin("Config")) {
                    ImGui.text("fps:" + 1/frameTime);
                    if(ImGui.collapsingHeader("Volumetric Fog")) {
                        ImGui.sliderFloat("Density", fogDensity, 0f, 0.005f, "%.6f");
                        ImGui.sliderFloat("Albedo", fogAlbedo, 0, 0.5f, "%.6f");
                        ImGui.sliderFloat("Step Size", stepSize, 1f, 6f);

                    }
                    if(ImGui.collapsingHeader("Tone Mapping")) {
                        ImGui.sliderFloat("Exposure", exposure, 0.5f, 3f);
                    }
                }

                ImGui.end();
            }
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





            if (getHeight() != gbuffer.getHeight() && getWidth() != gbuffer.getWidth()) {
                gbuffer.resize(getWidth(), getHeight());
            }
            sunEntity.setPosition(light.getPosition());

            float sunHeight = (float)Math.sin(light.getAngle());

            Vector3f nightColor = new Vector3f(0.02f, 0.03f, 0.05f);
            Vector3f horizonDayColor = new Vector3f(0.8f, 0.85f, 0.95f);
            Vector3f zenithDayColor = new Vector3f(0.35f, 0.55f, 0.75f);

            Vector3f sunsetColor = new Vector3f(1.0f, 0.72f, 0.55f);

            float t = Math.max(0, sunHeight);

            Vector3f skyDay = horizonDayColor.mul(1 - t).add(zenithDayColor.mul(t));

            float sunsetStart = 0.25f;
            float sunsetFactor = 0f;
            if (t < sunsetStart) {
                sunsetFactor = (sunsetStart - t) / sunsetStart;
                sunsetFactor = (float)Math.pow(sunsetFactor, 1.2f);
            }
            skyDay = skyDay.mul(1 - sunsetFactor).add(sunsetColor.mul(sunsetFactor));

            this.skyColor = nightColor.mul(1 - t).add(skyDay.mul(t));

            skyColor.x = Math.min(skyColor.x, 1.0f);
            skyColor.y = Math.min(skyColor.y, 1.0f);
            skyColor.z = Math.min(skyColor.z, 1.0f);

            renderer.init(skyColor.x, skyColor.y, skyColor.z, 0);




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


            if (inDebugMode) {
                ImGui.render();
                imGuiImplGl3.renderDrawData(ImGui.getDrawData());
                float[] xScale = new float[1];
                float[] yScale = new float[1];
                glfwGetWindowContentScale(window, xScale, yScale);

                ImGui.getIO().setFontGlobalScale(xScale[0]);
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
            }

            renderer.setVolumetricAlbedo(fogAlbedo[0]);
            renderer.setVolumetricFogDensity(fogDensity[0]);
            renderer.setExposure(exposure[0]);
            renderer.setVolumetricStepSize(stepSize[0]);

            player.move(terrainsArray[(int) (Math.ceil(player.getPosition().x / Terrain.SIZE)) + 1][(int) (Math.ceil(player.getPosition().z / Terrain.SIZE)) + 1]);
            light.updatePosition();
            glfwSwapBuffers(window); // swap the color buffers
            frameTime = GLFW.glfwGetTime() - lastTime;
            lastTime = GLFW.glfwGetTime();
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwMakeContextCurrent(window);
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
        imGuiImplGl3.shutdown();
        imGuiImplGlfw.shutdown();
        GLFW.glfwMakeContextCurrent(NULL);
        GL.setCapabilities(null);
        ImGui.destroyContext();
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
    public ImGuiImplGlfw getImGuiImplGlfw() {
        return this.imGuiImplGlfw;
    }

    public Vector3f getSkyColor() {
        return this.skyColor;
    }


}
