package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.CommonRegistries;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.LoadingContext;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.xeox.loader.JSEvents;

public class ServerLoader {
    public void load() {
        Registries.nopInit();
        RegistryEvents.REGISTRY_CREATION.factory().onRegistryCreation();
        JSEvents.emitAll("registryCreation");
        
        for (var reg : Registry.getRegistries()) {
            Registries.REGISTRY.register(reg.id(), reg);
        }

        if (DebugFlags.DUMP_REGISTRIES) {
            Registry.setDumpLogger((level, message, t) -> {
                switch (level) {
                    case DEBUG -> CommonConstants.LOGGER.debug(message, t);
                    case INFO -> CommonConstants.LOGGER.info(message, t);
                    case WARN -> CommonConstants.LOGGER.warn(message, t);
                    case ERROR -> CommonConstants.LOGGER.error(message, t);
                }
            });
            Registry.dump();
        }
        
        CommonRegistries.registerGameStuff();

        for (var mod : GamePlatform.get().getMods()) {
            final String id = mod.getId();
            LoadingContext.withinContext(new LoadingContext(id), () -> {
                for (Registry<?> registry : Registry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(id, registry);
                }
            });
        }

        JSEvents.emitAll("postRegister");
    }
}
