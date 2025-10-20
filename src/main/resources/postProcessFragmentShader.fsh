layout(location = 0) out vec4 out_Color;


in vec2 pass_textureCoords;
uniform sampler2D noise;
uniform sampler2D gNormal;
uniform sampler2D gPosition;
uniform mat4 projectionMatrix;

uniform vec3 samples[16];


uniform int screenWidth;
uniform int screenHeight;

int kernelSize = 16;
float radius = 0.4;
float bias = 0.03;




void main(void)
{

    vec2 noiseScale = vec2(float(screenWidth) / 4.0, float(screenHeight) / 4.0);
    float occlusion = 0;



    vec3 fragPos = texture(gPosition, pass_textureCoords).xyz;
    float maxDistance = 500.0;
    if (length(fragPos) > maxDistance) {
        out_Color = vec4(1.0);
        return;
    }
    vec3 normal = normalize(texture(gNormal, pass_textureCoords).xyz);
    vec3 randomVec = normalize(texture(noise, pass_textureCoords * noiseScale).xyz);
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal)); // finds projection vec and subtracts from random vec to get orthogonal to normal vec
    vec3 bitangent  = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);
    for (int i = 0; i < kernelSize; i++) {
        vec3 sample = TBN * samples[i];
        sample = fragPos + sample * radius;

        //get sample in screen space
        vec4 offset = vec4(sample, 1.0);
        offset = projectionMatrix * offset; //clip space
        offset.xyz /= offset.w;
        offset.xyz = offset.xyz * 0.5 + 0.5;



        //resampling the screenspace coords to the position again gives you the closest possible v
        vec3 occluderPos = texture(gPosition, offset.xy).xyz;

        float rangeCheck = smoothstep(0.0, 1.0, radius / (length(fragPos - occluderPos)));


        occlusion += (occluderPos.z >= sample.z + bias ? 1.0 : 0.0) * rangeCheck;

    }

    //occlusion *= 1 - smoothstep(0.1, 100, fragPos.z);

    //out_Color = vec4(texture(textureSampler, pass_textureCoords).rgb * (1 - occlusion/(count * 1.3)), 1) * 1.2;
    //out_Color = vec4(1.0 - out_Color.rgb, out_Color.a);
    //out_Color = vec4(vec3(1, 1, 1) * (1 - occlusion/(count * 1.3)), 1) * 1.2;
    occlusion = 1 - occlusion/(kernelSize);
    occlusion = pow(occlusion, 2);
    out_Color = vec4(vec3(1, 1, 1) * occlusion * 1.1, 1);
}
