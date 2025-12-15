package org.example;

import org.joml.Vector3f;

import static java.lang.Math.clamp;

public class Sunlight extends Light {
    private int distance;
    private double angle = Math.PI / 3;

    private Model sunModel;

    private float fogDensity;
    private float fogAnisotropy;


    public Sunlight(int distance, Vector3f color, Loader loader) {
        super(new Vector3f(0, distance, 0), color);
        this.distance = distance;
        this.sunModel = createSunModel(loader);


        this.setColor(new Vector3f(1.0f, 0.85f, 0.6f));
    }

    public void updatePosition() {
        float x = (float) Math.cos(angle) * distance;
        float y = (float) Math.sin(angle) * distance;
        super.setPosition(new Vector3f(x, y, 0));
        angle += Main.getDisplayManager().getFrameTimeInSeconds() / 20;

        Vector3f dayColor    = new Vector3f(1.0f, 1f, 1f);
        Vector3f sunsetColor = new Vector3f(1.0f, 0.72f, 0.55f);
        Vector3f nightColor  = new Vector3f(0.5f, 0.75f, 1f);
        float e = (float) Math.sin(angle);

// day starts earlier (-0.1) and lasts longer (range 1.1 instead of 0.9)
        float dayFactor = clamp((e + 0.1f) / 1.1f, 0f, 1f);

// sunset shorter duration (0.15 instead of 0.3), shifted a bit earlier (-0.55)
        float sunsetFactor = clamp((e + 0.55f) / 0.15f, 0f, 1f);

        Vector3f color =
                nightColor.mul(1 - sunsetFactor)
                        .add(sunsetColor.mul(sunsetFactor * (1 - dayFactor)))
                        .add(dayColor.mul(dayFactor));

        super.setColor(color);





        float nightFogDensity = 0.1f;


        float dayFogDensity = 0.05f;


        float sunsetFogDensity = 0.3f;


        fogDensity = nightFogDensity * (1 - sunsetFactor)
                + sunsetFogDensity * (sunsetFactor * (1 - dayFactor))
                + dayFogDensity * dayFactor;




        float nightFogAnisotropy = 0f;
        float dayFogAnisotropy = 0.45f;
        float sunsetFogAnisotropy = 0.15f;

        fogAnisotropy = nightFogAnisotropy * (1 - sunsetFactor)
                + sunsetFogAnisotropy * (sunsetFactor * (1 - dayFactor))
                + dayFogAnisotropy * dayFactor;

    }

    public double getAngle() {
        return angle;
    }

    public Model getSunModel() {
        return sunModel;
    }


    private Model createSunModel(Loader loader) {
        int latBands = 30;  // Number of latitude bands (rows)
        int lonBands = 30;  // Number of longitude bands (columns)
        float radius = 1.0f; // Radius of the sphere

        int vertexCount = (latBands + 1) * (lonBands + 1);  // Total number of vertices
        int indexCount = latBands * lonBands * 6;           // Total number of indices (2 triangles per square face)

        float[] vertices = new float[vertexCount * 3];   // Vertex positions (x, y, z)
        float[] texCoords = new float[vertexCount * 2];  // Texture coordinates (u, v)
        int[] indices = new int[indexCount];              // Indices for triangles

        float[] normals = new float[vertexCount * 3];  // Add normals

        int vi = 0;  // Vertex index
        int ti = 0;  // Texture coordinate index
        int ii = 0;  // Indices index
        int ni = 0;

        // Generate vertices and texture coordinates
        for (int lat = 0; lat <= latBands; lat++) {
            double theta = lat * Math.PI / latBands;  // Latitude angle (from top to bottom)
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            for (int lon = 0; lon <= lonBands; lon++) {
                double phi = lon * 2 * Math.PI / lonBands;  // Longitude angle (around the sphere)
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);

                // Calculate the vertex position using spherical coordinates
                float x = (float) (cosPhi * sinTheta);
                float y = (float) cosTheta;
                float z = (float) (sinPhi * sinTheta);

                // Map the texture coordinates (U and V)
                float u = (float) lon / lonBands;
                float v = (float) lat / latBands;

                // Store the vertex positions
                vertices[vi++] = x * radius;
                vertices[vi++] = y * radius;
                vertices[vi++] = z * radius;

                // Store the texture coordinates (UV)
                texCoords[ti++] = u;
                texCoords[ti++] = v;

                normals[ni++] = -x;
                normals[ni++] = -y;
                normals[ni++] = -z;
            }
        }

        // Generate the indices for the triangles
        for (int lat = 0; lat < latBands; lat++) {
            for (int lon = 0; lon < lonBands; lon++) {
                int first = (lat * (lonBands + 1)) + lon;           // First vertex of the current quad
                int second = first + lonBands + 1;                  // Second vertex of the current quad

                // First triangle (correct winding order)
                indices[ii++] = first;
                indices[ii++] = second + 1; // Flip order here for correct triangle orientation
                indices[ii++] = second;

                // Second triangle
                indices[ii++] = first;
                indices[ii++] = first + 1;
                indices[ii++] = second + 1; // Flip order here as well for correct triangle orientation


            }
        }


        return loader.loadToVao(vertices, texCoords, indices, normals);
    }


    public float getFogAnisotropy() {
        return this.fogAnisotropy;
    }

    public float getFogDensity() {
        return this.fogDensity;
    }
}
