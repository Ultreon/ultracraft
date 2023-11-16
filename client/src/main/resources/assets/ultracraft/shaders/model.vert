#version 140
#extension GL_ARB_explicit_attrib_location : enable

#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

in vec3 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

out vec4 v_color;
out vec2 v_texCoords;

void main () {
    v_color = a_color;
    v_color.a = v_color.a * (255.0/254.0);
    v_texCoords = a_texCoord0;

    vec4 pos = u_worldTrans * vec4(a_position, 1.0);
    gl_Position = u_projViewTrans * pos;
}
