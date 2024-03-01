package com.ultreon.craft.client.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.ImGuiEx;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.extension.texteditor.flag.TextEditorPaletteIndex;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.mozilla.javascript.RhinoException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class ImGuiOverlay {
    public static final ImFloat I_GAMMA = new ImFloat(1.5f);
    public static final ImFloat U_CAP = new ImFloat(0.45f);
    public static final ImFloat U_RADIUS = new ImFloat(0.45f);
    public static final ImFloat U_INTENSITY = new ImFloat(1.5f);
    public static final ImFloat U_MULTIPLIER = new ImFloat(1000.0f);
    public static final ImFloat U_DEPTH_TOLERANCE = new ImFloat(0.0001f);
    public static final ImBoolean SHOW_RENDER_PIPELINE = new ImBoolean(false);
    private static final ImBoolean SHOW_IM_GUI = new ImBoolean(false);
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_SHADER_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_DEBUGGER = new ImBoolean(false);
    private static final ImBoolean SHOW_PROFILER = new ImBoolean(false);
    private static final ImBoolean SHOW_RUN_DIALOG = new ImBoolean(false);

    protected static final String[] keys = {"A", "B", "C"};
    protected static final Double[] values = {0.1, 0.3, 0.6};

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static boolean isImplCreated;
    private static boolean isContextCreated;
    private static final GuiEditor guiEditor = new GuiEditor();
    private static boolean triggerLoadWorld;
    private static ImPlotContext imPlotCtx;
    private static TextEditor jsRunEditor;
    private static final Int2ReferenceMap<String> jsErrors = new Int2ReferenceArrayMap<>();
    private static final Int2ReferenceMap<String> jsRunErrors = new Int2ReferenceArrayMap<>();
    private static String[] jsScript = new String[]{"// Write your script here"};
    private static int jsCursorColumn;
    private static int jsCursorLine;
    private static boolean focused;
    private static boolean hovered;

    public static void setupImGui() {
        UltracraftClient.LOGGER.info("Setting up ImGui");

        UltracraftClient.get().deferClose(GLFWErrorCallback.create((error, description) -> UltracraftClient.LOGGER.error("GLFW Error: {}", description)).set());
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        synchronized (ImGuiOverlay.class) {
            ImGui.createContext();
            ImGuiOverlay.imPlotCtx = ImPlot.createContext();
            ImGuiOverlay.isContextCreated = true;
        }
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

        UltracraftClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGlfw.init(windowHandle, true);
            ImGuiOverlay.imGuiGl3.init("#version 110");
        });

        ImGuiOverlay.createTextEditor();
    }

    public static void preInitImGui() {
        synchronized (ImGuiOverlay.class) {
            ImGuiOverlay.imGuiGlfw = new ImGuiImplGlfw();
            ImGuiOverlay.imGuiGl3 = new ImGuiImplGl3();
            ImGuiOverlay.isImplCreated = true;
        }
    }

    public static boolean isChunkSectionBordersShown() {
        return ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get();
    }

    public static boolean isImGuiFocused() {
        return focused;
    }

    public static void renderImGui(UltracraftClient client) {
        if (!ImGuiOverlay.SHOW_IM_GUI.get()) return;

        ImGuiOverlay.imGuiGlfw.newFrame();

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(client.getWidth(), 18);
        ImGui.setNextWindowCollapsed(true);

        // Set style once
        ImGui.getStyle().setWindowRounding(12);
        ImGui.getStyle().setFrameRounding(12);
        ImGui.getStyle().setTabRounding(10);
        ImGui.getStyle().setPopupRounding(10);
        ImGui.getStyle().setChildRounding(10);
        ImGui.getStyle().setScrollbarRounding(10);
        ImGui.getStyle().setGrabRounding(10);
        ImGui.getStyle().setAntiAliasedFill(true);
        ImGui.getStyle().setAntiAliasedLines(true);
        ImGui.getStyle().setFramePadding(10, 10);
        ImGui.getStyle().setWindowPadding(10, 10);
        ImGui.getStyle().setCellPadding(5, 5);
        ImGui.getStyle().setScrollbarSize(10);

        if (Gdx.input.isCursorCatched()) {
            ImGui.getIO().setMouseDown(new boolean[5]);
            ImGui.getIO().setMousePos(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }


        if (ImGui.beginMainMenuBar()) {
            ImGuiOverlay.renderMenuBar();
            ImGui.endMainMenuBar();
        }

        ImGuiOverlay.renderDisplay();
        ImGuiOverlay.renderWindows(client);

        ImGuiOverlay.handleTriggers();

        ImGui.render();
        ImGuiOverlay.imGuiGl3.renderDrawData(ImGui.getDrawData());

        ImGuiOverlay.handleInput();
    }

    private static void createTextEditor() {
        jsRunEditor = new TextEditor();

        TextEditorLanguageDefinition jsDefinition = createJsDefinition();
        jsRunEditor.setLanguageDefinition(jsDefinition);
        jsRunEditor.setColorizerEnable(true);
        jsRunEditor.setShowWhitespaces(false);

        System.out.println("jsRunEditor.getPalette() = " + Arrays.toString(jsRunEditor.getPalette()));
    }

    @NotNull
    private static TextEditorLanguageDefinition createJsDefinition() {
        TextEditorLanguageDefinition jsDefinition = new TextEditorLanguageDefinition();
        jsDefinition.setSingleLineComment("//");
        jsDefinition.setCommentStart("/*");
        jsDefinition.setCommentEnd("*/");
        jsDefinition.setKeywords(new String[]{
                "break", "case", "catch", "class", "const", "continue", "debugger",
                "default", "delete", "do", "else", "export", "extends", "finally",
                "for", "function", "if", "import", "in", "instanceof", "new", "return",
                "super", "switch", "this", "throw", "try", "typeof", "var", "void",
                "while", "with", "yield", "enum", "await", "implements", "interface",
                "let", "package", "private", "protected", "public", "static", "await",
                "abstract", "boolean", "byte", "char", "double", "final", "float",
                "goto", "int", "long", "native", "short", "synchronized", "throws",
                "transient", "volatile", "null", "true", "false"
        });
        jsDefinition.setAutoIdentation(true);
        jsDefinition.setName("JavaScript");
        jsDefinition.setTokenRegexStrings(Map.of(
                "\\\"(\\\\.|[^\\\"])*\\\"", TextEditorPaletteIndex.String,
                "[+-]?(0|[1-9]+([.][0-9]*)?|[.][0-9]+)([eE][+-]?[0-9]+)?", TextEditorPaletteIndex.Number,
                "0[xX][0-9a-fA-F]+", TextEditorPaletteIndex.Number,
                "0[0-7]+", TextEditorPaletteIndex.Number,
                "0b[01]+", TextEditorPaletteIndex.Number,
                "\\b(true|false|null|undefined)\\b", TextEditorPaletteIndex.Keyword,
                "([a-zA-Z_$][a-zA-Z0-9_$]*)", TextEditorPaletteIndex.Identifier,
                "[\\[\\]\\{\\}\\!\\%\\^\\&\\*\\(\\)\\-\\+\\=\\~\\|\\<\\>\\?\\/\\;\\,\\.]", TextEditorPaletteIndex.Punctuation
        ));
        return jsDefinition;
    }

    private static void renderDisplay() {
        if (ImGuiFileDialog.display("Main::loadWorld", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                Path filePathName = Path.of(ImGuiFileDialog.getFilePathName());
                UltracraftClient.invoke(() -> UltracraftClient.get().startWorld(filePathName));
            }
            ImGuiFileDialog.close();
        }
    }

    private static void handleTriggers() {
        if (ImGuiOverlay.triggerLoadWorld) {
            ImGuiOverlay.triggerLoadWorld = false;
            ImGuiFileDialog.openModal("Main::loadWorld", "Choose Folder", null, UltracraftClient.getGameDir().toAbsolutePath().toString(), "", 1, 7, ImGuiFileDialogFlags.None);
        }
    }

    private static void renderWindows(UltracraftClient client) {
        if (ImGuiOverlay.SHOW_PLAYER_UTILS.get()) ImGuiOverlay.showPlayerUtilsWindow(client);
        if (ImGuiOverlay.SHOW_GUI_UTILS.get()) ImGuiOverlay.showGuiEditor(client);
        if (ImGuiOverlay.SHOW_UTILS.get()) ImGuiOverlay.showUtils(client);
        if (ImGuiOverlay.SHOW_CHUNK_DEBUGGER.get()) ImGuiOverlay.showChunkDebugger(client);
        if (ImGuiOverlay.SHOW_SHADER_EDITOR.get()) ImGuiOverlay.showShaderEditor();
        if (ImGuiOverlay.SHOW_RUN_DIALOG.get()) ImGuiOverlay.showRunDialog();

        focused = ImGui.isAnyItemFocused();
        hovered = ImGui.isAnyItemHovered();
    }

    private static void showRunDialog() {
        ImGui.setNextWindowBgAlpha(0.5f);
        ImGui.setNextWindowSize(800, 640, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() - 400, ImGui.getMainViewport().getPosY() - 320, ImGuiCond.Once);
        if (ImGui.begin("Run JS Dialog", ImGuiOverlay.getDefaultFlags())) {
            ImGui.beginGroup();
            if (ImGui.button("Close")) ImGuiOverlay.SHOW_RUN_DIALOG.set(false);
            ImGui.sameLine();
            if (ImGui.button("Run")) {
                try {
                    UltracraftClient.get().runJS(String.join("\n", jsScript));
                } catch (RhinoException error) {
                    for (Int2ReferenceMap.Entry<String> entry : jsErrors.int2ReferenceEntrySet()) {
                        if (error.lineNumber() == entry.getIntKey() && error.details().equals(entry.getValue()))
                            jsErrors.remove(entry.getIntKey(), entry.getValue());
                    }
                    jsRunErrors.clear();
                    jsRunErrors.put(error.lineNumber(), error.details());
                    jsErrors.putAll(jsRunErrors);
                    jsRunEditor.setErrorMarkers(jsErrors);
                }
            }
            ImGui.endGroup();

            jsRunEditor.setTextLines(jsScript);
            jsRunEditor.setCursorPosition(jsCursorLine, jsCursorColumn);
            jsRunEditor.render("##RunJSDialog::Input");
            if (jsRunEditor.isTextChanged()) {
                jsScript = jsRunEditor.getTextLines();

                RhinoException rhinoException = UltracraftClient.get().compileJS(String.join("\n", jsScript));
                jsErrors.clear();
                if (rhinoException != null)
                    jsErrors.put(rhinoException.lineNumber(), rhinoException.details());

                jsRunErrors.clear();

                jsRunEditor.setTabSize(2);
                jsRunEditor.setErrorMarkers(jsErrors);
            }
            if (jsRunEditor.isCursorPositionChanged()) {
                jsCursorLine = jsRunEditor.getCursorPositionLine();
                jsCursorColumn = jsRunEditor.getCursorPositionColumn();
            }

            ImGui.end();
        }
    }

    private static void handleInput() {
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.O))
            ImGuiOverlay.triggerLoadWorld = true;
        else if (Gdx.input.isKeyJustPressed(Input.Keys.P))
            ImGuiOverlay.SHOW_PLAYER_UTILS.set(!ImGuiOverlay.SHOW_PLAYER_UTILS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.G))
            ImGuiOverlay.SHOW_GUI_UTILS.set(!ImGuiOverlay.SHOW_GUI_UTILS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.set(!ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.R))
            ImGuiOverlay.SHOW_RUN_DIALOG.set(!ImGuiOverlay.SHOW_RUN_DIALOG.get());
    }

    private static void renderMenuBar() {
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Load World...", "Ctrl+O"))
                ImGuiOverlay.triggerLoadWorld = true;
            ImGui.menuItem("Run JS Command", "Ctrl+R", ImGuiOverlay.SHOW_RUN_DIALOG);
            ImGui.endMenu();
        }
        if (ImGui.beginMenu("Edit")) {
            ImGui.menuItem("Player Editor", "Ctrl+P", ImGuiOverlay.SHOW_PLAYER_UTILS);
            ImGui.menuItem("Gui Editor", "Ctrl+G", ImGuiOverlay.SHOW_GUI_UTILS);
            ImGui.menuItem("Shader Editor", "", ImGuiOverlay.SHOW_SHADER_EDITOR);
            ImGui.endMenu();
        }
        if (ImGui.beginMenu("View")) {
            ImGui.menuItem("Utils", null, ImGuiOverlay.SHOW_UTILS);
            ImGui.menuItem("Chunks", null, ImGuiOverlay.SHOW_CHUNK_DEBUGGER);
            ImGui.menuItem("Chunk Node Borders", "Ctrl+F4", ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS);
            ImGui.menuItem("InspectionRoot", "Ctrl+P", ImGuiOverlay.SHOW_PROFILER);
            ImGui.menuItem("Render Pipeline", null, ImGuiOverlay.SHOW_RENDER_PIPELINE);
            ImGui.endMenu();
        }

        ImGui.text(" FPS: " + Gdx.graphics.getFramesPerSecond() + " ");
        ImGui.sameLine();
        ImGui.text(" Client TPS: " + Gdx.graphics.getFramesPerSecond() + " ");
        ImGui.sameLine();
        UltracraftServer server = UltracraftServer.get();
        if (server != null) {
            ImGui.text(" Server TPS: " + server.getCurrentTps() + " ");
            ImGui.sameLine();
        }
        ImGui.text(" Frame ID: " + Gdx.graphics.getFrameId() + " ");
    }

    private static void showChunkDebugger(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Chunk Debugging", ImGuiOverlay.getDefaultFlags())) {
            ImGui.end();
        }
    }

    private static void showShaderEditor() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Shader Editor", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.editFloat("iGamma", "Shader::SSAO::iGamma", ImGuiOverlay.I_GAMMA::get, ImGuiOverlay.I_GAMMA::set);
            ImGui.end();
        }
    }

    private static void showPlayerUtilsWindow(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Player Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.text("Id:", client.player::getId);
            ImGuiEx.text("Dead:", client.player::isDead);
            ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed", client.player::getWalkingSpeed, client.player::setWalkingSpeed);
            ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed", client.player::getFlyingSpeed, client.player::setFlyingSpeed);
            ImGuiEx.editFloat("Gravity:", "PlayerGravity", () -> client.player.gravity, v -> client.player.gravity = v);
            ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity", () -> client.player.jumpVel, v -> client.player.jumpVel = v);
            ImGuiEx.editFloat("Health:", "PlayerHealth", client.player::getHealth, client.player::setHealth);
            ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth", client.player::getMaxHeath, client.player::setMaxHeath);
            ImGuiEx.editBool("No Gravity:", "PlayerNoGravity", () -> client.player.noGravity, v -> client.player.noGravity = v);
            ImGuiEx.editBool("Flying:", "PlayerFlying", client.player::isFlying, client.player::setFlying);
            ImGuiEx.editBool("Allow Flight:", "PlayerAllowFlight", client.player::isAllowFlight, v -> {
            });
            ImGuiEx.bool("On Ground:", () -> client.player.onGround);
            ImGuiEx.bool("Colliding:", () -> client.player.isColliding);
            ImGuiEx.bool("Colliding X:", () -> client.player.isCollidingX);
            ImGuiEx.bool("Colliding Y:", () -> client.player.isCollidingY);
            ImGuiEx.bool("Colliding Z:", () -> client.player.isCollidingZ);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerX", client.player::getX, v -> client.player.setX(v));
                ImGuiEx.editDouble("Y:", "PlayerY", client.player::getY, v -> client.player.setY(v));
                ImGuiEx.editDouble("Z:", "PlayerZ", client.player::getZ, v -> client.player.setZ(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Velocity")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerVelocityX", () -> client.player.velocityX, v -> client.player.velocityX = v);
                ImGuiEx.editDouble("Y:", "PlayerVelocityY", () -> client.player.velocityY, v -> client.player.velocityY = v);
                ImGuiEx.editDouble("Z:", "PlayerVelocityZ", () -> client.player.velocityZ, v -> client.player.velocityZ = v);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Rotation")) {
                ImGui.treePush();
                ImGuiEx.editFloat("X:", "PlayerXRot", client.player::getXRot, v -> client.player.setXRot(v));
                ImGuiEx.editFloat("Y:", "PlayerYRot", client.player::getYRot, v -> client.player.setYRot(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Player Input")) {
                ImGui.treePush();
                ImGuiEx.bool("Forward", () -> client.playerInput.forward);
                ImGuiEx.bool("Backward", () -> client.playerInput.backward);
                ImGuiEx.bool("Left", () -> client.playerInput.strafeLeft);
                ImGuiEx.bool("Right", () -> client.playerInput.strafeRight);
                ImGuiEx.bool("Up", () -> client.playerInput.up);
                ImGuiEx.bool("Down", () -> client.playerInput.down);
                ImGui.treePop();
            }
            ImGui.end();
        }
    }

    private static void showGuiEditor(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("GUI Editor", ImGuiOverlay.getDefaultFlags())) {
            ImGuiOverlay.guiEditor.render(client);
        }
        ImGui.end();
    }

    private static void showUtils(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.slider("FOV", "GameFOV", (int) client.camera.fieldOfView, 10, 150, i -> client.camera.fieldOfView = i);
        }
        ImGui.end();
    }

    private static int getDefaultFlags() {
        boolean cursorCaught = Gdx.input.isCursorCatched();
        var flags = ImGuiWindowFlags.None;
        if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
        return flags;
    }

    public static boolean isShown() {
        return ImGuiOverlay.SHOW_IM_GUI.get();
    }

    public static void setShowingImGui(boolean value) {
        ImGuiOverlay.SHOW_IM_GUI.set(value);
    }

    public static boolean isProfilerShown() {
        return ImGuiOverlay.SHOW_PROFILER.get();
    }

    public static void dispose() {
        synchronized (ImGuiOverlay.class) {
            if (ImGuiOverlay.isImplCreated) {
                ImGuiOverlay.imGuiGl3.dispose();
                ImGuiOverlay.imGuiGlfw.dispose();
            }

            if (ImGuiOverlay.isContextCreated) {
                ImGui.destroyContext();
                ImPlot.destroyContext(ImGuiOverlay.imPlotCtx);
            }
        }
    }

    public static boolean isImGuiHovered() {
        return hovered;
    }
}
