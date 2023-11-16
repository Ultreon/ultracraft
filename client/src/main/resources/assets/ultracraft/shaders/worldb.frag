#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying vec2 v_texCoords;

uniform sampler2D u_depthMap;
uniform sampler2D u_diffuseTexture;
uniform vec2 u_resolution;
const float u_radius = 0.025;// Adjust this parameter based on your scene
const float u_intensity = 1.5;// Adjust this parameter based on your scene

void main() {
    vec2 texelSize = 1.0 / u_resolution;

    // Assuming gl_FragColor contains the encoded color
    HIGH vec4 color = texture2D(u_depthMap, v_texCoords);

    // Reconstruct the depth value from the color components
    HIGH float depth = color.x + color.y / 255.0 + color.z / 65025.0 + color.w / 16581375.0;

    float occlusion = 0.0;

    for (float i = 0.0; i < 6.28319; i += 1.0) {
        vec2 offset = u_radius * vec2(cos(i), sin(i));
        vec3 sampleCoord = fragCoord + vec3(offset, 0.0);
        float sampleDepth = texture2D(u_depthMap, sampleCoord.xy * texelSize).r;
        occlusion += smoothstep(0.0, 1.0, u_radius / abs(depth - sampleDepth));
    }

    occlusion = 1.0 - occlusion / 6.28319;
    occlusion = pow(occlusion, u_intensity);

    gl_FragColor = vec4(vec3(occlusion), 1.0);
}
