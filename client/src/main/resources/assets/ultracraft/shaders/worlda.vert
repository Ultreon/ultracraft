#version 330

attribute vec3 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

out vec4 v_color;
out vec2 v_texCoords;

void main() {
    v_color = a_color;
    v_color.a = v_color.a * (255.0/254.0);
    v_texCoords = a_texCoord0;

    vec4 position = u_worldTrans * vec4(a_position, 1.0);
    v_position = position.xyz;

    vec4 pos = u_projViewTrans * position;
    gl_Position = pos;
}
