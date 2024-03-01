package com.ultreon.craft.experiments;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.util.YamlIo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@SuppressWarnings("SameParameterValue")
public class ExperimentalFeatures {
    public static final ExperimentalFeature CHUNK_SECTION_BORDERS = create("chunk_section_borders", false);
    private static HashMap<Identifier, Boolean> experiments = YamlIo.readYaml("experiments.yml");

    static {
        if (experiments == null) {
            experiments = new HashMap<>();
            for (ExperimentalFeature experimentalFeature : Registries.EXPERIMENTAL_FEATURE.getValues()) {
                experiments.put(Registries.EXPERIMENTAL_FEATURE.getId(experimentalFeature), experimentalFeature.getDefaultState());
            }
        }


        YamlIo.writeYaml("experiments", experiments);
    }

    private static ExperimentalFeature create(@NotNull String name, boolean defaultState) {
        ExperimentalFeature experimentalFeature = new ExperimentalFeature(defaultState);
        Registries.EXPERIMENTAL_FEATURE.register(new Identifier(name), experimentalFeature);
        return experimentalFeature;
    }

    public static void updateAll() {
        for (ExperimentalFeature experimentalFeature : Registries.EXPERIMENTAL_FEATURE.getValues()) {
            experimentalFeature.update();
        }

        YamlIo.writeYaml("experiments", experiments);
    }

    public static boolean isEnabled(@NotNull ExperimentalFeature feature) {
        @Nullable Identifier id = Registries.EXPERIMENTAL_FEATURE.getId(feature);

        if (id == null) {
            return false;
        }

        return experiments.getOrDefault(id, feature.getDefaultState());
    }

    public static void setEnabled(@NotNull ExperimentalFeature feature, boolean enabled) {
        @Nullable Identifier experimentalFeature = Registries.EXPERIMENTAL_FEATURE.getId(feature);
        updateAll();
    }

    public static void toggle(ExperimentalFeature experimentalFeature) {
        ExperimentalFeatures.setEnabled(experimentalFeature, !ExperimentalFeatures.isEnabled(experimentalFeature));
    }
}
