#version 400 core
out vec4 out_color;

in vec2 pass_textureCoords;
in vec2 blurTextureCoords[11];

uniform sampler2D textureSampler;
uniform sampler2D gPosition;
uniform sampler2D gNormal;

const float KERNEL_SIZE = 5;

//const float gaussianWeights[] = float[] (
    //0.0093, 0.028002, 0.065984, 0.121703, 0.175713,
  //  0.198596, 0.175713, 0.121703, 0.065984, 0.028002, 0.0093
//);

const float omega = 5;
const float absorption = 1.0 / (2.0 * omega * omega);

const float gaussianWeights[5] = float[](
	0.06250000000,
	0.2500000000,
	0.3750000000,
	0.2500000000,
	0.06250000000
);

void main() {
    out_color = vec4(0.0);

    vec4 originalColor = texture(textureSampler, pass_textureCoords);
    float currentDepth = texture(gPosition, pass_textureCoords).z;
    vec3 currentNormal = texture(gNormal, pass_textureCoords).xyz;

    float weightSum = 0.0;

    for (int i = 0; i < KERNEL_SIZE; i++) {

        //get differences for sampled depth and sampled normal in 0-1 range
        vec4 color;
        float sampledDepth;
        vec3 sampledNormal;
        if (i == KERNEL_SIZE / 2) {
          sampledDepth = currentDepth;
          sampledNormal = currentNormal;
          color = originalColor;
        } else {
          sampledDepth = texture(gPosition, blurTextureCoords[i]).z;
          sampledNormal = max(texture(gNormal, blurTextureCoords[i]).xyz, 0.01);
          color = texture(textureSampler, blurTextureCoords[i]);
        }
        float depthDiff = exp(-pow(abs(currentDepth - sampledDepth), 2) * absorption);

        float normalDiff = max((dot(normalize(currentNormal), normalize(sampledNormal)) + 1) / 2, 0.01);

        float combinedWeight = depthDiff * normalDiff * gaussianWeights[i];

        out_color += vec4(color.rgb * combinedWeight, color.a);
        weightSum += combinedWeight;
    }


    //dividing by weighted sum to normalize the output color
    out_color = vec4((out_color.rgb / weightSum), originalColor.a);
}
