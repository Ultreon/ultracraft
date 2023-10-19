package com.ultreon.craft.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Pool;
import com.google.common.collect.Queues;
import com.ultreon.craft.util.Utils;

import java.util.Deque;
import java.util.function.Consumer;

public class MatrixStack {
    private final Pool<Matrix4> pool = new Pool<>() {
        @Override
        protected Matrix4 newObject() {
            return new Matrix4();
        }
    };

    private final Deque<Matrix4> stack;
    public Consumer<Matrix4> onPush = matrix -> {};
    public Consumer<Matrix4> onPop = matrix -> {};
    public Consumer<Matrix4> onEdit = matrix -> {};

    public MatrixStack() {
        this.stack = Utils.make(Queues.newArrayDeque(), matrixDeque -> matrixDeque.add(this.pool.obtain().idt()));
    }

    public MatrixStack(Matrix4 origin) {
        this.stack = Utils.make(Queues.newArrayDeque(), matrixDeque -> matrixDeque.add(origin));
    }

    public void clean() {
    }

    public void push() {
        this.stack.addLast(this.stack.getLast().cpy());
        this.onEdit.accept(this.stack.getLast());
    }

    public void pop() {
        this.stack.removeLast().idt();
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
        Matrix4 matrix = this.stack.getLast();
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
