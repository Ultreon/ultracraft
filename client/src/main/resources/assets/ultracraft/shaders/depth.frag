#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
varying vec2 v_texCoords0;
uniform sampler2D u_diffuseTexture;
uniform float u_alphaTest;
#endif

void main() {
	float depth = gl_FragCoord.z / gl_FragCoord.w;

	vec3 depthIn3Channels;
	depthIn3Channels.r = mod(depth, 1.0);
	depth -= depthIn3Channels.r;
	depth /= 256.0;

	depthIn3Channels.g = mod(depth, 1.0);
	depth -= depthIn3Channels.g;
	depth /= 256.0;

	depthIn3Channels.b = depth;

	gl_FragColor = vec4(depthIn3Channels, 1.0);
}
