#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord);
    if (texColor.a < 0.05) {
        discard;
    }
    
    float alpha = texColor.a * vertexColor.a;
    fragColor = vec4(vec3(1.0), alpha) * ColorModulator;
}
