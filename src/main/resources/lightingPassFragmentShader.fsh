#version 400 core

uniform sampler2D shadowMap;
uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D textureSampler;

uniform vec3 lightPosition;
uniform vec3 lightColor;


uniform mat4 inversePlayerViewMatrix;
uniform mat4 viewMatrix;
uniform mat4 toShadowMapSpace;

in vec2 pass_textureCoords;

out vec4 out_Color;
const float shadowDistance = 150;
const float transitionDistance = 3;

const int pcfDistance = 1;
const float totalTexels = (pcfDistance * 2 + 1) * (pcfDistance * 2 + 1);



const float reflectivity = 0.4;
const float shineDamper = 20.0;

void main(void){
    vec4 color = texture(textureSampler, pass_textureCoords);
    if (color.a == 0) {
      out_Color = color;
      return;
    }

    vec4 viewPos = texture(gPosition, pass_textureCoords);
	  vec4 worldPosition = inversePlayerViewMatrix * viewPos;


    vec4 shadowCoords = toShadowMapSpace * worldPosition;


    vec3 projCoords = shadowCoords.xyz / shadowCoords.w;

    float objectNearestLight = texture(shadowMap, projCoords.xy).r;


    float distance = length(viewPos.xyz);
    distance = distance - (shadowDistance - transitionDistance);
    distance = distance / transitionDistance;
    shadowCoords.w = clamp(1 - distance, 0.0, 1.0);


    float pcfFactor = 0;
    vec2 texSize = vec2(textureSize(shadowMap, 0));
    vec2 texelSize = 1.0 / texSize;

    for (int i = -pcfDistance; i <= pcfDistance; i++) {
        for (int j = -pcfDistance; j <= pcfDistance; j++) {
            float objectNearestLight = texture(shadowMap, projCoords.xy + vec2(i, j) * texelSize).r;
            if (projCoords.z > objectNearestLight) {
                pcfFactor++;
            }
        }

    }

    pcfFactor /= totalTexels;

    float lightFactor = 1.0 - (pcfFactor * shadowCoords.w) * 0.2;



    //specular
    if (lightPosition.y < -100) {
        out_Color = vec4(color.rgb * lightFactor, 1);
        return;
    }
    vec3 lightPosView = (viewMatrix * vec4(lightPosition, 1.0)).xyz;

    vec3 normal = normalize(texture(gNormal, pass_textureCoords).xyz);
    vec3 lightDir = normalize(lightPosView - viewPos.xyz);
    vec3 viewDir = normalize(-viewPos.xyz);
    vec3 halfwayDir = normalize(lightDir + viewDir);

    float specAngle = max(dot(normal, halfwayDir), 0.0);
    float specular = pow(specAngle, shineDamper);



    float fresnel = pow(1.0 - max(dot(viewDir, normal), 0.0), 5.0);
    specular *= mix(0.1, 1.0, fresnel);

    float falloffStrength = 5;
    float falloff = exp(-falloffStrength * (1.0 - max(dot(normal, halfwayDir), 0.0)));
    specular *= falloff;


    vec3 specularHighlights = lightColor * specular * reflectivity;


    vec3 light = color.rgb * lightFactor;
    if (length(light) < 0.7) {
        light += specularHighlights;
    }
    out_Color = vec4(light, 1);

}
