attribute vec4 a_position;

uniform mat4 u_projTrans;
uniform int u_thickness;
uniform vec3 u_blockPosition;

void main() {
    vec4 worldPosition = u_projTrans * a_position;
    gl_Position = worldPosition;
    gl_Position.xyz += normalize(u_blockPosition - worldPosition.xyz) * u_thickness;
}