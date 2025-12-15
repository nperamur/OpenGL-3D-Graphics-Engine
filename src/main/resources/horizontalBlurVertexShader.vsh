#version 400 core
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoords;
layout(location = 2) in vec3 normal;
uniform float targetWidth;
uniform int numSamples;

out vec2 blurTextureCoords[11];
out vec2 pass_textureCoords;

void main(void) {
    gl_Position = vec4( position, 1.0 );
    vec2 centerTexCoords = position.xy * 0.5 + 0.5;
    float pixelSize = 1.0 / targetWidth;
    pass_textureCoords = textureCoords;

    for (int i = -numSamples / 2; i <= numSamples / 2; i++) {
        blurTextureCoords[i + numSamples / 2] = centerTexCoords + vec2(i * pixelSize, 0);
    }

}