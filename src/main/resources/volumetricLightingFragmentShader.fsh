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
const float cloudY = 265;
const float cloudHeight = 400;
const float cloudStepSize = 70;


const float cloudFadeStart = 2000;
const float cloudFadeEnd = 3000;
const float numCloudShadowSamples = 3;
const float cloudAlbedo = 0.8f;
float dayCloudStrength = 7.0;
float nightCloudStrength = 2.0;


in vec2 pass_textureCoords;

float phaseFunction(vec3 L, vec3 V, float g) {
    float cosTheta = dot(L, V);
    float numerator = 1.0f - g * g;
    float denominator = pow(1.0f + g * g - 2.0f * g * cosTheta, 1.5f);
    return (1.0f / (4.0f * 3.1415926535)) * (numerator / denominator);
}


float triplePhaseFunction(vec3 L, vec3 V, float g) {

    float lobeForward = phaseFunction(L, V, g);

    float lobeInternal = phaseFunction(L, V, g * 0.5);

    float lobeBack = phaseFunction(L, V, -0.2);

    return 0.7 * lobeForward + 0.2 * lobeInternal + 0.1 * lobeBack;
}

//dithering

float rand(vec2 p) {
    const float PHI = 1.61803398875;
    const float G   = 0.61803398875;

    p = fract(p * vec2(G, PHI));
    p += dot(p, p + 31.416);
    return fract(p.x * p.y);
}

vec3 computeFinalSkyColor(vec3 color, vec3 farPoint) {
  vec3 rayDir = normalize(farPoint);
  float verticalPos = clamp(rayDir.y, 0.0, 1.0);
  float phi = phaseFunction(normalize(lightPosition), rayDir, anisotropy);
  float heightFactor = clamp(1.0 - rayDir.y, 0.0, 1.0);
  float sunHeight = normalize(lightPosition).y;
  float horizonIntensity = mix(0.2, 2, clamp(1.0 - sunHeight, 0.0, 1.0));
  float finalDensity = density * pow(heightFactor, 5 / horizonIntensity);
  float fogTransmittance = exp(-finalDensity * totalDistance);
  vec3 skyWithFog = color * fogTransmittance;
  return lightStrength * phi * (1 - fogTransmittance) * lightColor * albedo + skyWithFog;
}


float getCloudDensity(vec3 noiseCoords, float distFromCamera, float currentY) {
  float cloudDensity = texture(cloudNoiseTexture, noiseCoords).r;
  cloudDensity = pow(cloudDensity, 3);
  cloudDensity = smoothstep(0.1, 0.8, cloudDensity);

  float fadeFactor = 1.0 - smoothstep(cloudFadeStart, cloudFadeEnd, distFromCamera);
  cloudDensity *= fadeFactor;

  float heightFraction = (currentY - cloudY) / cloudHeight;
  float heightMask = smoothstep(0.0, 0.2, heightFraction) * smoothstep(1.0, 0.7, heightFraction);

  cloudDensity *= heightMask;
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
        vec3 ambientColor = max(skyColor, vec3(0.125, 0.187, 0.25));
        if (cloudRayDir.y > 0) {
            vec3 cloudRay = ((cloudY - cameraPos.y) / cloudRayDir.y) * cloudRayDir;

            vec3 cloudPosition = vec3(cameraPos.x + cloudRay.x + jitter.x * cloudStepSize, cameraPos.y + cloudRay.y, cameraPos.z + cloudRay.z + jitter.y * cloudStepSize);

            if (cloudPosition.z < cloudDist && cloudPosition.z > -cloudDist && cloudPosition.x < cloudDist && cloudPosition.x > -cloudDist) {
              float offset = rand(floor(pixelCoords) + vec2(randomNumber)) * cloudStepSize * 3;
              float transmittance = 1;
              vec3 accumulation = vec3(0.0);

              float phiOctaveOne = triplePhaseFunction(normalize(lightPosition), cloudRayDir, 0.65);
              float phiOctaveTwo = triplePhaseFunction(normalize(lightPosition), cloudRayDir, 0.25);
              float phiOctaveThree = triplePhaseFunction(normalize(lightPosition), cloudRayDir, 0.15);

              transmittance = 1;
              float sunHeight = normalize(lightPosition).y;

              float cloudLightStrength = mix(nightCloudStrength, dayCloudStrength, smoothstep(-0.4, -0.2, sunHeight));
              float distFromCamera = length(cloudRay);
              for (float t = offset; t < cloudHeight; t += cloudStepSize) {
                  vec3 currentPos = cloudPosition + cloudRayDir * t;

                  vec3 lightAmount = lightColor * cloudLightStrength;
                  vec3 noiseCoords = vec3(currentPos.x + moveFactor * 100, currentPos.y, currentPos.z + moveFactor * 100) * 0.0004;
                  float cloudDensity = getCloudDensity(noiseCoords, distFromCamera, currentPos.y);

                  //vec3 lightRay = normalize(lightPosition) * ((cloudHeight - currentPos.y) / numCloudShadowSamples);
                  vec3 lightRay = normalize(lightPosition) * 45;
                  float offset = rand(floor(pixelCoords) + vec2(randomNumber)) * (1/cloudStepSize) * 0.2;
                  //float ambientStepSize = (cloudHeight - currentPos.y) / numCloudShadowSamples;
                  float ambientStepSize = 45;
                  float lightStepSize = length(lightRay);
                  float lightRayDensitySum = 0;
                  float ambientRayDensitySum = 0;
                  if (cloudDensity > 0.001) {
                    for (float i = offset; i <= numCloudShadowSamples; i++) {
                      vec3 lightShadowPos = currentPos + lightRay * i;
                      float lightShadowCloudDensity = getCloudDensity(vec3(lightShadowPos.x + moveFactor * 100, lightShadowPos.y, lightShadowPos.z + moveFactor * 100) * 0.0004, distFromCamera, lightShadowPos.y);
                      lightRayDensitySum += lightShadowCloudDensity;
                      vec3 ambientPos = currentPos + vec3(0, ambientStepSize, 0) * i;
                      float ambientCloudDensity = getCloudDensity(vec3(ambientPos.x + moveFactor * 100, ambientPos.y, ambientPos.z + moveFactor * 100) * 0.0004, distFromCamera, ambientPos.y);
                      ambientRayDensitySum += ambientCloudDensity;
                    }
                  }
                  //shading
                  vec3 shadingOctaveOne = exp(-lightRayDensitySum * vec3(1.0, 1.2, 1.4) * lightStepSize * 1.5);
                  vec3 shadingOctaveTwo = exp(-lightRayDensitySum * vec3(1.0, 1.2, 1.4) * lightStepSize * 0.5);
                  vec3 shadingOctaveThree = exp(-lightRayDensitySum * vec3(1.0, 1.2, 1.4) * lightStepSize * 0.05);
                  float powderTerm = 1.0 - exp(-lightRayDensitySum * lightStepSize * 2.0);
                  vec3 lightShading = (shadingOctaveOne * phiOctaveOne) * powderTerm + (shadingOctaveTwo * phiOctaveTwo) * 0.5
                                       + (shadingOctaveThree * phiOctaveThree) * 0.2 + vec3(0.09) * powderTerm;
                  vec3 lightContribution = cloudAlbedo * lightAmount * lightShading;

                  //ambient light
                  float ambientTransmittance = exp(-ambientRayDensitySum * lightStepSize);
                  vec3 ambientContribution = (ambientTransmittance + 0.1) * ambientColor * cloudAlbedo;
                  //accumulation += (lightContribution + ambientContribution) * cloudDensity * cloudStepSize * transmittance;
                  //transmittance *= exp(-cloudDensity * cloudStepSize);
                  float T = exp(-cloudDensity * cloudStepSize);
                  accumulation += (lightContribution + ambientContribution) * (1.0f - T) * transmittance;
                  transmittance *= T;
                  if (transmittance < 0.01) {
                    break;
                  }
              }

              out_transmittance = vec4(transmittance);
              out_color.rgb = computeFinalSkyColor(vec3(accumulation), cloudWorldSpace.xyz);
              out_color.a = color.a;
              return;
            }

        }
        out_color.rgb = computeFinalSkyColor(vec3(0), cloudWorldSpace.xyz);

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
        accumulation += albedo * (1 - falloff) * lightAmount * transmittance;
        transmittance *= falloff;
    }
    out_color.rgb = vec3(accumulation);
    out_color.a = color.a;
    out_transmittance = vec4(vec3(transmittance), 1);
}
