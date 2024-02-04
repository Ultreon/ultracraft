package com.ultreon.craft.client.gui.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.debug.inspect.InspectionNode;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class InspectorDebugPage implements DebugPage {
    private String inspectCurrentPath = "/";
    private String inspectIdxInput = "";

    @Override
    public void render(DebugRenderContext context) {
        String path = this.inspectCurrentPath;

        Comparator<InspectionNode<?>> comparator = Comparator.comparing(InspectionNode::getName);

        context.entryLine(TextObject.literal(this.inspectIdxInput).setColor(Color.WHITE))
                .entryLine(TextObject.literal(path).setColor(Color.AZURE).setBold(true).setUnderlined(true))
                .entryLine();

        if (this.renderNodes(context, path, comparator)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) this.handleEnterKey(context, path, comparator);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) this.handleBackspaceKey(path);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) this.handleReset();
        else this.handleNumberKey();
    }

    private void handleReset() {
        this.inspectIdxInput = "";
    }

    private void handleNumberKey() {
        for (int num = 0; num < 10; num++) {
            int key = Input.Keys.NUMPAD_0 + num;
            if (Gdx.input.isKeyJustPressed(key)) {
                this.inspectIdxInput += String.valueOf(num);
                break;
            }
        }
    }

    private void handleBackspaceKey(String path) {
        if (this.inspectCurrentPath.equals("/")) {
            return;
        }

        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        this.inspectCurrentPath = path.substring(0, path.lastIndexOf("/")) + '/';
    }

    private void handleEnterKey(DebugRenderContext context, String path, Comparator<InspectionNode<?>> comparator) {
        String input = this.inspectIdxInput;
        try {
            int idx = Integer.parseInt(input);

            @Nullable InspectionNode<?> node = context.client().inspection.getNode(path);
            if (node == null) {
                this.inspectCurrentPath = "/";
                return;
            }
            List<InspectionNode<?>> nodes = node.getNodes().values().stream().sorted(comparator).toList();

            if (nodes.isEmpty()) return;

            if (idx >= 0 && idx < nodes.size()) {
                path += nodes.get(idx).getName() + "/";
                this.inspectCurrentPath = path;
            }
            this.handleReset();
        } catch (NumberFormatException ignored) {
            this.handleReset();
        }
    }

    private boolean renderNodes(DebugRenderContext context, String path, Comparator<InspectionNode<?>> comparator) {
        @Nullable InspectionNode<?> node = context.client().inspection.getNode(path);
        if (node == null) {
            this.inspectCurrentPath = "/";
            return true;
        }

        List<InspectionNode<?>> nodes = node.getNodes().values().stream().sorted(comparator).toList();
        for (int i = 0, nodeSize = nodes.size(); i < nodeSize; i++) {
            InspectionNode<?> curNode = nodes.get(i);
            context.entryLine(i, curNode.getName());
        }

        List<Pair<String, String>> elements = node.getElements().entrySet().stream().map(t -> new Pair<>(t.getKey(), t.getValue().get())).sorted(Comparator.comparing(Pair::getFirst)).toList();
        for (Pair<String, String> element : elements) {
            context.entryLine(element.getFirst(), element.getSecond());
        }
        return false;
    }
}
