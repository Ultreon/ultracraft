#version 330

#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;

uniform sampler2D u_depthMap;
uniform vec2 u_resolution;
const float u_radius = 0.1;// Adjust this parameter based on your scene
const float u_intensity = 1.5;// Adjust this parameter based on your scene

void main() {
    vec2 texelSize = 1.0 / u_resolution;

    vec4 encodedDepth = texture2D(u_depthMap, v_texCoords);
    float depth = dot(encodedDepth, vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0));

    vec3 fragCoord = vec3(v_texCoords, depth);

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
