out vec4 out_Color;

in vec2 pass_textureCoords;

uniform sampler2D ssaoInput;

void main() {
    vec2 texelSize = 1.0 / vec2(textureSize(ssaoInput, 0));
    float result = 0.0;
    for (int x = -8; x < 8; ++x)
    {
        for (int y = -8; y < 8; ++y)
        {
            vec2 offset = vec2(float(x), float(y)) * texelSize;
            result += texture(ssaoInput, pass_textureCoords + offset).r;
        }
    }
    out_Color = vec4(1, 1, 1, 1) * (result / (4.0 * 4.0));
}