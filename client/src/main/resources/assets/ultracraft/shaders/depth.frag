#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
varying vec2 v_texCoords0;
uniform sampler2D u_diffuseTexture;
uniform float u_alphaTest;
#endif

varying float v_depth;

void main() {
	// Sample the depth value from the depth texture
	float depth = v_depth;

	vec3 color = vec3(depth, fract(depth * 256.0), fract(depth * 65536.0)/**, fract(depth * 16777216.0)*/);

	// Output the final color
	gl_FragColor = vec4(color, 1.0);
}
