package org.example;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ModelLoader {
    public static Model load(String name, Loader loader) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("src/main/resources/" + name + ".obj"));
            ArrayList<Vector3f> vertices = new ArrayList<>();
            ArrayList<Vector2f> texCoords = new ArrayList<>();
            ArrayList<Vector3f> normals = new ArrayList<>();
            ArrayList<Vector2f> texCoords2 = null;
            ArrayList<Vector3f> normals2 = null;
            ArrayList<Vector3f> vertices2 = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            HashMap<Vector3i, Integer> indexHashes = new HashMap<>();
            int i = 0;

            try {
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line.startsWith("vt")) {
                        double[] arr = Arrays.stream(line.substring(3).split(" ")).mapToDouble(Double::parseDouble).toArray();
                        texCoords.add(new Vector2f((float) arr[0], 1f - (float) arr[1]));
                    } else if (line.startsWith("vn")) {
                        double[] arr = Arrays.stream(line.substring(3).split(" ")).mapToDouble(Double::parseDouble).toArray();
                        normals.add(new Vector3f((float) arr[0], (float) arr[1], (float) arr[2]));
                    } else if (line.startsWith("v")) {
                        double[] arr = Arrays.stream(line.substring(2).split(" ")).mapToDouble(Double::parseDouble).toArray();
                        vertices.add(new Vector3f((float) arr[0], (float) arr[1], (float) arr[2]));

                    } else if (line.startsWith("f")) {
                        if (normals2 == null) {
                            normals2 = new ArrayList<Vector3f>();
                        }
                        if (texCoords2 == null) {
                            texCoords2 = new ArrayList<Vector2f>();
                        }
                        Object[] strings = Arrays.stream(line.substring(2).split(" ")).toArray();
                        for (Object str : strings) {
                            String s = (String) str;
                            int[] nums = Arrays.stream(s.split("/")).mapToInt(Integer::parseInt).toArray();

                            Vector3i v = new Vector3i(nums[0] - 1, nums[1] - 1, nums[2] - 1);
                            if (!indexHashes.containsKey(v)) {
                                //add to vertices, normals, and texcoords new arraylists
                                indexHashes.put(v, indexHashes.size());
                                vertices2.add(vertices.get(v.x));
                                texCoords2.add(texCoords.get(v.y));
                                normals2.add(normals.get(v.z));
                            }
                            //hash v to get the next index to put in indices

                            indices.add(indexHashes.get(v));

                        }
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Successfully loaded model: " + name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }



            return loader.loadToVao(convertToFloatArray(vertices2), convertTexturesToFloatArray(texCoords2), convertToIntArray(indices), convertToFloatArray(normals2));
            //return loader.loadToVaoWithoutTexture(convertToFloatArray(vertices), convertToIntArray(indices));

        } catch (FileNotFoundException e) {
            System.out.println("Cannot find file for model: " + name + ".obj");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.println("Could not close file!");
                }
            }
        }
        return null;
    }

    private static float[] convertToFloatArray(ArrayList<Vector3f> arr) {
        float[] newArr = new float[arr.size() * 3];
        for (int i = 0; i < arr.size(); i++) {
            newArr[i * 3] = arr.get(i).x;
            newArr[i * 3 + 1] = arr.get(i).y;
            newArr[i * 3 + 2] = arr.get(i).z;
        }
        return newArr;
    }

    private static float[] convertToFloatArray(Vector3f[] arr) {
        float[] newArr = new float[arr.length * 3];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                break;
            }
            newArr[i * 3] = arr[i].x;
            newArr[i * 3 + 1] = arr[i].y;
            newArr[i * 3 + 2] = arr[i].z;
        }
        return newArr;
    }

    private static float[] convertTexturesToFloatArray(ArrayList<Vector2f> arr) {
        float[] newArr = new float[arr.size() * 2];
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) == null) {
                break;
            }
            newArr[i * 2] = arr.get(i).x;
            newArr[i * 2 + 1] = arr.get(i).y;
        }
        return newArr;
    }

    private static int[] convertToIntArray(ArrayList<Integer> arr) {
        int[] newArr = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            newArr[i] = arr.get(i);
        }
        return newArr;
    }

}
