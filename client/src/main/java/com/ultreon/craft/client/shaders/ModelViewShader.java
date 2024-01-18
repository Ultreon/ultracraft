package com.ultreon.craft.client.shaders;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.client.world.ClientChunk;

public class ModelViewShader extends DefaultShader {
    private final static Attributes tmpAttributes = new Attributes();
    private static String version = "330 core";
    public final int u_globalSunlight;

    public ModelViewShader(final Renderable renderable) {
        this(renderable, new Config());
    }

    public ModelViewShader(final Renderable renderable, final Config config) {
        this(renderable, config, createPrefix(renderable, config));
    }

    public ModelViewShader(final Renderable renderable, final Config config, final String prefix) {
        this(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
    }

    public ModelViewShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader,
                           final String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    public ModelViewShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        this.u_globalSunlight = this.register(Inputs.globalSunlight, Setters.globalSunlight);
    }

    public static class Inputs extends DefaultShader.Inputs {
        public final static Uniform globalSunlight = new Uniform("u_globalSunlight");
    }


    public static class Setters extends DefaultShader.Setters {
        public final static Setter globalSunlight = new LocalSetter() {
            @Override
            public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                if (renderable != null) {
                    if (renderable.userData instanceof ClientChunk clientChunk) {
                        shader.set(inputID, clientChunk.getWorld().getGlobalSunlight());
                    } else {
                        shader.set(inputID, 1.0f);
                    }
                } else {
                    shader.set(inputID, 1.0f);
                }
            }
        };
    }
    public static String createPrefix (final Renderable renderable, final Config config) {
        final Attributes attributes = ModelViewShader.combineAttributes(renderable);
        StringBuilder prefix = new StringBuilder();
//        prefix.append("#version ").append(ModelViewShader.version).append("\n");
        final long attributesMask = attributes.getMask();
        final long vertexMask = renderable.meshPart.mesh.getVertexAttributes().getMask();
        if (ModelViewShader.and(vertexMask, VertexAttributes.Usage.Position)) prefix.append("#define positionFlag\n");
        if (ModelViewShader.or(vertexMask, VertexAttributes.Usage.ColorUnpacked | VertexAttributes.Usage.ColorPacked)) prefix.append("#define colorFlag\n");
        if (ModelViewShader.and(vertexMask, VertexAttributes.Usage.BiNormal)) prefix.append("#define binormalFlag\n");
        if (ModelViewShader.and(vertexMask, VertexAttributes.Usage.Tangent)) prefix.append("#define tangentFlag\n");
        if (ModelViewShader.and(vertexMask, VertexAttributes.Usage.Normal)) prefix.append("#define normalFlag\n");
        if ((ModelViewShader.and(vertexMask, VertexAttributes.Usage.Normal) || ModelViewShader.and(vertexMask, VertexAttributes.Usage.Tangent | VertexAttributes.Usage.BiNormal)) && renderable.environment != null) {
            prefix.append("#define lightingFlag\n");
            prefix.append("#define ambientCubemapFlag\n");
            prefix.append("#define numDirectionalLights ").append(config.numDirectionalLights).append("\n");
            prefix.append("#define numPointLights ").append(config.numPointLights).append("\n");
            prefix.append("#define numSpotLights ").append(config.numSpotLights).append("\n");
            if (attributes.has(ColorAttribute.Fog)) {
                prefix.append("#define fogFlag\n");
            }
            if (renderable.environment.shadowMap != null) prefix.append("#define shadowMapFlag\n");
            if (attributes.has(CubemapAttribute.EnvironmentMap)) prefix.append("#define environmentCubemapFlag\n");
        }
        final int n = renderable.meshPart.mesh.getVertexAttributes().size();
        for (int i = 0; i < n; i++) {
            final VertexAttribute attr = renderable.meshPart.mesh.getVertexAttributes().get(i);
            if (attr.usage == VertexAttributes.Usage.TextureCoordinates) prefix.append("#define texCoord").append(attr.unit).append("Flag\n");
        }
        if (renderable.bones != null) {
            for (int i = 0; i < config.numBoneWeights; i++) {
                prefix.append("#define boneWeight").append(i).append("Flag\n");
            }
        }
        if ((attributesMask & BlendingAttribute.Type) == BlendingAttribute.Type)
            prefix.append("#define " + BlendingAttribute.Alias + "Flag\n");
        if ((attributesMask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse) {
            prefix.append("#define " + TextureAttribute.DiffuseAlias + "Flag\n");
            prefix.append("#define " + TextureAttribute.DiffuseAlias + "Coord texCoord0\n"); // FIXME implement UV mapping
        }
        if ((attributesMask & TextureAttribute.Specular) == TextureAttribute.Specular) {
            prefix.append("#define " + TextureAttribute.SpecularAlias + "Flag\n");
            prefix.append("#define " + TextureAttribute.SpecularAlias + "Coord texCoord0\n"); // FIXME implement UV mapping
        }
        if ((attributesMask & TextureAttribute.Normal) == TextureAttribute.Normal) {
            prefix.append("#define " + TextureAttribute.NormalAlias + "Flag\n");
            prefix.append("#define " + TextureAttribute.NormalAlias + "Coord texCoord0\n"); // FIXME implement UV mapping
        }
        if ((attributesMask & TextureAttribute.Emissive) == TextureAttribute.Emissive) {
            prefix.append("#define " + TextureAttribute.EmissiveAlias + "Flag\n");
            prefix.append("#define " + TextureAttribute.EmissiveAlias + "Coord texCoord0\n"); // FIXME implement UV mapping
        }
        if ((attributesMask & TextureAttribute.Reflection) == TextureAttribute.Reflection) {
            prefix.append("#define " + TextureAttribute.ReflectionAlias + "Flag\n");
            prefix.append("#define " + TextureAttribute.ReflectionAlias + "Coord texCoord0\n"); // FIXME implement UV mapping
        }
        if ((attributesMask & TextureAttribute.Ambient) == TextureAttribute.Ambient) {
            prefix.append("#define " + TextureAttribute.AmbientAlias + "Flag\n");
            prefix.append("#define " + TextureAttribute.AmbientAlias + "Coord texCoord0\n"); // FIXME implement UV mapping
        }
        if ((attributesMask & ColorAttribute.Diffuse) == ColorAttribute.Diffuse)
            prefix.append("#define " + ColorAttribute.DiffuseAlias + "Flag\n");
        if ((attributesMask & ColorAttribute.Specular) == ColorAttribute.Specular)
            prefix.append("#define " + ColorAttribute.SpecularAlias + "Flag\n");
        if ((attributesMask & ColorAttribute.Emissive) == ColorAttribute.Emissive)
            prefix.append("#define " + ColorAttribute.EmissiveAlias + "Flag\n");
        if ((attributesMask & ColorAttribute.Reflection) == ColorAttribute.Reflection)
            prefix.append("#define " + ColorAttribute.ReflectionAlias + "Flag\n");
        if ((attributesMask & FloatAttribute.Shininess) == FloatAttribute.Shininess)
            prefix.append("#define " + FloatAttribute.ShininessAlias + "Flag\n");
        if ((attributesMask & FloatAttribute.AlphaTest) == FloatAttribute.AlphaTest)
            prefix.append("#define " + FloatAttribute.AlphaTestAlias + "Flag\n");
        if (renderable.bones != null && config.numBones > 0) prefix.append("#define numBones ").append(config.numBones).append("\n");
        return prefix.toString();
    }

    private static Attributes combineAttributes(final Renderable renderable) {
        ModelViewShader.tmpAttributes.clear();
        if (renderable.environment != null) ModelViewShader.tmpAttributes.set(renderable.environment);
        if (renderable.material != null) ModelViewShader.tmpAttributes.set(renderable.material);
        return ModelViewShader.tmpAttributes;
    }

    private static boolean and(final long mask, final long flag) {
        return (mask & flag) == flag;
    }

    private static boolean or(final long mask, final long flag) {
        return (mask & flag) != 0;
    }

    public static void setVersion(String version) {
        ModelViewShader.version = version;
    }
}
