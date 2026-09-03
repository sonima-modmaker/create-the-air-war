#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord);
    if (texColor.a < 0.05) {
        discard;
    }

    float lightStrength = max(0.45, max(lightMapColor.r, max(lightMapColor.g, lightMapColor.b)));
    float alpha = texColor.a * vertexColor.a * lightStrength;
    vec4 thermalColor = vec4(vec3(1.0), alpha) * ColorModulator;
    fragColor = linear_fog(thermalColor, vertexDistance, FogStart, FogEnd, FogColor);
}
