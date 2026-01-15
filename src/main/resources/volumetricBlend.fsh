#version 400 core
uniform sampler2D originalTexture;
uniform sampler2D volumetricTexture;
uniform sampler2D transmittanceMap;


in vec2 pass_textureCoords;
out vec4 out_color;

void main(void) {
    vec4 volumetricColor = texture(volumetricTexture, pass_textureCoords);
    vec4 color = texture(originalTexture, pass_textureCoords);
    float transmittance = texture(transmittanceMap, pass_textureCoords).r;

    out_color = vec4(color.rgb * transmittance + volumetricColor.rgb, 1);
}