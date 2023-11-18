#ifdef GL_ES
precision mediump float;
#endif

in vec4 v_color;
in vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_ssaoMap;
uniform vec2 u_resolution;// Screen resolution

void main() {
    vec2 texCoord = v_texCoords.xy;

    // Sample texture
    vec4 diffuse = texture2D(u_texture, texCoord).rgba;
    vec3 ssao = texture2D(u_ssaoMap, texCoord).rgb;

    gl_FragColor = vec4(ssao, 1.0) * diffuse;
}