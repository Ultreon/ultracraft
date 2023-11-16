#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#if defined(specularFlag) || defined(fogFlag)
#define cameraPositionFlag
#endif

attribute vec3 a_position;
uniform mat4 u_projViewTrans;

#if defined(colorFlag)
varying vec4 v_color;
attribute vec4 a_color;
#endif// colorFlag

#ifdef normalFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;
#endif// normalFlag

#ifdef textureFlag
attribute vec2 a_texCoord0;
#endif// textureFlag

#ifdef diffuseTextureFlag
uniform vec4 u_diffuseUVTransform;
varying vec2 v_diffuseUV;
#endif

#ifdef emissiveTextureFlag
uniform vec4 u_emissiveUVTransform;
varying vec2 v_emissiveUV;
#endif

#ifdef specularTextureFlag
uniform vec4 u_specularUVTransform;
varying vec2 v_specularUV;
#endif

#ifdef boneWeight0Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight0;
#endif//boneWeight0Flag

#ifdef boneWeight1Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight1;
#endif//boneWeight1Flag

#ifdef boneWeight2Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight2;
#endif//boneWeight2Flag

#ifdef boneWeight3Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight3;
#endif//boneWeight3Flag

#ifdef boneWeight4Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight4;
#endif//boneWeight4Flag

#ifdef boneWeight5Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight5;
#endif//boneWeight5Flag

#ifdef boneWeight6Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight6;
#endif//boneWeight6Flag

#ifdef boneWeight7Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight7;
#endif//boneWeight7Flag

#if defined(numBones) && defined(boneWeightsFlag)
#if (numBones > 0)
#define skinningFlag
#endif
#endif

uniform mat4 u_worldTrans;

#if defined(numBones)
#if numBones > 0
uniform mat4 u_bones[numBones];
#endif//numBones
#endif

#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif// shininessFlag

#ifdef blendedFlag
uniform float u_opacity;
varying float v_opacity;

#ifdef alphaTestFlag
uniform float u_alphaTest;
varying float v_alphaTest;
#endif//alphaTestFlag
#endif// blendedFlag

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif// ambientLightFlag

#ifdef ambientCubemapFlag
uniform vec3 u_ambientCubemap[6];
#endif// ambientCubemapFlag

#ifdef sphericalHarmonicsFlag
uniform vec3 u_sphericalHarmonics[9];
#endif//sphericalHarmonicsFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif// specularFlag

#ifdef cameraPositionFlag
uniform vec4 u_cameraPosition;
#endif// cameraPositionFlag

#ifdef fogFlag
varying float v_fog;
#endif// fogFlag


#if numDirectionalLights > 0
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif// numDirectionalLights

#if numPointLights > 0
struct PointLight
{
    vec3 color;
    vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif// numPointLights

#if    defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif//ambientFlag

#ifdef shadowMapFlag
uniform mat4 u_shadowMapProjViewTrans;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag
#endif//shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif//separateAmbientFlag

#endif// lightingFlag

void main() {
    #ifdef diffuseTextureFlag
    v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
    #endif//diffuseTextureFlag

    #ifdef emissiveTextureFlag
    v_emissiveUV = u_emissiveUVTransform.xy + a_texCoord0 * u_emissiveUVTransform.zw;
    #endif//emissiveTextureFlag

    #ifdef specularTextureFlag
    v_specularUV = u_specularUVTransform.xy + a_texCoord0 * u_specularUVTransform.zw;
    #endif//specularTextureFlag

    #if defined(colorFlag)
    v_color = a_color;
    #endif// colorFlag

    #ifdef blendedFlag
    v_opacity = u_opacity;
    #ifdef alphaTestFlag
    v_alphaTest = u_alphaTest;
    #endif//alphaTestFlag
    #endif// blendedFlag

    #ifdef skinningFlag
    mat4 skinning = mat4(0.0);
    #ifdef boneWeight0Flag
    skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
    #endif//boneWeight0Flag
    #ifdef boneWeight1Flag
    skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
    #endif//boneWeight1Flag
    #ifdef boneWeight2Flag
    skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
    #endif//boneWeight2Flag
    #ifdef boneWeight3Flag
    skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];
    #endif//boneWeight3Flag
    #ifdef boneWeight4Flag
    skinning += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];
    #endif//boneWeight4Flag
    #ifdef boneWeight5Flag
    skinning += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];
    #endif//boneWeight5Flag
    #ifdef boneWeight6Flag
    skinning += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];
    #endif//boneWeight6Flag
    #ifdef boneWeight7Flag
    skinning += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];
    #endif//boneWeight7Flag
    #endif//skinningFlag

    #ifdef skinningFlag
    vec4 pos = u_worldTrans * skinning * vec4(a_position, 1.0);
    #else
    vec4 pos = u_worldTrans * vec4(a_position, 1.0);
    #endif

    gl_Position = u_projViewTrans * pos;
}
