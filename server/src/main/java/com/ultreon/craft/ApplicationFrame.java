package com.ultreon.craft;

public interface ApplicationFrame {

    long getHandle();

    void close();

    void setVisible(boolean visible);

    void requestAttention();

    boolean isHovered();
}
