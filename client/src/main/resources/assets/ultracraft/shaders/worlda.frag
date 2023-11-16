#version 330
uniform vec2 u_resolution;

uniform sampler2D u_texture;
uniform sampler2D u_depthMap;

in vec4 v_color;
in vec2 v_texCoords;

const float RADIUS = 0.1;

void main(void) {
    vec2 texelSize = 1.0 / u_resolution;
    vec2 offsets[4] = vec2[](
    vec2(-texelSize.x, -texelSize.y),
    vec2(texelSize.x, -texelSize.y),
    vec2(-texelSize.x, texelSize.y),
    vec2(texelSize.x, texelSize.y)
    );

    float depth = texture2D(u_depthMap, v_texCoords).r;

    float occlusion = 0.0;

    for (int i = 0; i < 4; i++) {
        float sampleDepth = texture2D(u_depthMap, v_texCoords + offsets[i] * RADIUS).r;
        occlusion += step(sampleDepth, depth);
    }

    occlusion = 1.0 - ((occlusion / 4.0) * 0.5);// Normalize

    gl_FragColor = v_color * vec4(occlusion, occlusion, occlusion, 1.0 - occlusion);
}
