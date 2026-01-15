package org.example.noise;

import java.util.Random;

public class WorleyNoise3D {
    private Random random;

    public WorleyNoise3D(int seed) {
        this.random = new Random(seed);
    }

    public double noise(double x, double y, double z) {
        int xi = (int)Math.floor(x);
        int yi = (int)Math.floor(y);
        int zi = (int)Math.floor(z);

        double minDist = Double.MAX_VALUE;

        // Check 3x3x3 grid of cells
        for (int xo = -1; xo <= 1; xo++) {
            for (int yo = -1; yo <= 1; yo++) {
                for (int zo = -1; zo <= 1; zo++) {
                    // Get feature point for this cell
                    random.setSeed(hash(xi + xo, yi + yo, zi + zo));
                    double px = xi + xo + random.nextDouble();
                    double py = yi + yo + random.nextDouble();
                    double pz = zi + zo + random.nextDouble();

                    // Distance to feature point
                    double dx = x - px;
                    double dy = y - py;
                    double dz = z - pz;
                    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

                    minDist = Math.min(minDist, dist);
                }
            }
        }

        return 1.0 - Math.min(minDist, 1.0); // Invert so cells are high
    }

    private long hash(int x, int y, int z) {
        return ((long)x * 73856093) ^ ((long)y * 19349663) ^ ((long)z * 83492791);
    }
}