#version 330

#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_radius;
uniform float u_intensity;

float readDepth(in vec2 coord) {
    // Sample the color value from the RGB encoded depth texture
    vec3 color = texture2D(u_texture, coord).rgb;

    // Decode the RGB values back into linear depth
    float r = color.r / 256.0;
    float g = color.g / 65536.0;
    float b = color.b / 16777216.0;
    float depth = r + (r - g) + (r - g - b)/** + color.a / 16777216.0*/;

    return depth;
}

void main() {
    vec2 texCoord = v_texCoords;

    float depth = readDepth(texCoord);

    // Convert screen coordinates to clip space
    vec2 fragCoord = gl_FragCoord.xy / u_resolution;
    fragCoord = fragCoord * 2.0 - 1.0;

    // Calculate occlusion
    float occlusion = 0.0;
    float numSamples = 8.0; // Adjust the number of samples as needed

    for (float i = 0.0; i < 2.0 * 3.1416; i += 2.0 * 3.1416 / numSamples) {
        vec2 offset = vec2(cos(i), sin(i)) * u_radius;
        vec2 coord = fragCoord + offset;

        // Convert back to texture coordinates
        coord = (coord + 1.0) / 2.0;

        float sampleDepth = texture2D(u_texture, coord).r;
        occlusion += step(depth, sampleDepth);
    }

    // Normalize the occlusion
    occlusion /= numSamples;

    // Apply intensity
    occlusion = 1.0 - occlusion * u_intensity;

    // Output final color
    gl_FragColor = vec4(vec3(occlusion), 1.0);
}