package com.ultreon.craft.client.util;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.function.Consumer;

public class RenderableArray extends Array<Renderable> {
    private Vector3 translation;
    private static int globalSize = 0;

    public RenderableArray() {
    }

    public RenderableArray(int capacity) {
        super(capacity);
    }

    public RenderableArray(boolean ordered, int capacity) {
        super(ordered, capacity);
    }

    public RenderableArray(boolean ordered, int capacity, Class arrayType) {
        super(ordered, capacity, arrayType);
    }

    public RenderableArray(Class arrayType) {
        super(arrayType);
    }

    public RenderableArray(Array<? extends Renderable> array) {
        super(array);
    }

    public RenderableArray(Renderable[] array) {
        super(array);
    }

    public RenderableArray(boolean ordered, Renderable[] array, int start, int count) {
        super(ordered, array, start, count);
    }

    public static int getGlobalSize() {
        return globalSize;
    }

    @Override
    public void add(Renderable value) {
        if (translation != null) {
            value.worldTransform.setToTranslation(translation);
        }
        globalSize++;
        super.add(value);
    }

    @Override
    public void add(Renderable value1, Renderable value2) {
        if (translation != null) {
            value1.worldTransform.setToTranslation(translation);
            value2.worldTransform.setToTranslation(translation);
        }
        globalSize += 2;
        super.add(value1, value2);
    }

    @Override
    public void add(Renderable value1, Renderable value2, Renderable value3) {
        if (translation != null) {
            value1.worldTransform.setToTranslation(translation);
            value2.worldTransform.setToTranslation(translation);
            value3.worldTransform.setToTranslation(translation);
        }
        globalSize += 3;
        super.add(value1, value2, value3);
    }

    @Override
    public void add(Renderable value1, Renderable value2, Renderable value3, Renderable value4) {
        if (translation != null) {
            value1.worldTransform.setToTranslation(translation);
            value2.worldTransform.setToTranslation(translation);
            value3.worldTransform.setToTranslation(translation);
            value4.worldTransform.setToTranslation(translation);
        }
        globalSize += 4;
        super.add(value1, value2, value3, value4);
    }

    @Override
    public void addAll(Array<? extends Renderable> array) {
        if (translation != null) {
            for (Renderable renderable : array.items) {
                renderable.worldTransform.setToTranslation(translation);
            }
        }
        globalSize += array.size;
        super.addAll(array);
    }

    @Override
    public void addAll(Array<? extends Renderable> array, int start, int count) {
        if (translation != null) {
            for (Renderable renderable : array.items) {
                renderable.worldTransform.setToTranslation(translation);
            }
        }
        globalSize += count;
        super.addAll(array, start, count);
    }

    @Override
    public void addAll(Renderable... array) {
        if (translation != null) {
            for (Renderable renderable : array) {
                renderable.worldTransform.setToTranslation(translation);
            }
        }
        globalSize += array.length;
        super.addAll(array);
    }

    @Override
    public void addAll(Renderable[] array, int start, int count) {
        if (translation != null) {
            for (Renderable renderable : array) {
                renderable.worldTransform.setToTranslation(translation);
            }
        }
        globalSize += count;
        super.addAll(array, start, count);
    }

    @Override
    public void set(int index, Renderable value) {
        if (translation != null) {
            value.worldTransform.setToTranslation(translation);
        }

        if (index == size) {
            globalSize++;
        }
        super.set(index, value);
    }

    public void setTranslation(Vector3 translation) {
        this.translation = translation;

        for (Renderable renderable : this) {
            renderable.worldTransform.setToTranslation(translation);
        }
    }

    @Override
    public void clear() {
        globalSize -= size;
        super.clear();
    }

    public void transform(Consumer<Matrix4> value) {
        for (Renderable renderable : this) {
            value.accept(renderable.worldTransform);
        }
    }
}
