package org.example.terrain;

import org.example.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;

public class Terrain {
    public static final int SIZE = 800;
    private static final int VERTEX_COUNT = 128;
    private static final float MAX_HEIGHT = 80;
    private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;
    private float x;
    private float z;
    private Model model;
    private float[][] heights;
    private TerrainTexturePack texturePack;
    private ModelTexture blendMap;
    private HeightsGenerator heightsGenerator;
    private static int seed = new Random().nextInt();
    private Loader loader;


    public Terrain(int gridX, int gridZ, Loader loader, String heightMap, TerrainTexturePack textures, ModelTexture blendMap) {
        this.heightsGenerator = new HeightsGenerator(gridX, gridZ, VERTEX_COUNT, seed);
        this.x = gridX * SIZE;
        this.z = gridZ * SIZE;
        this.texturePack = textures;
        this.blendMap = blendMap;
        this.loader = loader;
        this.model = generateTerrain(loader, heightMap);

    }

    private Model generateTerrain(Loader loader, String heightMap){
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("src/main/resources/" + heightMap + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int count = VERTEX_COUNT * VERTEX_COUNT;
        float[] vertices = new float[count * 3];
        int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count*2];
        int vertexPointer = 0;
        heights = new float[VERTEX_COUNT][VERTEX_COUNT];
        for(int i=0;i<VERTEX_COUNT;i++){
            for(int j=0;j<VERTEX_COUNT;j++){
                vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * SIZE;
                float height = getHeight(i, j);
                heights[j][i] = height;
                textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
                textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
                Vector3f normal = calculateNormal(i, j);
                normals[vertexPointer*3] = normal.x;
                normals[vertexPointer*3+1] = normal.y;
                normals[vertexPointer*3+2] = normal.z;
                vertices[vertexPointer*3+1] = height;
                vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * SIZE;
                vertexPointer++;
            }
        }
        int pointer = 0;
        for(int gz=0;gz<VERTEX_COUNT-1;gz++){
            for(int gx=0;gx<VERTEX_COUNT-1;gx++){
                int topLeft = (gz*VERTEX_COUNT)+gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return loader.loadToVao(vertices, textureCoords, indices, normals);
    }




    private float getHeight(int x, int z) {
//        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) {
//            return 0;
//        }
//        float height = image.getRGB(x, z);
//        height += MAX_PIXEL_COLOUR/2f;
//        height /= MAX_PIXEL_COLOUR/2f;
//        height *= MAX_HEIGHT;
        float height = heightsGenerator.generateHeight(x, z);
        return height;
    }


    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public Model getModel() {
        return model;
    }

    public TerrainTexturePack getTexturePack() {
        return texturePack;
    }

    public ModelTexture getBlendMap() {
        return blendMap;
    }

    public float getHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;
        float gridSquareSize = SIZE / ((float) heights.length - 1);
        int gridX = (int) Math.floor(terrainX/gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ/gridSquareSize);
        if (gridX >= heights.length || gridZ >= heights.length || gridX < 0 || gridZ < 0) {
            return 0;
        }
        float xCoord = (terrainX - gridX * gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ - gridZ * gridSquareSize) / gridSquareSize;
        float answer;
        if (xCoord <= (1 - zCoord)) {
            answer = GameMath.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX][gridZ], 0), new Vector3f(0, heights[gridX][gridZ], 1), new Vector2f(xCoord, zCoord));
        } else {
            answer = GameMath.barryCentric(new Vector3f(1, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX][gridZ], 1), new Vector3f(0, heights[gridX][gridZ], 1), new Vector2f(xCoord, zCoord));
        }
        return answer;
    }

    private Vector3f calculateNormal(int x, int z) {
        float heightL = getHeight(x - 1, z);
        float heightR = getHeight(x + 1, z);
        float heightD = getHeight(x, z - 1);
        float heightU = getHeight(x, z + 1);
        Vector3f normal = new Vector3f(heightD - heightU, 2f, heightL - heightR);
        normal.normalize();
        return normal;
    }



   
}
