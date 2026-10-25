#version 400 core

in vec3 colour;
in vec3 surfaceNormal;
in vec3 toLightNormal;
in vec3 pass_position;
in vec3 viewSpaceNormal;
in vec4 clipPos;
in vec4 prevClipPos;
uniform vec3 lightColor;
uniform sampler2D textureSampler;
in vec2 pass_textureCoords;
layout(location = 0) out vec4 out_Color;
layout(location = 1) out vec4 normal;
layout(location = 2) out vec4 position;
layout(location = 3) out vec4 out_motionVector;

void main(void)
{
    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitLightNormal = normalize(toLightNormal);
    float dotProduct = dot(unitNormal, unitLightNormal);
    float brightness = min(max(dotProduct, 0.3), 1.0);
    vec3 diffuse = brightness * lightColor;

    //float near = 0.1;
    //float far = 8000;
    //float depth = gl_FragCoord.z;
    //float dist = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    normal = vec4(normalize(viewSpaceNormal), 1);
    position = vec4(pass_position, 1);
    out_Color = vec4(texture(textureSampler, pass_textureCoords).rgb * diffuse, 1);
    out_motionVector = vec4((clipPos.xy / clipPos.w) - (prevClipPos.xy / prevClipPos.w), 0.0, 1.0);
}
