#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float Mode;
uniform float Time;
uniform float IsLocked;

in vec2 texCoord;
out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

bool draw_pixel(vec2 pos, vec2 pixel) {
    return floor(pos.x) == pixel.x && floor(pos.y) == pixel.y;
}

bool draw_L(vec2 p) {
    return (p.x == 0.0 && p.y >= 0.0 && p.y < 5.0) || (p.y == 4.0 && p.x > 0.0 && p.x < 3.0);
}

bool draw_O(vec2 p) {
    return (p.x == 0.0 && p.y >= 0.0 && p.y < 5.0) || (p.x == 2.0 && p.y >= 0.0 && p.y < 5.0) || (p.y == 0.0 && p.x == 1.0) || (p.y == 4.0 && p.x == 1.0);
}

bool draw_C(vec2 p) {
    return (p.x == 0.0 && p.y >= 0.0 && p.y < 5.0) || (p.y == 0.0 && p.x > 0.0 && p.x < 3.0) || (p.y == 4.0 && p.x > 0.0 && p.x < 3.0);
}

bool draw_K(vec2 p) {
    return (p.x == 0.0 && p.y >= 0.0 && p.y < 5.0) || (p.x == 1.0 && p.y == 2.0) || (p.x == 2.0 && (p.y == 1.0 || p.y == 3.0)) || (p.x == 3.0 && (p.y == 0.0 || p.y == 4.0));
}

void main() {
    vec4 color = texture(Sampler0, texCoord) * ColorModulator;
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    
    vec3 outColor;
    if (Mode > 0.5) {
        // Enhanced Vision (Night Vision / Thermal White Hot)
        float br = clamp(gray * 2.0 + 0.1, 0.0, 1.0);
        outColor = vec3(br);
    } else {
        // TV Mode (Greenish)
        outColor = vec3(gray * 0.8, gray, gray * 0.7);
    }
    
    vec2 screenPos = texCoord * ScreenSize;
    vec2 hudPos = texCoord * vec2(256.0);
    
    // Scanlines
    if (mod(floor(screenPos.y), 2.0) == 0.0) {
        outColor *= 0.9;
    }
    
    // Noise
    float n = (rand(texCoord + Time * 0.01) - 0.5) * 0.1;
    outColor += vec3(n);
    
    // Crosshair
    vec2 center = vec2(128.0);
    vec2 centerDist = abs(hudPos - center);
    if ((centerDist.x < 1.0 && centerDist.y < 8.0) || (centerDist.y < 1.0 && centerDist.x < 8.0)) {
        if (centerDist.x > 1.5 || centerDist.y > 1.5) {
            outColor = vec3(0.0, 1.0, 0.0);
        }
    }
    
    // LOCK Text
    if (IsLocked > 0.5 && mod(Time, 20.0) < 10.0) {
        vec2 p = floor(hudPos - (center + vec2(10.0, -8.0)));
        bool draw = false;
        if (p.x >= 0.0 && p.x < 3.0) draw = draw_L(p);
        p.x -= 4.0;
        if (p.x >= 0.0 && p.x < 3.0) draw = draw || draw_O(p);
        p.x -= 4.0;
        if (p.x >= 0.0 && p.x < 3.0) draw = draw || draw_C(p);
        p.x -= 4.0;
        if (p.x >= 0.0 && p.x < 4.0) draw = draw || draw_K(p);
        
        if (draw) {
            outColor = vec3(1.0, 0.0, 0.0);
        }
    }
    
    fragColor = vec4(clamp(outColor, 0.0, 1.0), 1.0);
}
