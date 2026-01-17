#version 400 core
uniform sampler2D textureSampler;
uniform sampler2D gNormal;
uniform sampler2D gPosition;
uniform sampler2D shadowMap;
uniform sampler3D cloudNoiseTexture;
uniform vec3 lightPosition;
uniform mat4 inversePlayerViewMatrix;
uniform mat4 toShadowMapSpace;
uniform vec3 lightColor;
uniform mat4 toLightSpace;
uniform mat4 lightProjectionMatrix;
uniform float density;
uniform float anisotropy;
uniform float albedo;
uniform float stepSize;
uniform mat4 inverseProjectionMatrix;
uniform float moveFactor;
uniform vec3 skyColor;

uniform int randomNumber;

layout(location = 0) out vec4 out_color;
layout(location = 1) out vec4 out_transmittance;


const float totalDistance = 145;
const float lightStrength = 500;

const float cloudDist = 3000;
const float cloudY = 200;
const float cloudHeight = 400;
const float cloudStepSize = 40;


const float cloudFadeStart = 2000;
const float cloudFadeEnd = 3000;
const float numCloudShadowSamples = 3;
const float cloudAlbedo = 0.02;
float dayCloudStrength = 500.0;
float nightCloudStrength = 100.0;


in vec2 pass_textureCoords;

float phaseFunction(vec3 L, vec3 V, float g) {
    float cosTheta = dot(L, V);
    float numerator = 1.0f - g * g;
    float denominator = pow(1.0f + g * g - 2.0f * g * cosTheta, 1.5f);
    return (1.0f / (4.0f * 3.1415926535)) * (numerator / denominator);
}

//dithering

float rand(vec2 p) {
    const float PHI = 1.61803398875;
    const float G   = 0.61803398875;

    p = fract(p * vec2(G, PHI));
    p += dot(p, p + 31.416);
    return fract(p.x * p.y);
}



float getCloudDensity(vec3 noiseCoords, float distFromCamera) {
  float cloudDensity = texture(cloudNoiseTexture, noiseCoords).r;
  cloudDensity = pow(cloudDensity, 3);
  cloudDensity = smoothstep(0.1, 0.8, cloudDensity);
  float fadeFactor = 1.0 - smoothstep(cloudFadeStart, cloudFadeEnd, distFromCamera);
  cloudDensity *= fadeFactor;
  return cloudDensity;
}


void main(void) {
    vec2 fboSize = textureSize(gPosition, 0);

    vec2 r = vec2(
        rand(vec2(float(randomNumber * 100), 1.0)) - 0.5,
        rand(vec2(float(randomNumber * 100), 2.0)) - 0.5
    );

    vec2 jitter = r / fboSize;


    vec4 color = texture(textureSampler, pass_textureCoords);
    //if (color.a == 0) {
        //out_color = color;
        //return;
    //}
    vec4 viewPos = texture(gPosition, pass_textureCoords);

    vec3 cameraPos = (inversePlayerViewMatrix * vec4(0, 0, 0, 1)).xyz;



    ivec2 texSize = textureSize(gPosition, 0);
    vec2 screenSize = vec2(texSize);
    vec2 pixelCoords = pass_textureCoords * screenSize;

    //raycasted clouds
    if (viewPos.a == 0) {
        float clip_x = (pixelCoords.x / screenSize.x) * 2.0 - 1.0;
        float clip_y = (pixelCoords.y / screenSize.y) * 2.0 - 1.0;
        vec4 farPoint = vec4(clip_x, clip_y, 1.0, 1.0);


        vec4 cloudWorldSpace = inversePlayerViewMatrix * inverseProjectionMatrix * farPoint;
        vec3 cloudPos = cloudWorldSpace.xyz / cloudWorldSpace.w;

        vec3 cloudRayDir = normalize(cloudPos - cameraPos);
        vec3 ambient = max(skyColor, vec3(0.125, 0.187, 0.25));
        if (cloudRayDir.y > 0) {
            //using placeholder number for now
            vec3 cloudRay = (cloudY / cloudRayDir.y) * cloudRayDir;

            vec3 cloudPosition = vec3(cameraPos.x + cloudRay.x + jitter.x * cloudStepSize, cameraPos.y + cloudRay.y, cameraPos.z + cloudRay.z + jitter.y * cloudStepSize);

            if (cloudPosition.z < cloudDist && cloudPosition.z > -cloudDist && cloudPosition.x < cloudDist && cloudPosition.x > -cloudDist) {
              float offset = rand(floor(pixelCoords) + vec2(randomNumber)) * cloudStepSize * 3;
              float transmittance = 1;
              vec3 accumulation = vec3(0.0);

              float phi = phaseFunction(normalize(lightPosition), cloudRayDir, anisotropy);

              transmittance = 1;
              float sunHeight = normalize(lightPosition).y;

              float cloudLightStrength = mix(nightCloudStrength, dayCloudStrength, smoothstep(-1, -0.2, sunHeight));
              float distFromCamera = length(cloudRay);
              for (float t = offset; t < cloudHeight; t += cloudStepSize) {
                  vec3 currentPos = cloudPosition + cloudRayDir * t;

                  vec3 lightAmount = lightColor * cloudLightStrength;
                  vec3 noiseCoords = vec3(currentPos.x + moveFactor * 100, currentPos.y, currentPos.z + moveFactor * 100) * 0.0004;
                  float cloudDensity = getCloudDensity(noiseCoords, distFromCamera);
                  float lightTransmittance = 1;
                  vec3 lightRay = normalize(lightPosition) * ((cloudHeight - currentPos.y) / numCloudShadowSamples);
                  float offset = rand(floor(pixelCoords) + vec2(randomNumber)) * (1/cloudStepSize) * 0.5;
                  float lightAmbientTransmittance = 1;
                  float ambientStepSize = (cloudHeight - currentPos.y) / numCloudShadowSamples;
                  for (float i = offset; i <= numCloudShadowSamples; i++) {
                    vec3 lightShadowPos = currentPos + lightRay * i;
                    float lightShadowCloudDensity = getCloudDensity(vec3(lightShadowPos.x + moveFactor * 100, lightShadowPos.y, lightShadowPos.z + moveFactor * 100) * 0.0004, distFromCamera);
                    lightTransmittance *= exp(-lightShadowCloudDensity * length(lightRay));
                    vec3 lightAmbientPos = currentPos + vec3(0, ambientStepSize , 0) * i;
                    float lightAmbientCloudDensity = getCloudDensity(vec3(lightAmbientPos.x + moveFactor * 100, lightAmbientPos.y, lightAmbientPos.z + moveFactor * 100) * 0.0004, distFromCamera);

                    lightAmbientTransmittance *= exp(-lightAmbientCloudDensity * length(lightRay));

                  }
                  float powderTerm = 1.0 - exp(-cloudDensity * cloudStepSize * 2.0);
                  vec3 lightShading = vec3(lightTransmittance * powderTerm) + vec3(0.1);
                  vec3 lightContribution = cloudAlbedo * phi * lightAmount * lightShading;
                  vec3 ambientContribution = lightAmbientTransmittance * ambient;
                  accumulation += (lightContribution + ambientContribution) * cloudDensity * cloudStepSize * transmittance;
                  transmittance *= exp(-cloudDensity * cloudStepSize);
              }

              out_transmittance = vec4(transmittance);
              out_color.rgb = vec3(accumulation);
              out_color.a = color.a;
              return;
            }

        }
        out_color.rgb = vec3(0);
        out_color.a = color.a;
        out_transmittance = vec4(1);
        return;
    }
    vec3 surfacePos = (inversePlayerViewMatrix * viewPos).xyz;
    vec3 rayDir = normalize(surfacePos - cameraPos);
    float maxDist = min(length(surfacePos - cameraPos), totalDistance);
    float offset = rand(floor(pixelCoords) + vec2(randomNumber)) * stepSize * 2;

    float falloff = exp(-density * stepSize);
    float phi = phaseFunction(normalize(lightPosition), rayDir, anisotropy);

    float transmittance = 1;
    vec3 accumulation = vec3(0.0);

    for (float t = offset; t < maxDist; t += stepSize) {
        vec3 currentPos = cameraPos + rayDir * t;

        vec4 shadowCoords = toShadowMapSpace * vec4(currentPos, 1);
        shadowCoords /= shadowCoords.w;

        float shadowDepth = texture(shadowMap, shadowCoords.xy).r;
        float diff = shadowCoords.z - 0.001 - shadowDepth;
        float visible = smoothstep(-0.02, 0.02, -diff);


        vec3 lightAmount = lightColor * lightStrength * visible * phi;
        accumulation += albedo * density * stepSize * lightAmount * transmittance;
        transmittance *= falloff;
    }
    out_color.rgb = vec3(accumulation);
    out_color.a = color.a;
    out_transmittance = vec4(vec3(transmittance), 1);
}
