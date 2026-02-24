#version 430 core
layout(local_size_x = 16, local_size_y = 16, local_size_z = 1) in;
uniform vec3 lightPosition;
uniform int trianglesLength;
uniform int triangleBVHLength;
uniform int modelBVHLength;
uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform mat4 inverseViewMatrix;

layout(rgba16f, binding = 4) uniform image2D outputImage;

layout(std430, binding = 1) buffer Triangles {
    float triangleData[];
};

layout(std430, binding = 2) buffer TriangleBVH {
    float triangleBVHData[];
};

layout(std430, binding = 3) buffer ModelBVH {
    float modelBVHData[];
};

bool hitsTriangle(vec3 p1, vec3 p2, vec3 p3, vec3 rayDir, vec3 rayOrigin) {
    vec3 e1 = p2 - p1;
    vec3 e2 = p3 - p1;
    vec3 h = cross(rayDir, e2);
    float a = dot(e1, h);
    if (abs(a) < 0.0001) return false;
    float f = 1.0 / a;
    vec3 s = rayOrigin - p1;
    float u = f * dot(s, h);
    if (u < 0.0 || u > 1.0) return false;
    vec3 q = cross(s, e1);
    float v = f * dot(rayDir, q);
    if (v < 0.0 || u + v > 1.0) return false;
    float t = f * dot(e2, q);
    return t > 0.01;
}

//Step 1: raycast towards aabb min and max in direction to perpendicular plane to ray dir.
//only accept t values above zero and that are not infinity or Nan or something
//Step 2: Reject when tMax < tMin on any of the axis (remember to flip max and min depending on raydir)
//Step 3:If it hits traverse bvh and move onto next node keep repeating this process until you get to the raw triangles.
//Step 4:Test against each individual triangle. To do this, hit up the triangle plane with a raycast and then use barcyentric or the other method to determine if its between the 3 points
//Step 5:If we hit one of those triangles, then darken the shadowmap otherwise let it be white...

bool raytrace(vec3 rayDir, vec3 shadowOrigin) {
  int bufferIndex = 0;
  int modelBVHCurrIndex = 0;
  int triangleBVHCurrIndex = 0;
  int triangleCurrIndex = 0;
  bool hasHit = false;
  int numTestTriangles = 0;
  int numTestModels = 0;
  int stopIndex = 0;
  int modelBVHSkipPointer;
  int triangleBVHSkipPointer;
  vec3 invDir = 1.0 / rayDir;
  while (modelBVHCurrIndex >= 0 && modelBVHCurrIndex < modelBVHLength) {
    //process node
    if (bufferIndex == 0) {
      vec3 min = vec3(modelBVHData[modelBVHCurrIndex], modelBVHData[modelBVHCurrIndex + 1], modelBVHData[modelBVHCurrIndex + 2]);
      vec3 max = vec3(modelBVHData[modelBVHCurrIndex + 3], modelBVHData[modelBVHCurrIndex + 4], modelBVHData[modelBVHCurrIndex + 5]);

      vec3 t1 = (min - shadowOrigin) * invDir;
      vec3 t2 = (max - shadowOrigin) * invDir;
      vec3 tMin = vec3(
          invDir.x < 0.0 ? t2.x : t1.x,
          invDir.y < 0.0 ? t2.y : t1.y,
          invDir.z < 0.0 ? t2.z : t1.z
      );
      vec3 tMax = vec3(
          invDir.x < 0.0 ? t1.x : t2.x,
          invDir.y < 0.0 ? t1.y : t2.y,
          invDir.z < 0.0 ? t1.z : t2.z
      );

      float tNear = max(max(tMin.x, tMin.y), tMin.z);
      float tFar = min(min(tMax.x, tMax.y), tMax.z);
      bool isLeaf = modelBVHData[modelBVHCurrIndex + 6] > 0.5 ? true : false;

      if (tNear > tFar || tFar < 0.01 || any(isnan(tMin)) || any(isnan(tMax))) {
         int nextIdx = int(modelBVHData[modelBVHCurrIndex + 7] + 0.5) * 10;
         modelBVHCurrIndex = nextIdx;
         continue;
      }
      if (isLeaf && int(modelBVHData[modelBVHCurrIndex + 8]) > 0) {
        bufferIndex++;
        triangleBVHCurrIndex = int(modelBVHData[modelBVHCurrIndex + 9] + 0.5) * 10;
        numTestModels = int(modelBVHData[modelBVHCurrIndex + 8] + 0.5);
        stopIndex = triangleBVHCurrIndex + (numTestModels * 10);
        modelBVHSkipPointer = int(modelBVHData[modelBVHCurrIndex + 7] + 0.5) * 10;
        continue;
      } else if (isLeaf) {
         int nextIdx = int(modelBVHData[modelBVHCurrIndex + 7] + 0.5) * 10;
         modelBVHCurrIndex = nextIdx;
         continue;
      }
      modelBVHCurrIndex += 10;

    } else if (bufferIndex == 1) {
      if (triangleBVHCurrIndex < 0 || triangleBVHCurrIndex >= stopIndex || triangleBVHCurrIndex >= triangleBVHLength) {
          bufferIndex = 0;
          modelBVHCurrIndex = modelBVHSkipPointer;
          continue;
      }

      vec3 min = vec3(triangleBVHData[triangleBVHCurrIndex], triangleBVHData[triangleBVHCurrIndex + 1], triangleBVHData[triangleBVHCurrIndex + 2]);
      vec3 max = vec3(triangleBVHData[triangleBVHCurrIndex + 3], triangleBVHData[triangleBVHCurrIndex + 4], triangleBVHData[triangleBVHCurrIndex + 5]);

      vec3 t1 = (min - shadowOrigin) * invDir;
      vec3 t2 = (max - shadowOrigin) * invDir;
      vec3 tMin = vec3(
          invDir.x < 0.0 ? t2.x : t1.x,
          invDir.y < 0.0 ? t2.y : t1.y,
          invDir.z < 0.0 ? t2.z : t1.z
      );
      vec3 tMax = vec3(
          invDir.x < 0.0 ? t1.x : t2.x,
          invDir.y < 0.0 ? t1.y : t2.y,
          invDir.z < 0.0 ? t1.z : t2.z
      );
      float tNear = max(max(tMin.x, tMin.y), tMin.z);
      float tFar = min(min(tMax.x, tMax.y), tMax.z);
      bool isLeaf = triangleBVHData[triangleBVHCurrIndex + 6] > 0.5 ? true : false;

      if (tNear > tFar || tFar < 0.01 || any(isnan(tMin)) || any(isnan(tMax))) {
         int nextIdx = int(triangleBVHData[triangleBVHCurrIndex + 7] + 0.5) * 10;
         triangleBVHCurrIndex = nextIdx;
         continue;
      }

      if (isLeaf && int(triangleBVHData[triangleBVHCurrIndex + 8]) > -0.01) {
        bufferIndex++;
        numTestTriangles = int(triangleBVHData[triangleBVHCurrIndex + 8] + 0.5);
        triangleCurrIndex = int(triangleBVHData[triangleBVHCurrIndex + 9] + 0.5) * 9;
        triangleBVHSkipPointer = int(triangleBVHData[triangleBVHCurrIndex + 7] + 0.5) * 10;
        continue;
      } else if (isLeaf) {
        int nextIdx = int(modelBVHData[modelBVHCurrIndex + 7] + 0.5) * 10;
        modelBVHCurrIndex = nextIdx;
        continue;
      }
      triangleBVHCurrIndex += 10;
    } else if (bufferIndex == 2) {
      if (numTestTriangles <= 0 || triangleCurrIndex >= trianglesLength) {
        bufferIndex = 1;
        triangleBVHCurrIndex = triangleBVHSkipPointer;
        continue;
      }

      vec3 p1 = vec3(triangleData[triangleCurrIndex], triangleData[triangleCurrIndex + 1], triangleData[triangleCurrIndex + 2]);
      vec3 p2 = vec3(triangleData[triangleCurrIndex + 3], triangleData[triangleCurrIndex + 4], triangleData[triangleCurrIndex + 5]);
      vec3 p3 = vec3(triangleData[triangleCurrIndex + 6], triangleData[triangleCurrIndex + 7], triangleData[triangleCurrIndex + 8]);

      bool hitTriangle = hitsTriangle(p1, p2, p3, rayDir, shadowOrigin);

      if (hitTriangle) {
        hasHit = true;
        //imageStore(outputImage, pixel, vec4(1.0, 0.0, 0.0, 1.0));
        //return;
        break;
      }

      triangleCurrIndex += 9;
      numTestTriangles--;
    }
  }
  return hasHit;
}


uint hash(uint x)
{
    x ^= x >> 16;
    x *= 0x7feb352du;
    x ^= x >> 15;
    x *= 0x846ca68bu;
    x ^= x >> 16;
    return x;
}

float hashToFloat(uint x)
{
    return float(hash(x)) / 4294967296.0;
}


void main(void) {


  ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
  ivec2 size = imageSize(outputImage);
  vec2 uv = vec2(pixel) / vec2(size);

  vec4 viewPos = textureLod(gPosition, uv, 0.0);
  if (viewPos.w == 0.0) {
      imageStore(outputImage, pixel, vec4(0.0));
      return;
  }
  vec3 worldPos = (inverseViewMatrix * viewPos).xyz;
  vec3 rayDir = normalize(vec3(lightPosition.x, max(lightPosition.y, 5000.0), lightPosition.z));

  rayDir.x = (abs(rayDir.x) < 0.2) ? sign(rayDir.x) * 0.2 : rayDir.x;
  rayDir.z = (abs(rayDir.z) < 0.2) ? sign(rayDir.z) * 0.2 : rayDir.z;


  vec3 shadowOrigin = worldPos + (rayDir * 0.1);



  uint seed = uint(pixel.x)
            + uint(pixel.y) * uint(size.x)
            + 16777619u;

  float rand = hashToFloat(seed);
  bool hasHit = raytrace(rayDir, shadowOrigin);



  if (hasHit) {
    imageStore(outputImage, pixel, vec4(vec3(0.0), 1.0));
  } else {
    imageStore(outputImage, pixel, vec4(1));
  }




}