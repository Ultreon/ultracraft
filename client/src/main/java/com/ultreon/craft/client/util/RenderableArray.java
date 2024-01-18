package com.ultreon.craft.client.util;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g3d.Renderable;

public class RenderableArray extends Array<Renderable> {
    private Environment defaultEnvironment;

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

    public void setDefaultEnvironment(Environment environment) {
        this.defaultEnvironment = environment;
    }

    public Environment getDefaultEnvironment() {
        return defaultEnvironment;
    }

    @Override
    public void add(Renderable value) {
        if (defaultEnvironment != null) {
            value.environment = defaultEnvironment;
        }
        super.add(value);
    }

    @Override
    public void add(Renderable value1, Renderable value2) {
        if (defaultEnvironment != null) {
            value1.environment = defaultEnvironment;
            value2.environment = defaultEnvironment;
        }
        super.add(value1, value2);
    }

    @Override
    public void add(Renderable value1, Renderable value2, Renderable value3) {
        if (defaultEnvironment != null) {
            value1.environment = defaultEnvironment;
            value2.environment = defaultEnvironment;
            value3.environment = defaultEnvironment;
        }
        super.add(value1, value2, value3);
    }

    @Override
    public void add(Renderable value1, Renderable value2, Renderable value3, Renderable value4) {
        if (defaultEnvironment != null) {
            value1.environment = defaultEnvironment;
            value2.environment = defaultEnvironment;
            value3.environment = defaultEnvironment;
            value4.environment = defaultEnvironment;
        }
        super.add(value1, value2, value3, value4);
    }

    @Override
    public void addAll(Array<? extends Renderable> array) {
        if (defaultEnvironment != null) {
            for (Renderable renderable : array.items) {
                renderable.environment = defaultEnvironment;
            }
        }
        super.addAll(array);
    }

    @Override
    public void addAll(Array<? extends Renderable> array, int start, int count) {
        if (defaultEnvironment != null) {
            for (Renderable renderable : array.items) {
                renderable.environment = defaultEnvironment;
            }
        }
        super.addAll(array, start, count);
    }

    @Override
    public void addAll(Renderable... array) {
        if (defaultEnvironment != null) {
            for (Renderable renderable : array) {
                renderable.environment = defaultEnvironment;
            }
        }
        super.addAll(array);
    }

    @Override
    public void addAll(Renderable[] array, int start, int count) {
        if (defaultEnvironment != null) {
            for (Renderable renderable : array) {
                renderable.environment = defaultEnvironment;
            }
        }
        super.addAll(array, start, count);
    }

    @Override
    public void set(int index, Renderable value) {
        if (defaultEnvironment != null) {
            value.environment = defaultEnvironment;
        }
        super.set(index, value);
    }
}
