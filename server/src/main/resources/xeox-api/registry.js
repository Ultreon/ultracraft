/**
 * Parses a string ID into a ID instance.
 * 
 * @param {string} str 
 * @returns {Id}
 */
function id(str) {
    return registries.id(str);
}

/**
 * Creates a new ID by specifying a path and the mod id from the current context.
 * 
 * @param {string} path 
 * @returns {Id}
 */
function modId(path) {
    return registries.id(thisMod.id, path);
}

/**
 * Gets the registry by id.
 * 
 * @param {Id} id the id
 * @returns {Registry} the registry
 */
function registry(id) {
    return registries.registry(id);
}

/**
 * Registries for Ultracraft
 */
const Registries = {
}

events.on("postRegister", () => {
    Registries["REGISTRY"] = registry(id("registry"))
    Registries.REGISTRY.entries().forEach((entry) => {
        let key = entry.key;
        let value = entry.value;

        if (key.namespace == "ultracraft") {
            Registries[key.path.toUpperCase()] = value
        }
    })
})

/**
 * Creates a new registry builder.
 * 
 * @returns {RegistryBuilder} the registry builder
 */
function registryBuilder() {
    return registries.createBuilder();
}
