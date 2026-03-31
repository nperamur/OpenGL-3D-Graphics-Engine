uniform sampler2D lowResFog;
uniform sampler2D lowResTransmittance;
uniform sampler2D fullResMask;
uniform float lowResScaleFactor;

in vec2 pass_textureCoords;

layout(location = 0) out vec4 out_color;
layout(location = 1) out vec4 out_transmittance;

void main(void) {
  vec4 viewPos = texture(fullResMask, pass_textureCoords);
  vec2 texelSize = 1.0 / vec2(textureSize(lowResFog, 0));

  vec2 neighbors[3] = vec2[](
      vec2(texelSize.x, 0.0),
      vec2(0.0, texelSize.y),
      vec2(texelSize.x, texelSize.y)
  );

  vec2 fullResNeighbors[3] = vec2[](
      pass_textureCoords + vec2(texelSize.x / lowResScaleFactor, 0.0),
      pass_textureCoords + vec2(0.0, texelSize.y / lowResScaleFactor),
      pass_textureCoords + vec2(texelSize.x / lowResScaleFactor, texelSize.y / lowResScaleFactor)
  );
  vec4 transmittanceSum = vec4(0.0);
  vec4 fogSum = vec4(0.0);
  float sum = 0.0;

  float weight = 0.75;
  vec4 basePos = texture(fullResMask, pass_textureCoords);
  if (abs(basePos.a - viewPos.a) < 0.5) {
      transmittanceSum += texture(lowResTransmittance, pass_textureCoords) * weight;
      fogSum += texture(lowResFog, pass_textureCoords) * weight;
      sum += weight;
  }
  float weights[3] = float[](0.75, 0.75, 0.5);
  for (int i = 0; i < 3; i++) {
    vec4 candidatePos = texture(fullResMask, fullResNeighbors[i]);
    if (abs(candidatePos.a - viewPos.a) < 0.5) {
      transmittanceSum += texture(lowResTransmittance, pass_textureCoords + neighbors[i]) * weights[i];
      fogSum += texture(lowResFog, pass_textureCoords + neighbors[i]) * weights[i];
      sum+=weights[i];
    }

  }

  out_color = fogSum / sum;
  out_transmittance = transmittanceSum / sum;

  //out_color = texture(lowResFog, pass_textureCoords);
  //out_transmittance = texture(lowResTransmittance, pass_textureCoords);
}