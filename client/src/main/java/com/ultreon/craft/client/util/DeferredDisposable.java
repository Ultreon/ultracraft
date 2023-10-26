package com.ultreon.craft.client.util;

import com.badlogic.gdx.utils.Disposable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public interface DeferredDisposable extends Disposable {
    @CanIgnoreReturnValue
    <T extends Disposable> T deferDispose(T disposable);
}
