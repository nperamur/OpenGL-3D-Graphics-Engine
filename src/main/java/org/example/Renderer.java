package org.example;

import org.example.bloom.Bloom;
import org.example.bloom.CombineTextures;
import org.example.blur.*;
import org.example.shadow.*;
import org.example.terrain.Terrain;
import org.example.terrain.TerrainShader;
import org.example.tonemapping.ToneMapping;
import org.example.vignette.Vignette;
import org.example.volumetrics.VolumetricLighting;
import org.example.water.WaterShader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;

public class Renderer {
    private int fov = 60;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 8000;
    public static final float WATER_Y = 0f;
    private Matrix4f projectionMatrix;
    private ArrayList<Entity> entities = new ArrayList<>();
    private TestShader shader;
    private ArrayList<Terrain> terrains;
    private TerrainShader terrainShader;
    private WaterShader waterShader;
    private Sunlight light;
    private TexturedModel waterModel;
    private FrameBuffers fbos;
    private int waterDudvTexture;
    private static final float WAVE_SPEED = 0.02f;
    private float moveFactor = 0;
    private int waterNormalMap;
    private Item heldItem;
    private SsaoShader ssaoShader;
    private GaussianBlur gaussianBlur;
    private Bloom bloom;
    private CombineTextures combineTextures;
    private Gbuffer gbuffer;
    private int noiseTexture;
    private ShadowRenderer shadowRenderer;
    private LightingPassShader lightingPassShader;
    private VolumetricLighting volumetricLighting;
    private ToneMapping toneMapping;
    private Vignette vignette;
    private float volumetricStepSize;

    private Fbo lightFbo;
    private final ArrayList<Entity> shadowedEntities = new ArrayList<>();


    private float volumetricFogDensity;
    private float volumetricAlbedo;
    

    public Renderer(TestShader shader, TerrainShader terrainShader, WaterShader waterShader, SsaoShader ssaoShader, ArrayList<Terrain> terrains, Sunlight light, TexturedModel waterModel, FrameBuffers fbos, int waterDudvTexture, int waterNormalMap, Gbuffer gbuffer) {
        this.terrainShader = terrainShader;
        this.shader = shader;
        this.terrains = terrains;
        this.light = light;
        this.gbuffer = gbuffer;
        this.waterShader = waterShader;
        this.waterModel = waterModel;
        this.fbos = fbos;
        this.lightFbo = new Fbo(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight(), Fbo.NONE);
        this.waterDudvTexture = waterDudvTexture;
        this.combineTextures = new CombineTextures(false);
        this.waterNormalMap = waterNormalMap;
        this.gaussianBlur = new GaussianBlur(3);
        this.bloom = new Bloom();
        this.lightingPassShader = new LightingPassShader();
        this.volumetricStepSize = 1.45f;


        this.vignette = new Vignette();
        this.toneMapping = new ToneMapping(1.8f);

        noiseTexture = generateNoiseTexture();
        glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
        glEnable(GL11.GL_DEPTH_TEST); // Enable depth testing
        GL11.glDepthFunc(GL_LESS);
        GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
        //GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        this.ssaoShader = ssaoShader;
        shader.start();
        createProjectionMatrix();
        shader.connectTextureUnits();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
        terrainShader.start();
        terrainShader.loadProjectionMatrix(projectionMatrix);
        terrainShader.connectTextureUnits();
        terrainShader.stop();
        waterShader.start();
        waterShader.connectTextureUnits();
        waterShader.loadProjectionMatrix(projectionMatrix);
        waterShader.stop();

        ssaoShader.start();
        ssaoShader.connectTextureUnits();
        ssaoShader.loadSamplingKernels(generateRandomSampleKernels());
        ssaoShader.loadProjectionMatrix(projectionMatrix);
        ssaoShader.stop();

        lightingPassShader.start();
        lightingPassShader.connectTextureUnits();
        lightingPassShader.stop();




        GLFW.glfwSetWindowSizeCallback(Main.getDisplayManager().getWindow(), (long window, int width, int height) -> {
            glViewport(0, 0, width, height);
            createProjectionMatrix();
            fbos.initialiseSceneFrameBuffer();

        });
        this.volumetricFogDensity = 0.003f;
        this.volumetricAlbedo = 0.049f;
    }

//    public void initShadows() {
//        this.shadowRenderer = new ShadowRenderer(Main.getDisplayManager().getPlayer(), (Sunlight) light);
//    }

    public void initShadowsAndVolumetricLighting() {
        this.shadowRenderer = new ShadowRenderer(Main.getDisplayManager().getPlayer(), light);
        this.volumetricLighting = new VolumetricLighting(light, gbuffer, shadowRenderer);

    }


    public void init(float red, float green, float blue, float alpha) {
        GL11.glClearColor(red, green, blue, alpha);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    public void render(Player player, Vector4f clipPlane) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        if (player.isSprinting()) {
            fov = 70;
            createProjectionMatrix();
        } else if (fov != 60) {
            fov = 60;
            createProjectionMatrix();
        }
        shader.start();
        shader.loadViewMatrix(player);
        shader.loadClipPlane(clipPlane);
        shader.loadProjectionMatrix(projectionMatrix);
//        heldItem.updatePosition();
        if (heldItem != null) {
            heldItem.render(shader);
        }
        shader.loadClipPlane(new Vector4f(0, 10000, 0f, 0));
        shader.loadLight(light);
        for (Entity entity : entities) {
            entity.render(shader);
        }
        shader.stop();


        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);  // Standard alpha blending
        terrainShader.start();
        terrainShader.loadProjectionMatrix(projectionMatrix);
        terrainShader.loadClipPlane(clipPlane);
        terrainShader.loadViewMatrix(player);
        for (Terrain terrain : terrains) {
            renderTerrain(terrain);
        }
        terrainShader.stop();

        waterShader.start();
        waterShader.loadProjectionMatrix(projectionMatrix);
        waterShader.loadViewMatrix(player);
        moveFactor += (float) (WAVE_SPEED * Main.getDisplayManager().getFrameTimeInSeconds());
        moveFactor %= 1;
        waterShader.loadMoveFactor(moveFactor);
        renderWater();
        waterShader.stop();
        GL30.glBindVertexArray(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE5);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);


        shadowRenderer.bindFrameBuffer();
        shadowedEntities.clear();
        shadowedEntities.addAll(entities);
        if (heldItem != null) {
            shadowedEntities.add(heldItem);
        }
        shadowRenderer.render(shadowedEntities);
        shadowRenderer.unbindFrameBuffer();
    }

    private void renderTerrain(Terrain terrain) {

        Matrix4f transformationMatrix = GameMath.createTransformationMatrix(new Vector3f(terrain.getX(), -1, terrain.getZ()), 0, 0,0, 1);
        terrainShader.loadTransformationMatrix(transformationMatrix);
        terrainShader.loadLight(light);
        terrainShader.loadShineVariables(terrain.getTexturePack().getShineDamper(), terrain.getTexturePack().getReflectivity());
        Model model = terrain.getModel();
        bindTerrainTextures(terrain);
        renderModel(model);
    }

    private void renderWater() {
        Matrix4f transformationMatrix = GameMath.createTransformationMatrix(new Vector3f(-10000, WATER_Y, -10000), 0, 0,0, 20000);
        waterShader.loadTransformationMatrix(transformationMatrix);
        waterShader.loadLight(light);
        bindWaterTextures();
        renderModel(waterModel.getRawModel());

    }

    private void bindTerrainTextures(Terrain terrain) {
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTexturePack().getBackgroundTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTexturePack().getrTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTexturePack().getgTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTexturePack().getbTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE5);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());
    }

    private void bindWaterTextures() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, waterDudvTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, waterNormalMap);
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
    }


    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);;
    }




    private void createProjectionMatrix() {
        float aspectRatio = (float) Main.getDisplayManager().getWidth() / Main.getDisplayManager().getHeight();
        float yScale = (float) ((1f / Math.tan(Math.toRadians(fov / 2f))));
        float xScale = yScale / aspectRatio;
        float frustumLength = FAR_PLANE - NEAR_PLANE;
        projectionMatrix = new Matrix4f();
        projectionMatrix.m00(xScale);
        projectionMatrix.m11(yScale);
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustumLength));
        projectionMatrix.m23(-1);
        projectionMatrix.m32(-(2 * NEAR_PLANE * FAR_PLANE) / frustumLength);
        projectionMatrix.m33(0);
    }

    public void setHeldItem(Item item) {
        this.heldItem = item;
    }

    public void doPostProcessing(Model fullScreenQuad) {
//        combineTextures.bindFrameBuffer();
        if (lightFbo.getWidth() != Main.getDisplayManager().getWidth() && lightFbo.getHeight() != Main.getDisplayManager().getHeight()) {
            lightFbo.resize(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        }
        gbuffer.downSampleAll();
        lightFbo.bindFrameBuffer();
//        bloom.bindFrameBuffer();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        lightingPassShader.start();
        lightingPassShader.loadLight(light);
        lightingPassShader.loadViewMatrix(GameMath.createViewMatrix(Main.getDisplayManager().getPlayer()));
        lightingPassShader.loadToShadowMapSpace(shadowRenderer.getToShadowMapSpaceMatrix());
        lightingPassShader.loadInversePlayerViewMatrix(GameMath.createViewMatrix(Main.getDisplayManager().getPlayer()).invert());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, gbuffer.getTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL_TEXTURE_2D, gbuffer.getPositionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL_TEXTURE_2D, shadowRenderer.getShadowMap());
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL_TEXTURE_2D, gbuffer.getNormalTexture());


        renderModel(fullScreenQuad);
        lightingPassShader.stop();
        lightFbo.unbindCurrentFrameBuffer();



        gaussianBlur.bindFrameBuffer();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        ssaoShader.start();
        ssaoShader.loadProjectionMatrix(projectionMatrix);
        ssaoShader.loadScreenDimensions(Main.getDisplayManager().getWidth(), Main.getDisplayManager().getHeight());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, gbuffer.getNormalTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, gbuffer.getPositionTexture());
        renderModel(fullScreenQuad);
        ssaoShader.stop();

        VerticalBlur vBlur = gaussianBlur.getVBlur();
        HorizontalBlur hBlur = gaussianBlur.getHBlur();
        vBlur.bindFrameBuffer();
        hBlur.render(fullScreenQuad);
        vBlur.unbindFrameBuffer();
        combineTextures.bindFrameBuffer();
        vBlur.render(fullScreenQuad);
        combineTextures.unbindFrameBuffer();
        combineTextures.setSecondTexture(lightFbo.getTexture());

        bloom.bindFrameBuffer();
        combineTextures.render(fullScreenQuad);
        bloom.unbindFrameBuffer();

        bloom.prepareRender(fullScreenQuad);


        volumetricLighting.bindFrameBuffer();
        bloom.render(fullScreenQuad);
        volumetricLighting.unbindFrameBuffer();


        vignette.bindFrameBuffer();
        if (Main.getDisplayManager().getPlayer().getPosition().y <= -1) {
            volumetricLighting.setVolumetricParams(new Vector3f(0.3f, 0.5f, 1.0f).mul(2).mul(light.getColor()), volumetricStepSize, 0f, volumetricFogDensity,  volumetricAlbedo);
        } else {
            volumetricLighting.setVolumetricParams(light.getColor(), volumetricStepSize, light.getFogAnisotropy(), volumetricFogDensity,  volumetricAlbedo);
        }
        volumetricLighting.render(fullScreenQuad);

        vignette.unbindFrameBuffer();

        toneMapping.bindFrameBuffer();
        vignette.render(fullScreenQuad);
        toneMapping.unbindFrameBuffer();

        toneMapping.render(fullScreenQuad);




    }

    public static void renderModel(Model model) {
        GL30.glBindVertexArray(model.getId());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0L);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    private static Vector3f[] generateRandomSampleKernels() {
        Vector3f[] vectors = new Vector3f[16];
        for (int i = 0; i < 16; i++) {
            Vector3f vector = new Vector3f((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1), (float) Math.random()).normalize();
            float scale = (float) i / 16;
            vector.mul(org.joml.Math.lerp(0.1f, 1.0f, scale * scale));
            vectors[i] = vector;
        }
        return vectors;
    }

    private static int generateNoiseTexture() {
        Random random = new Random();

        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(48);
        for (int i = 0; i < 16; i++) {
            float x = random.nextFloat() * 2.0f - 1.0f;
            float y = random.nextFloat() * 2.0f - 1.0f;
            float z = 0.0f;
            floatBuffer.put(x);
            floatBuffer.put(y);
            floatBuffer.put(z);

        }
        floatBuffer.flip();

        int noiseTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, noiseTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_RGB16F, 4, 4, 0, GL_RGB, GL_FLOAT, floatBuffer);
        return noiseTexture;
    }

    public int getFov() {
        return this.fov;
    }


    public void cleanUp() {
        gaussianBlur.cleanUp();
        combineTextures.cleanUp();
        bloom.cleanUp();
        ssaoShader.cleanUp();
        shader.cleanUp();
        terrainShader.cleanUp();
        waterShader.cleanUp();
        fbos.cleanUp();
        gbuffer.cleanUp();
        shadowRenderer.cleanUp();
        lightingPassShader.cleanUp();
        lightFbo.cleanUp();
        volumetricLighting.cleanUp();
        vignette.cleanUp();
        glDeleteTextures(noiseTexture);
    }

    public void setVolumetricFogDensity(float density) {
        this.volumetricFogDensity = density;
    }

    public void setVolumetricAlbedo(float albedo) {
        this.volumetricAlbedo = albedo;
    }

    public void setExposure(float exposure) {
        toneMapping.setExposure(exposure);
    }

    public void setVolumetricStepSize(float stepSize) {
        this.volumetricStepSize = stepSize;
    }




}
