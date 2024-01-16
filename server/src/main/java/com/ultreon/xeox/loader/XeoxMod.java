package com.ultreon.xeox.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.Mod;
import com.ultreon.craft.ModOrigin;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.mozilla.javascript.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Implements {@link Mod} for {@link XeoxLoader}
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see XeoxLoader
 */
public class XeoxMod implements Mod {
    private static final Context cx = GamePlatform.get().enterXeoxContext();
    private static final Pattern WINDOW_FILE = Pattern.compile("^[a-zA-Z]:.*$");
    private static final FileHandle API_HANDLE = Gdx.files.internal("xeox-api/");
    private final XeoxMetadata metadata;
    private final FileHandle modPath;
    private final String path;
    private ScriptableObject scope;
    private static final Map<Scriptable, XeoxMod> MOD_SCRIPTS = new IdentityHashMap<>();

    /**
     * Creates a new {@link XeoxMod} with the given metadata and path.
     * 
     * @param metadata the mod metadata
     * @param path     the mod path
     */
    public XeoxMod(XeoxMetadata metadata, String path) {
        this.metadata = metadata;
        this.modPath = Gdx.files.external("mods/" + metadata.id());
        this.path = path;
    }

    /**
     * Initializes the mod.
     * <p>NOTE: Internal API. This should only be invoked when you know what you are doing.</p>
     * <p>Improper usage may result in memory leaks, crashes or corruptions.</p>
     * 
     * @throws IOException if an I/O error occurs while initializing the mod
     */
    @Internal
    public void init() throws IOException {
        enable();

        Object init = scope.get("init");
        if (init instanceof NativeFunction func) {
            func.call(cx, scope, scope, new Object[0]);
        } else {
            System.out.println("init = " + init.getClass().getSuperclass().getName());
            XeoxLoader.LOGGER.error("Mod {} does not have an init function.", metadata.id());
            disable();
        }
    }

    /**
     * Enables the mod.
     */
    private void enable() {
        try {
            this.scope = importScript("main.js");
        } catch (IOException e) {
            XeoxLoader.LOGGER.error("Failed to import script main.js from mod " + metadata.id() + ".", e);
            scope = null;
            return;
        }

        Object onEnable = scope.get("onEnable", scope);
        if (onEnable instanceof FunctionObject func) {
            func.call(cx, scope, scope, new Object[0]);
        }
    }

    /**
     * Creates a new script scope.
     * 
     * @return the new scope
     * @throws InternalError if an internal error occurs.
     */
    private ScriptableObject createScope() throws InternalError {
        var scope = cx.initSafeStandardObjects();
        this.populateScope(scope);
        MOD_SCRIPTS.put(scope, this);
        return scope;
    }

    /**
     * Populates the scope with default values.
     * 
     * @param scope the scope to populate
     * @throws InternalError if an internal error occurs.
     */
    private void populateScope(ScriptableObject scope) throws InternalError {
        scope.put("thisMod", scope, new JSModInfo(this));
        scope.put("logger", scope, LoggerFactory.getLogger("Xeox:" + this.metadata.id()));
        scope.put("events", scope, new JSEvents(scope, cx));
        scope.put("libgdx", scope, new JSLibGDX());
        scope.put("loader", scope, XeoxLoader.get());
        scope.put("platform", scope, GamePlatform.get());
        scope.put("BlockPos", scope, BlockPos.class);
        scope.put("Id", scope, ElementID.class);
        scope.put("registries", scope, new JSRegistries());
        scope.put("server", scope, (Supplier<UltracraftServer>) UltracraftServer::get);
        try {
            scope.put("include", scope, new FunctionObject("require", this.getClass().getMethod("importScript", Context.class, Scriptable.class, Object[].class, Function.class), scope));
        } catch (NoSuchMethodException e) {
            throw new InternalError("Failed to create require function.", e);
        }
        XeoxEvents.INIT_BINDINGS.factory().onInitBindings(scope);
    }

    /**
     * Disables the mod.
     */
    public void disable() {
        Object onDisable = scope.get("onDisable", scope);
        if (onDisable instanceof FunctionObject func) {
            func.call(cx, scope, scope, new Object[0]);
            scope = null;
        }
    }

    /**
     * Imports a script. This method is called dynamically using reflection.
     * Implements the {@code require} function for Xeox scripts.
     * <p>
     * Example usage in Xeox scripts (.js):
     * <pre>
     * const { registry } = require("@registry");
     * </pre>
     * <p>
     * NOTE: This method should not be called directly.
     * 
     * @param cx      the current context
     * @param thisObj the current scope
     * @param args    the arguments
     * @param funObj  the function object
     * @return        the imported script
     * @throws IOException if an I/O error occurs
     */
    public static ScriptableObject importScript(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        String filename = (String) args[0];

        if (!filename.contains("..") && !filename.startsWith("/") && !filename.startsWith("\\") && !WINDOW_FILE.matcher(filename).matches() && thisObj != null) {
            XeoxMod mod = MOD_SCRIPTS.get(thisObj);
            if (filename.startsWith("@")) {
                return mod.importStdLib(filename.substring(1));
            } else {
                return mod.importScript(filename);
            }
        }
        throw new IllegalArgumentException("Invalid filename: " + filename);
    }

    private ScriptableObject importStdLib(String path) throws IOException {
        if (!path.endsWith(".js")) path += ".js";
        var scope = createScope();
        cx.evaluateReader(scope, API_HANDLE.child(path).reader(), metadata.id() + ":<@" + path + ">", 1, null);
        return scope;
    }

    private ScriptableObject importScript(String path) throws IOException {
        if (!path.endsWith(".js")) path += ".js";
        var scope =  createScope();
        cx.evaluateReader(scope, modPath.child(path).reader(), metadata.id() + ":" + path, 1, null);
        return scope;
    }

    /**
     * Disposes the context.
     * <p>
     * ⚠️ WARNING: This should only be called when you know what you are doing.
     */
    public static void dispose() {
        Context.exit();
    }

    public String getPath() {
        return path;
    }

    public XeoxMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String getId() {
        return metadata.id();
    }

    @Override
    public String getName() {
        return metadata.name();
    }

    @Override
    public String getVersion() {
        return metadata.version();
    }

    @Override
    public String getDescription() {
        return metadata.description();
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return metadata.authors();
    }

    @Override
    public ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    public @Nullable Iterable<Path> getRootPaths() {
        return Paths.get(modPath.path());
    }

    @Override
    public Optional<String> getIconPath(int size) {
        FileHandle child = modPath.child("icon.png");
        if (child.exists()) {
            return Optional.of(child.path());
        }
        return Optional.empty();
    }
}
