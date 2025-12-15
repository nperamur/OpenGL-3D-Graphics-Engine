#version 400 core
in vec3 position;
in vec2 textureCoords;
out vec2 pass_textureCoords;
out vec3 pass_lightPosition;


uniform vec3 lightPosition;


void main(void){
	gl_Position = vec4(position, 1.0);
	pass_textureCoords = textureCoords;
	pass_lightPosition = lightPosition;
}
