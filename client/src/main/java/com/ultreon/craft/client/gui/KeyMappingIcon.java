package com.ultreon.craft.client.gui;

import com.badlogic.gdx.Input;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.key.KeyBind;
import com.ultreon.craft.util.ElementID;

public enum KeyMappingIcon {
    ESC(0, 0),
    F1(16, 0),
    F2(32, 0),
    F3(48, 0),
    F4(64, 0),
    F5(80, 0),
    F6(96, 0),
    F7(112, 0),
    F8(128, 0),
    F9(144, 0),
    F10(160, 0),
    F11(176, 0),
    F12(192, 0),
    TILDE(208, 0),
    EXCLAMATION(224, 0),
    AT(240, 0),
    HASH(256, 0),
    KEY_1(0, 16),
    KEY_2(16, 16),
    KEY_3(32, 16),
    KEY_4(48, 16),
    KEY_5(64, 16),
    KEY_6(80, 16),
    KEY_7(96, 16),
    KEY_8(112, 16),
    KEY_9(128, 16),
    KEY_0(144, 16),
    MINUS(160, 16),
    PLUS(176, 16),
    EQUALS(192, 16),
    UNDERSCORE(208, 16),
    BROKEN_BAR(224, 16),
    BACKSPACE(240, 16, 32, 16),
    Q(0, 32),
    W(16, 32),
    E(32, 32),
    R(48, 32),
    T(64, 32),
    Y(80, 32),
    U(96, 32),
    I(112, 32),
    O(128, 32),
    P(144, 32),
    LEFT_BRACKET(160, 32),
    RIGHT_BRACKET(176, 32),
    LEFT_CURLY(192, 32),
    RIGHT_CURLY(208, 32),
    BACKSLASH(224, 32),
    ENTER(240, 32, 32, 32),
    A(0, 48),
    S(16, 48),
    D(32, 48),
    F(48, 48),
    G(64, 48),
    H(80, 48),
    J(96, 48),
    K(112, 48),
    L(128, 48),
    QUOTE(144, 48),
    DOUBLE_QUOTE(160, 48),
    COLON(176, 48),
    SEMICOLON(192, 48),
    ASTERISK(208, 48),
    SPACE_SMALL(0, 64),
    WINDOWS(16, 64),
    Z(32, 64),
    X(48, 64),
    C(64, 64),
    V(80, 64),
    B(96, 64),
    N(112, 64),
    M(128, 64),
    LESS(144, 64),
    GREATER(160, 64),
    QUESTION(176, 64),
    SLASH(192, 64),
    UP(208, 64),
    RIGHT(224, 64),
    DOWN(240, 64),
    LEFT(256, 64),
    ALT(0, 80, 23, 16),
    TAB(32, 80, 23, 16),
    DELETE(64, 80, 23, 16),
    END(96, 80, 23, 16),
    NUM_LOCK(128, 80, 23, 16),
    PERIOD(160, 80),
    DOLLAR(176, 80),
    PERCENT(192, 80),
    CIRCUMFLEX(208, 80),
    CENT(224, 80),
    LEFT_PARENTHESIS(240, 80),
    RIGHT_PARENTHESIS(256, 80),
    CTRL(0, 96, 27, 16),
    CAPS(32, 96, 27, 16),
    HOME(64, 96, 27, 16),
    PAGE_UP(96, 96, 27, 16),
    PAGE_DOWN(128, 96, 27, 16),
    COMMA(160, 96),
    ENLARGE(176, 96),
    EMPTY(192, 96),
    RECORD(208, 96),
    SPACE_BIG(224, 96, 48, 16),
    SHIFT(0, 112, 33, 16),
    INSERT(32, 112, 33, 16),
    PRINT(64, 112, 33, 16),
    SCROLL_LOCK(96, 112, 33, 16),
    PAUSE_BREAK(128, 112, 3, 16),
    PLAY(160, 112),
    PAUSE(176, 112),
    STOP(192, 112),
    FAST_BACKWARD(208, 112),
    FAST_FORWARD(224, 112),
    PREVIOUS(240, 112),
    NEXT(256, 112),
    MOUSE(0, 128),
    MOUSE_LEFT(16, -1),
    MOUSE_RIGHT(32, -1),
    MOUSE_MIDDLE(48, -1),
    MOUSE_SCROLL_UP(64, -1),
    MOUSE_SCROLL_DOWN(80, -1),
    MOUSE_SCROLL(96, -1),
    POWER;

    public static final ElementID TEXTURE = UltracraftClient.id("textures/gui/input_icons.png");
    public final int u;
    public final int v;
    public final int width;
    public final int height;

    KeyMappingIcon() {
        this(0, 0);
    }

    KeyMappingIcon(int u, int v) {
        this(u, v, 16, 16);
    }

    KeyMappingIcon(int u, int v, int width, int height) {
        this.u = u + 272;
        this.v = v + 128;
        this.width = width;
        this.height = height;
    }

    public static KeyMappingIcon byChar(char c) {
        return switch (c) {
            case '0' -> KEY_0;
            case '1' -> KEY_1;
            case '2' -> KEY_2;
            case '3' -> KEY_3;
            case '4' -> KEY_4;
            case '5' -> KEY_5;
            case '6' -> KEY_6;
            case '7' -> KEY_7;
            case '8' -> KEY_8;
            case '9' -> KEY_9;
            case 'a' -> A;
            case 'b' -> B;
            case 'c' -> C;
            case 'd' -> D;
            case 'e' -> E;
            case 'f' -> F;
            case 'g' -> G;
            case 'h' -> H;
            case 'i' -> I;
            case 'j' -> J;
            case 'k' -> K;
            case 'l' -> L;
            case 'm' -> M;
            case 'n' -> N;
            case 'o' -> O;
            case 'p' -> P;
            case 'q' -> Q;
            case 'r' -> R;
            case 's' -> S;
            case 't' -> T;
            case 'u' -> U;
            case 'v' -> V;
            case 'w' -> W;
            case 'x' -> X;
            case 'y' -> Y;
            case 'z' -> Z;
            case '~' -> TILDE;
            case '-' -> MINUS;
            case '=' -> EQUALS;
            case '[' -> LEFT_BRACKET;
            case ']' -> RIGHT_BRACKET;
            case '\\' -> BACKSLASH;
            case ':' -> COLON;
            case ';' -> SEMICOLON;
            case '\'' -> QUOTE;
            case '"' -> DOUBLE_QUOTE;
            case ',' -> COMMA;
            case '.' -> PERIOD;
            case '<' -> LESS;
            case '>' -> GREATER;
            case '/' -> SLASH;
            case '?' -> QUESTION;
            case ' ' -> SPACE_BIG;
            case '\0' -> CTRL;
            case '\3' -> CAPS;
            case '\5', '\6' -> SHIFT;
            case '\7' -> ENLARGE;
            case '\b' -> BACKSPACE;
            case '\t' -> TAB;
            case '\n', '\r' -> ENTER;
            default -> EMPTY;
        };
    }

    public void render(Renderer renderer, int x, int y, boolean focused) {
        if (this == POWER) {
            renderer.blit(TEXTURE, x, y, 16, 16, 240, focused ? 320 : 336, 16, 16, 544, 384);
            return;
        }

        if (v == 127) {
            renderer.blit(TEXTURE, x, y, 16, 16, u - 272 + 128, focused ? 32 : 48, width, height, 544, 384);
            return;
        }

        renderer.blit(TEXTURE, x, y, width, height, u, focused ? v - 128 : v, width, height, 544, 384);
    }

    public static KeyMappingIcon byKey(KeyBind.Type type, int keyCode) {
        if (type == KeyBind.Type.MOUSE) {
            return switch (keyCode) {
                case Input.Buttons.LEFT -> MOUSE_LEFT;
                case Input.Buttons.RIGHT -> MOUSE_RIGHT;
                case Input.Buttons.MIDDLE -> MOUSE_MIDDLE;
                default -> MOUSE;
            };
        }
        if (type != KeyBind.Type.KEY) {
            return null;
        }

        return switch (keyCode) {
            case Input.Keys.ESCAPE -> ESC;
            case Input.Keys.F1 -> F1;
            case Input.Keys.F2 -> F2;
            case Input.Keys.F3 -> F3;
            case Input.Keys.F4 -> F4;
            case Input.Keys.F5 -> F5;
            case Input.Keys.F6 -> F6;
            case Input.Keys.F7 -> F7;
            case Input.Keys.F8 -> F8;
            case Input.Keys.F9 -> F9;
            case Input.Keys.F10 -> F10;
            case Input.Keys.F11 -> F11;
            case Input.Keys.F12 -> F12;
            case Input.Keys.GRAVE -> TILDE;
            case Input.Keys.NUM_1 -> KEY_1;
            case Input.Keys.NUM_2 -> KEY_2;
            case Input.Keys.NUM_3 -> KEY_3;
            case Input.Keys.NUM_4 -> KEY_4;
            case Input.Keys.NUM_5 -> KEY_5;
            case Input.Keys.NUM_6 -> KEY_6;
            case Input.Keys.NUM_7 -> KEY_7;
            case Input.Keys.NUM_8 -> KEY_8;
            case Input.Keys.NUM_9 -> KEY_9;
            case Input.Keys.NUM_0 -> KEY_0;
            case Input.Keys.MINUS -> MINUS;
            case Input.Keys.EQUALS -> EQUALS;
            case Input.Keys.BACKSPACE -> BACKSPACE;
            case Input.Keys.TAB -> TAB;
            case Input.Keys.INSERT -> INSERT;
            case Input.Keys.FORWARD_DEL -> DELETE;
            case Input.Keys.RIGHT -> RIGHT;
            case Input.Keys.LEFT -> LEFT;
            case Input.Keys.DOWN -> DOWN;
            case Input.Keys.UP -> UP;
            case Input.Keys.PAGE_UP -> PAGE_UP;
            case Input.Keys.PAGE_DOWN -> PAGE_DOWN;
            case Input.Keys.HOME -> HOME;
            case Input.Keys.END -> END;
            case Input.Keys.CAPS_LOCK -> CAPS;
            case Input.Keys.SCROLL_LOCK -> SCROLL_LOCK;
            case Input.Keys.NUM_LOCK -> NUM_LOCK;
            case Input.Keys.PRINT_SCREEN -> PRINT;
            case Input.Keys.PAUSE -> PAUSE;
            case Input.Keys.BACKSLASH -> BACKSLASH;
            case Input.Keys.LEFT_BRACKET -> LEFT_BRACKET;
            case Input.Keys.RIGHT_BRACKET -> RIGHT_BRACKET;
            case Input.Keys.SEMICOLON -> SEMICOLON;
            case Input.Keys.COMMA -> COMMA;
            case Input.Keys.PERIOD -> PERIOD;
            case Input.Keys.SLASH -> SLASH;
            case Input.Keys.SPACE -> SPACE_SMALL;
            case Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT -> SHIFT;
            case Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> CTRL;
            case Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT -> ALT;
            case Input.Keys.A -> A;
            case Input.Keys.B -> B;
            case Input.Keys.C -> C;
            case Input.Keys.D -> D;
            case Input.Keys.E -> E;
            case Input.Keys.F -> F;
            case Input.Keys.G -> G;
            case Input.Keys.H -> H;
            case Input.Keys.I -> I;
            case Input.Keys.J -> J;
            case Input.Keys.K -> K;
            case Input.Keys.L -> L;
            case Input.Keys.M -> M;
            case Input.Keys.N -> N;
            case Input.Keys.O -> O;
            case Input.Keys.P -> P;
            case Input.Keys.Q -> Q;
            case Input.Keys.R -> R;
            case Input.Keys.S -> S;
            case Input.Keys.T -> T;
            case Input.Keys.U -> U;
            case Input.Keys.V -> V;
            case Input.Keys.W -> W;
            case Input.Keys.X -> X;
            case Input.Keys.Y -> Y;
            case Input.Keys.Z -> Z;
            case Input.Keys.ENTER -> ENTER;
            default -> EMPTY;
        };
    }

    public ElementID getTexture() {
        return UltracraftClient.id("textures/gui/icons.png");
    }
}
