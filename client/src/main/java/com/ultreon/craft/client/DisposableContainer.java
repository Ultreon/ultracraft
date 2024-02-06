package com.ultreon.craft.client;

import com.badlogic.gdx.utils.Disposable;

public interface DisposableContainer extends Disposable {
    <T extends Disposable> T deferDispose(T disposable);
}
