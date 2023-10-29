#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_breakingStage;

varying vec2 v_texCoord0;

void main() {
    vec4 color = texture2D(u_texture, v_texCoord0);
    float breakingFactor = step(u_breakingStage, v_texCoord0.x);
    gl_FragColor = color * vec4(vec3(1.0 - breakingFactor), 1.0);
}