#version 400 core

in vec2 pass_textureCoords;
uniform sampler2D textureSampler;
uniform vec3 lightColor;
uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;
uniform float shineDamper;
uniform float reflectivity;
in vec3 surfaceNormal;
in vec3 toLightNormal;
in vec3 toCameraVector;
in vec3 pass_position;
in vec3 viewSpaceNormal;

layout(location = 0) out vec4 out_Color;
layout(location = 1) out vec4 normal;
layout(location = 2) out vec4 position;

void main(void)
{
    vec4 blendMapColor = texture(blendMap, pass_textureCoords);
    float backTextureAmount = 1 - (blendMapColor.r + blendMapColor.g + blendMapColor.b);
    vec2 tiledCoords = pass_textureCoords * 40.0;
    vec4 backgroundTextureColor = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColor = texture(rTexture, tiledCoords) * blendMapColor.r;
    vec4 gTextureColor = texture(gTexture, tiledCoords) * blendMapColor.g;
    vec4 bTextureColor = texture(bTexture, tiledCoords) * blendMapColor.b;
    vec3 unitNormal = normalize(surfaceNormal);
    vec4 totalColor = backgroundTextureColor + rTextureColor + gTextureColor + bTextureColor;

    vec3 unitLightNormal = normalize(toLightNormal);

    //vec3 unitVectorToCamera = normalize(toCameraVector);
    //vec3 lightDirection = -unitLightNormal;
    //vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
    //float specularFactor = dot(unitVectorToCamera, reflectedLightDirection);

    //specularFactor = max(0.0, specularFactor);
    //float dampedFactor = pow(specularFactor, shineDamper);
    //vec3 finalSpecular = dampedFactor * lightColor;

    float dotProduct = dot(unitNormal, unitLightNormal);
    float brightness = max(dotProduct, 0.4);
    vec3 diffuse = brightness * lightColor;



    out_Color = vec4((vec4(diffuse, 1.0) * totalColor).rgb, 1);
    normal = vec4(normalize(viewSpaceNormal), 1);
    position = vec4(pass_position, 1);
}
