#version 400 core

uniform vec3 lightColor;
uniform sampler2D dudvMap;
uniform float moveFactor;

uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;
uniform sampler2D normalMap;
uniform sampler2D depthMap;
in vec3 surfaceNormal;
in vec3 toLightNormal;
in vec4 clipSpace;
in vec3 toCameraVector;
in vec2 pass_textureCoords;
in vec3 fromLightVector;

const float shineDamper = 20.0;
const float waveStrength = 0.03;
const float reflectivity = 0.8;

layout(location = 0) out vec4 out_Color;
layout(location = 1) out vec3 normal;
layout(location = 2) out vec3 position;

void main(void)
{

    vec3 viewVector = normalize(toCameraVector);
    float refractiveFactor = dot(viewVector, vec3(0.0, 1.0, 0.0));
    refractiveFactor = pow(refractiveFactor, 0.5);




	vec2 distortedTexCoords = texture(dudvMap, vec2(pass_textureCoords.x + moveFactor, pass_textureCoords.y)).rg*0.1;
	distortedTexCoords = pass_textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y+moveFactor);
	vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;

    vec2 ndc = (clipSpace.xy/clipSpace.w) / 2.0 + 0.5;
    vec2 refractionTexCoords = vec2(ndc.x, ndc.y);


    float near = 0.1;
    float far = 8000;
    float depth = texture(depthMap, refractionTexCoords).r;
    float floorDist = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    float depth2 = gl_FragCoord.z;
    float waterDist = 2.0 * near * far / (far + near - (2.0 * depth2 - 1.0) * (far - near));
    float waterDepth = floorDist - waterDist;

    refractionTexCoords += totalDistortion;
    refractionTexCoords = clamp(refractionTexCoords, 0.001, 0.999);
    vec2 reflectionTexCoords = vec2(ndc.x, -ndc.y) + totalDistortion;
    reflectionTexCoords.x = clamp(reflectionTexCoords.x, 0.001, 0.999);
    reflectionTexCoords.y = clamp(reflectionTexCoords.y, -0.999, -0.001);
    vec4 reflectColor = texture(reflectionTexture, reflectionTexCoords);
    vec4 refractColor = texture(refractionTexture, refractionTexCoords);




    vec4 normalMapColor = texture(normalMap, distortedTexCoords);
    vec3 normal = vec3(normalMapColor.r * 2.0 - 1.0, normalMapColor.b, normalMapColor.g * 2 - 1);
    normal = normalize(normal);

    vec3 reflectedLight = reflect(normalize(fromLightVector), normal);
    float specular = max(dot(reflectedLight, viewVector), 0.0);
    specular = pow(specular, shineDamper);
    vec3 specularHighlights = lightColor * specular * reflectivity;
    out_Color = mix(reflectColor, refractColor, refractiveFactor);
    out_Color = mix(out_Color, vec4(0.0, 0.3, 0.5, 1.0), 0.2) + vec4(specularHighlights, 0);
}
