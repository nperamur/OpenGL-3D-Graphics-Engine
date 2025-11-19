#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoords;
layout(location = 2) in vec3 normal;
out vec3 surfaceNormal;
out vec3 toLightNormal;
out vec3 fromLightVector;

out vec4 clipSpace;
out vec2 pass_textureCoords;
out vec3 toCameraVector;

uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 lightPosition;
uniform vec3 cameraPosition;

const float tiling = 1500.0;

void main(void)
{

    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
    clipSpace =  projectionMatrix * viewMatrix * worldPosition;
    gl_Position =  clipSpace;
    pass_textureCoords = vec2(position.x / 2.0 + 0.5, position.z / 2.0 + 0.5) * tiling;
    toCameraVector = cameraPosition - worldPosition.xyz;

    toLightNormal = lightPosition - worldPosition.xyz;
    fromLightVector = worldPosition.xyz - lightPosition;
}