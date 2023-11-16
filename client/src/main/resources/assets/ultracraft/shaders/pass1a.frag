#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
varying vec3 v_normal;
#endif//normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif//alphaTestFlag
#endif//blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#ifdef emissiveTextureFlag
varying MED vec2 v_emissiveUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#if    defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif//ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif//specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));
}

float getShadow()
{
    return (//getShadowness(vec2(0,0)) +
    getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
#endif//shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif//separateAmbientFlag

#endif//lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif// fogFlag

uniform vec2 u_resolution;

const int kernelSize = 16;// Adjust this for more or less samples
const float radius = 0.1;// Adjust this for larger or smaller AO radius
const float bias = 0.1;// Adjust this to reduce self-occlusion artifacts

void main() {
    #if defined(normalFlag)
    vec3 normal = v_normal;
    #endif// normalFlag

    vec4 diffuse = vec4(1.0);

    #if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
    vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV) * u_emissiveColor;
    #elif defined(emissiveTextureFlag)
    vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV);
    #elif defined(emissiveColorFlag)
    vec4 emissive = u_emissiveColor;
    #else
    vec4 emissive = vec4(0.0);
    #endif

    #if defined(diffuseTextureFlag) && defined(normalFlag)
    vec2 texCoord = v_diffuseUV.xy;

    // Sample texture
    vec3 color = texture2D(u_diffuseTexture, texCoord).rgb;

    // Random sampling pattern in screen space
    vec2 noise = normalize(texture2D(u_diffuseTexture, gl_FragCoord.xy / u_resolution).rg * 2.0 - 1.0) / u_resolution;

    // Calculate occlusion
    float occlusion = 0.0;
    for (int i = 0; i < kernelSize; ++i) {
        vec2 samplePos = texCoord + noise * float(i) * radius;
        vec3 sampl = texture2D(u_diffuseTexture, samplePos).rgb;

        // Convert the sample to grayscale and calculate occlusion
        float sampleValue = dot(sampl, vec3(0.299, 0.587, 0.114));
        occlusion += step(color.r * 0.5, sampleValue);
    }

    // Average occlusion and apply bias
    occlusion = occlusion / float(kernelSize);
    occlusion = ((occlusion + bias) / (1.0 + bias));

    // Combine the original color with AO
    vec3 finalColor = color * occlusion;

    diffuse = vec4(finalColor, 1.0);
    #endif// diffuseTextureFlag

    #if (!defined(lightingFlag))
    gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
    #elif (!defined(specularFlag))
    #if defined(ambientFlag) && defined(separateAmbientFlag)
    #ifdef shadowMapFlag
    gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + getShadow() * v_lightDiffuse)) + emissive.rgb;
    //gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
    #else
    gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse)) + emissive.rgb;
    #endif//shadowMapFlag
    #else
    #ifdef shadowMapFlag
    gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse) + emissive.rgb;
    #else
    gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + emissive.rgb;
    #endif//shadowMapFlag
    #endif
    #else
    #if defined(specularTextureFlag) && defined(specularColorFlag)
    vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * v_lightSpecular;
    #elif defined(specularTextureFlag)
    vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * v_lightSpecular;
    #elif defined(specularColorFlag)
    vec3 specular = u_specularColor.rgb * v_lightSpecular;
    #else
    vec3 specular = v_lightSpecular;
    #endif

    #if defined(ambientFlag) && defined(separateAmbientFlag)
    #ifdef shadowMapFlag
    gl_FragColor.rgb = (diffuse.rgb * (getShadow() * v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
    //gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
    #else
    gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
    #endif//shadowMapFlag
    #else
    #ifdef shadowMapFlag
    gl_FragColor.rgb = getShadow() * ((diffuse.rgb * v_lightDiffuse) + specular) + emissive.rgb;
    #else
    gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular + emissive.rgb;
    #endif//shadowMapFlag
    #endif
    #endif//lightingFlag

    #ifdef fogFlag
    gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
    #endif// end fogFlag

    #ifdef blendedFlag
    gl_FragColor.a = diffuse.a * v_opacity;
    #ifdef alphaTestFlag
    if (gl_FragColor.a <= v_alphaTest)
    discard;
    #endif
    #else
    gl_FragColor.a = 1.0;
    #endif

}
