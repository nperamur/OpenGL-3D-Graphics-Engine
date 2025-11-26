#version 400 core
uniform sampler2D textureSampler;
uniform sampler2D gNormal;
uniform sampler2D gPosition;
uniform sampler2D shadowMap;
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
layout(location = 0) out vec4 out_color;


const float totalDistance = 145;
const float lightStrength = 500;

in vec2 pass_textureCoords;

float phaseFunction(vec3 L, vec3 V, float g) {
    float cosTheta = dot(L, V);
    float numerator = 1.0f - g * g;
    float denominator = pow(1.0f + g * g - 2.0f * g * cosTheta, 1.5f);
    return (1.0f / (4.0f * 3.1415926535)) * (numerator / denominator);
}

//dithering
float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}


void main(void) {
    vec4 color = texture(textureSampler, pass_textureCoords);
    if (color.a == 0) {
        out_color = color;
        return;
    }
    vec4 viewPos = texture(gPosition, pass_textureCoords);

    vec3 surfacePos = (inversePlayerViewMatrix * viewPos).xyz;
    vec3 cameraPos = (inversePlayerViewMatrix * vec4(0, 0, 0, 1)).xyz;

    vec3 rayDir = normalize(surfacePos - cameraPos);
    float maxDist = min(length(surfacePos - cameraPos), totalDistance);

    ivec2 texSize = textureSize(gPosition, 0);
    vec2 screenSize = vec2(texSize);
    vec2 pixelCoords = pass_textureCoords * screenSize;
    float offset = rand(floor(pixelCoords)) * stepSize;

    float falloff = exp(-density * stepSize);
    float phi = phaseFunction(normalize(lightPositio), rayDir, anisotropy);

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


    out_color.rgb = accumulation + color.rgb;
    out_color.a = color.a;


}
