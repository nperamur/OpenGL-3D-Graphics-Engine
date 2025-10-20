uniform sampler2D originalTexture;
uniform float threshold;

in vec2 pass_textureCoords;
out vec4 out_color;

void main(void) {
    vec4 color = texture(originalTexture, pass_textureCoords);


    float brightness = (color.r * 0.2126) + (color.g * 0.7152) + (color.b * 0.0722);

    out_color = vec4(color.rgb * brightness * brightness, 1);



}