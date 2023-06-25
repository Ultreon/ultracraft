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
    public Consumer<Matrix4> onEdit = matrix -> {};

    public MatrixStack() {
        this.stack = Utils.make(Queues.newArrayDeque(), matrixDeque -> matrixDeque.add(new Matrix4()));
    }

    public MatrixStack(Matrix4 origin) {
        this.stack = Utils.make(Queues.newArrayDeque(), matrixDeque -> matrixDeque.add(origin));
    }

    public void push() {
        this.stack.addLast(this.stack.getLast().cpy());
        this.onEdit.accept(this.stack.getLast());
    }

    public void pop() {
        this.stack.removeLast();
        this.onEdit.accept(this.stack.getLast());
    }

    public void translate(double x, double y) {
        this.translate((float) x, (float) y);
    }

    public void translate(float x, float y) {
        Matrix4 matrix = this.stack.getLast();
        matrix.translate(x, y, 0);
        this.onEdit.accept(matrix);
    }

    public void translate(float x, float y, float z) {
        Matrix4 matrix = this.stack.getLast();
        matrix.translate(x, y, z);
        this.onEdit.accept(matrix);
    }

    public void scale(float x, float y) {
        Matrix4 matrix = this.stack.getLast();
        matrix.scale(x, y, 0);
        this.onEdit.accept(matrix);
    }
    
    public void rotate(Quaternion quaternion) {
        var matrix = this.stack.getLast();
        matrix.rotate(quaternion);
        this.onEdit.accept(matrix);
    }

    public Matrix4 last() {
        return this.stack.getLast();
    }

    public boolean isClear() {
        return this.stack.size() == 1;
    }

    @Override
    public String toString() {
        return this.stack.toString();
    }
}
