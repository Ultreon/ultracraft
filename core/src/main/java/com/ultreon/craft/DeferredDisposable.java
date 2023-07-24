package com.ultreon.craft;

import com.badlogic.gdx.utils.Disposable;

public interface DeferredDisposable extends Disposable {
    void deferDispose(Disposable disposable);
}
