#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoords;
layout(location = 2) in vec3 normal;
out vec2 pass_textureCoords;
out vec3 colour;
out vec3 surfaceNormal;
out vec3 toLightNormal;
out vec3 pass_position;
out vec3 viewSpaceNormal;

uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec4 plane;
uniform vec3 lightPosition;

void main(void)
{
    vec4 worldPosition = transformationMatrix * vec4( position, 1.0 );
    gl_ClipDistance[0] = dot(worldPosition, plane);
    colour = vec3(position.x+0.5,1,position.y+0.5);
    surfaceNormal = (transformationMatrix * vec4(normal, 0.0)).xyz;
    toLightNormal = lightPosition - worldPosition.xyz;
    gl_Position =  projectionMatrix * viewMatrix * worldPosition;
    pass_textureCoords = textureCoords;
    pass_position = (viewMatrix * worldPosition).xyz;

    //mat3 normalMatrix = transpose(inverse(mat3(viewMatrix * transformationMatrix)));
    mat3 normalMatrix = mat3(viewMatrix * transformationMatrix);
    viewSpaceNormal = normalize(normalMatrix * normal);

}