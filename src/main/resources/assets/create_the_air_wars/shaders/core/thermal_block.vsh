#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform vec3 ChunkOffset;
uniform mat3 NormalMat;
uniform float SableEnableNormalLighting;
uniform float SableSkyLightScale;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec2 texCoord;

void main() {
    vec3 pos = Position + ChunkOffset;
    vec3 transformedNormal = normalize(inverse(NormalMat) * (ModelViewMat * vec4(Normal, 0.0)).xyz + vec3(0.0001));
    float brightness = mix(1.0, clamp(abs(transformedNormal.y) * 0.6 + 0.4, 0.0, 1.0), clamp(SableEnableNormalLighting, 0.0, 1.0));
    ivec2 lightUv = ivec2(vec2(UV2) * vec2(1.0, max(SableSkyLightScale, 0.0))) / 16;

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_distance(ModelViewMat, IViewRotMat * pos, FogShape);
    vertexColor = vec4(Color.rgb * brightness, Color.a);
    lightMapColor = texelFetch(Sampler2, lightUv, 0);
    texCoord = UV0;
}
