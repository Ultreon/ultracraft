package com.ultreon.craft.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.google.common.collect.Queues;
import com.ultreon.craft.util.Utils;

import java.util.Deque;
import java.util.function.Consumer;

public class MatrixStack {
    private final Deque<Matrix4> stack;
    public Consumer<Matrix4> onPush = matrix -> {};
    public Consumer<Matrix4> onPop = matrix -> {};

    public MatrixStack() {
        stack = Utils.make(Queues.newArrayDeque(), matrixDeque -> matrixDeque.add(new Matrix4()));
    }

    public MatrixStack(Matrix4 origin) {
        stack = Utils.make(Queues.newArrayDeque(), matrixDeque -> matrixDeque.add(origin));
    }

    public void push() {
        var matrix = stack.getLast();
        stack.addLast(new Matrix4(matrix));
        onPush.accept(stack.getLast());
    }

    public void pop() {
        stack.removeLast();
        onPush.accept(stack.getLast());
    }

    public void translate(double x, double y) {
        this.translate((float) x, (float) y);
    }

    public void translate(float x, float y) {
        Matrix4 matrix = stack.getLast();
        matrix.translate(x, y, 0);
    }

    public void scale(float x, float y) {
        Matrix4 matrix = stack.getLast();
        matrix.scale(x, y, 0);
    }
    
    public void rotate(Quaternion quaternion) {
        var matrix = stack.getLast();
        matrix.rotate(quaternion);
    }

    public Matrix4 last() {
        return stack.getLast();
    }

    public boolean isClear() {
        return this.stack.size() == 1;
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
