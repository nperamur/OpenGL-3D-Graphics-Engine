#version 400 core
uniform sampler2D textureOne;
uniform sampler2D textureTwo;
uniform float threshold;
uniform bool add;

in vec2 pass_textureCoords;
out vec4 out_color;

void main(void) {
    vec4 color2 = texture(textureTwo, pass_textureCoords);

    vec4 color = texture(textureOne, pass_textureCoords);
    if (add) {
        out_color = vec4(color + color2);
    } else {
        out_color = vec4(color.rgb * color2.rgb, color2.a);
    }
}