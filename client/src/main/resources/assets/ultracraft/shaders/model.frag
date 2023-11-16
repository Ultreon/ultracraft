#version 330
#extension GL_ARB_explicit_attrib_location : enable

#ifdef GL_ES
precision mediump float;
#endif

in vec4 v_color;
in vec2 v_texCoords;

uniform vec3 u_viewPos = vec3(0.0);
uniform mat4 u_inverseProjectionMatrix;

uniform sampler2D u_diffuseTexture;
uniform sampler2D u_depthTexture;
uniform vec2 u_resolution;
uniform float u_radius = 0.025;
uniform float u_intensity = 1.0;

in vec3 fragCoord;
out vec4 fragColor;

void main () {
    vec2 texelSize = 1.0 / u_resolution;

    vec4 diffuse = texture(u_diffuseTexture, v_texCoords);

    vec4 encodedDepth = texture2D(u_depthTexture, v_texCoords);
    float depth = dot(encodedDepth, vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0));

    vec3 fragCoord = vec3(v_texCoords, depth);

    float occlusion = 0.0;

    for (float i = 0.0; i < 6.28319; i += 1.0) {
        vec2 offset = u_radius * vec2(cos(i), sin(i));
        vec3 sampleCoord = fragCoord + vec3(offset, 0.0);
        float sampleDepth = texture2D(u_depthTexture, sampleCoord.xy * texelSize).r;
        occlusion += smoothstep(0.0, 1.0, u_radius / abs(depth - sampleDepth));
    }

    occlusion = 1.0 - occlusion / 6.28319;
    occlusion = pow(occlusion, u_intensity);

    fragColor = diffuse/* * vec4(vec3(occlusion), 1.0)*/;
}
