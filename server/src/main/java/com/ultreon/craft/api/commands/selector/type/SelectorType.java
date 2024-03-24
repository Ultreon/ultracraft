package com.ultreon.craft.api.commands.selector.type;

public abstract class SelectorType {
  private final Object value;

  public SelectorType(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return this.value;
  }
}