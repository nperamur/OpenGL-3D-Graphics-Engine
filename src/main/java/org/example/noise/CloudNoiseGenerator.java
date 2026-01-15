package org.example.noise;


public class CloudNoiseGenerator {

    private final WorleyNoise3D noise;
    private final int sizeX, sizeY, sizeZ;
    private final float[][][] noiseMap;

    public CloudNoiseGenerator(int sizeX, int sizeY, int sizeZ, int seed, float scale, int octaves, float persistence) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.noiseMap = new float[sizeX][sizeY][sizeZ];
        this.noise = new WorleyNoise3D(seed);

        generateNoiseMap(scale, octaves, persistence);
    }

    private void generateNoiseMap(float scale, int octaves, float persistence) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    float nx = x * scale;
                    float ny = y * scale;
                    float nz = z * scale;

                    float value = 0;
                    float amplitude = 1;
                    float frequency = 1;
                    float maxValue = 0;

                    for (int i = 0; i < octaves; i++) {
                        value += amplitude * (float) noise.noise(nx * frequency, ny * frequency, nz * frequency);
                        maxValue += amplitude;
                        amplitude *= persistence;
                        frequency *= 2;
                    }

                    noiseMap[x][y][z] = value / maxValue;
                }
            }
        }
    }

    public float sample(int x, int y, int z) {
        x = Math.min(Math.max(x, 0), sizeX - 1);
        y = Math.min(Math.max(y, 0), sizeY - 1);
        z = Math.min(Math.max(z, 0), sizeZ - 1);
        return noiseMap[x][y][z];
    }

    public float[][][] getNoiseMap() {
        return noiseMap;
    }
}

