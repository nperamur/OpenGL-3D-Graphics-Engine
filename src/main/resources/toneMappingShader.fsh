#version 400 core
uniform float exposure;
uniform sampler2D textureSampler;
out vec4 out_color;
in vec2 pass_textureCoords;

void main(void) {
  vec4 color = texture(textureSampler, pass_textureCoords);
  out_color = vec4(color.rgb * exposure / (color.rgb + 1), color.a);
}