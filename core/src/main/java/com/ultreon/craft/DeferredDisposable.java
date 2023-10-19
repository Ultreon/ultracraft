package com.ultreon.craft;

import com.badlogic.gdx.utils.Disposable;

public interface DeferredDisposable extends Disposable {
    <T extends Disposable> T deferDispose(T disposable);
}
