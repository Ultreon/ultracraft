#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
varying vec3 v_normal;

uniform sampler2D u_texture;
uniform float u_ambientIntensity;

void main() {
    vec3 ambientColor = vec3(0.3, 0.3, 0.3); // Adjust this for the desired color and intensity.

    float ambientOcclusion = texture2D(u_texture, v_texCoord).r;

    vec3 finalColor = texture2D(u_texture, v_texCoord).rgb * ambientColor * (1.0 - ambientOcclusion * u_ambientIntensity);

    gl_FragColor = vec4(finalColor, 1.0);
}
