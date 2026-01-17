#version 400 core
uniform sampler2D volumetricTexture;
uniform sampler2D transmittanceMap;
uniform sampler2D historyColor;
uniform sampler2D historyTransmittance;
uniform sampler2D gPosition;
uniform sampler2D currPosition;

uniform mat4 projectionMatrix;

uniform mat4 prevViewMatrix;
uniform mat4 currViewMatrix;

uniform float prevMoveFactor;
uniform float moveFactor;
uniform mat4 inverseViewMatrix;
uniform mat4 inverseProjectionMatrix;
in vec2 pass_textureCoords;
layout(location = 0) out vec4 out_color;
layout(location = 1) out vec4 out_transmittance;


void main(void) {
    float weight;
    vec4 currPos;
    vec4 origPos = texture(currPosition, pass_textureCoords);
    vec4 reprojectedWorldPos;
    if (origPos.a != 0) {
      weight = 0.2f;
      currPos = vec4(origPos.xyz, 1.0);
      reprojectedWorldPos = inverseViewMatrix * currPos;
    } else {
      ivec2 texSize = textureSize(currPosition, 0);
      vec2 screenSize = vec2(texSize);
      vec2 pixelCoords = pass_textureCoords * screenSize;
      float clip_x = (pixelCoords.x / screenSize.x) * 2.0 - 1.0;
      float clip_y = (pixelCoords.y / screenSize.y) * 2.0 - 1.0;
      vec4 farPoint = vec4(clip_x, clip_y, 1.0, 1.0);


      vec4 cloudPos = inverseProjectionMatrix * farPoint;
      vec3 cameraPos = (inverseViewMatrix * vec4(0, 0, 0, 1)).xyz;
      cloudPos = inverseViewMatrix * (cloudPos / cloudPos.w);
      vec3 cloudRayDir = normalize(cloudPos.rgb - cameraPos);
      vec3 cloudRay = (180 / cloudRayDir.y) * cloudRayDir;
      currPos = vec4(vec3(cameraPos.x + cloudRay.x, cameraPos.y + cloudRay.y, cameraPos.z + cloudRay.z), 1);
      reprojectedWorldPos = currPos;

      weight = 0.08f;

    }
    if (origPos.a == 0) {
      reprojectedWorldPos = vec4(reprojectedWorldPos.x - (moveFactor - prevMoveFactor) * 100,
                    reprojectedWorldPos.y, reprojectedWorldPos.z - (moveFactor - prevMoveFactor) * 100, reprojectedWorldPos.w);
    }
    vec4 clipReprojected = projectionMatrix * prevViewMatrix * reprojectedWorldPos;
    vec2 reprojectedCoords = (clipReprojected / clipReprojected.w).xy * 0.5 + 0.5;


    //accumulation
    vec4 volumetricColor = texture(volumetricTexture, pass_textureCoords);
    float transmittance = texture(transmittanceMap, pass_textureCoords).r;

    vec4 prevColor = texture(historyColor, reprojectedCoords);
    float prevTransmittance = texture(historyTransmittance, reprojectedCoords).r;
    vec4 prevPos = texture(gPosition, reprojectedCoords);

    bool isOffScreen = any(lessThan(reprojectedCoords, vec2(0.0))) ||
                       any(greaterThan(reprojectedCoords, vec2(1.0)));

    float depthCurr = origPos.z;
    float depthPrev = prevPos.z;
    bool badHistory = (origPos.a == 0 && prevPos.a > 0 || origPos.a > 0 && prevPos.a == 0 || abs(depthCurr - depthPrev) > 10);


    if (isOffScreen || badHistory) {
        out_color = vec4(volumetricColor.rgb, 1.0);
        out_transmittance = vec4(vec3(transmittance), 1.0);
    } else {
        out_color = vec4(mix(prevColor.rgb, volumetricColor.rgb, weight), 1.0);
        out_transmittance = vec4(vec3(mix(prevTransmittance, transmittance, weight)), 1.0);
    }
}