attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform vec2 u_blockCoords;

varying vec2 v_texCoord0;

void main() {
    // Calculate the texture coordinates of the mesh based on the block coords
    vec2 texCoords = vec2(u_blockCoords.x * 16.0 + a_texCoord0.x,
    u_blockCoords.y * 16.0 + a_texCoord0.y);
    v_texCoord0 = texCoords;
    gl_Position = u_projTrans * a_position;
}