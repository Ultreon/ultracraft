#version 330 core

precision highp float;
precision highp int;

in vec2 UV;
out vec4 color;

uniform sampler2D u_crosshair;
uniform sampler2D u_fbo;

uniform vec2 u_frameSize;

void main() {
    color = texture2D(u_crosshair, UV);
    vec4 frame_color = texture2D(u_fbo, gl_FragCoord.xy / u_frameSize);
    color *= 1.0f - frame_color;
}