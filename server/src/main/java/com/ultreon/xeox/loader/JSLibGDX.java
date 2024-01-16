package com.ultreon.xeox.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

/**
 * LibGDX JS wrapper for XeoxJS.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * 
 */
public class JSLibGDX {
    public final Input input = Gdx.input;
    public final Graphics graphics = Gdx.graphics;
    public final GL20 gl = Gdx.gl;
    public final GL20 gl20 = Gdx.gl20;
    public final GL30 gl30 = Gdx.gl30;
}
