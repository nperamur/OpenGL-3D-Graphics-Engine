uniform sampler2D textureSampler;
in vec2 pass_textureCoords;

const float strength = 1.7;

out vec4 out_Color;

void main(void) {
  float dist = sqrt(pow(pass_textureCoords.x - 0.5, 2) + pow(pass_textureCoords.y - 0.5, 2));
  out_Color = vec4(texture(textureSampler, pass_textureCoords).xyz * (1 - pow(dist, 2.0) * strength), 1);








}