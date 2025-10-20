layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoords;
layout(location = 2) in vec3 normal;
uniform float targetWidth;

out vec2 blurTextureCoords[11];
out vec2 pass_textureCoords;

void main(void) {
    gl_Position = vec4( position, 1.0 );
    vec2 centerTexCoords = position.xy * 0.5 + 0.5;
    float pixelSize = 1.0 / targetWidth;
    pass_textureCoords = textureCoords;

    for (int i = -5; i <= 5; i++) {
        blurTextureCoords[i + 5] = centerTexCoords + vec2(i * pixelSize, 0);
    }

}