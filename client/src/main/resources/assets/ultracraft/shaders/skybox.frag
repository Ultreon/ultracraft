uniform float x_color;
uniform float z_color;
varying vec3 v_position;

void main()
{
    // Normalize the position to getConfig values between 0 and 1
    vec3 normalizedPosition = normalize(v_position);

    // Create a gradient based on the x, y, and z coordinates
    vec3 gradient = vec3(normalizedPosition.x, x_color, normalizedPosition.z);

    // Add colors for y and z coordinates
    gradient.y = abs(x_color);
    gradient.z = abs(z_color);

    // Output the color
    gl_FragColor = vec4(gradient, 1.0);
}