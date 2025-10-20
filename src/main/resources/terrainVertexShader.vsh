#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoords;
layout(location = 2) in vec3 normal;
out vec2 pass_textureCoords;
out vec3 surfaceNormal;
out vec3 toLightNormal;
out vec3 toCameraVector;
out vec3 pass_position;
out vec3 viewSpaceNormal;

uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 lightPosition;

uniform vec4 plane;

void main(void)
{
    vec4 worldPosition = transformationMatrix * vec4( position, 1.0 );
    gl_ClipDistance[0] = dot(worldPosition, plane);
    gl_Position =  projectionMatrix * viewMatrix * worldPosition;
    pass_textureCoords = textureCoords;
    surfaceNormal = (transformationMatrix * vec4(normal, 0.0)).xyz;
    toLightNormal = lightPosition - worldPosition.xyz;
    toCameraVector = (inverse(viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition.xyz;
    pass_position = (viewMatrix * worldPosition).xyz;

    mat3 normalMatrix = mat3(viewMatrix);
    viewSpaceNormal = normalize(normalMatrix * normal);
}