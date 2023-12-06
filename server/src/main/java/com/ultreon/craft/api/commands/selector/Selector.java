package com.ultreon.craft.api.commands.selector;

public class Selector extends BaseSelector<String> {

    public Selector(String text) {
        super(text);
        this.result = this.calculateData();
    }

    @Override
    public Result<String> calculateData() {
        return new Result<>(this.stringValue, null);
    }
}