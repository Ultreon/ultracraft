package com.ultreon.craft;

public class DummyApplicationFrame implements ApplicationFrame {

    @Override
    public long getHandle() {
        return -1L;
    }

    @Override
    public void close() {

    }

    @Override
    public void setVisible(boolean visible) {

    }

    @Override
    public void requestAttention() {

    }

    @Override
    public boolean isHovered() {
        return true;
    }
}
