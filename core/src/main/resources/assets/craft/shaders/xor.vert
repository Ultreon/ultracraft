#version 330 core

precision highp float;
precision highp int;

out vec2 UV;

uniform vec4 u_position;
uniform mat4 u_projTrans;

void main() {
    UV = vec2(u_position.x, u_position.y);

    gl_Position = u_projTrans * u_position;
}