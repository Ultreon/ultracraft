package com.ultreon.craft.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.ultreon.craft.util.Color;

import java.util.ArrayDeque;
import java.util.Deque;

public class GlStateStack {
    private final Deque<GlState> states = new ArrayDeque<>();
    private GlState top = null;
    
    public void begin() {
        if (this.top != null)
            throw new IllegalStateException("Already in a state!");
        
        this.top = new GlState();
    }
    
    public void end() {
        if (this.top == null)
            throw new IllegalStateException("Not in a state!");
        
        if (!this.states.isEmpty())
            throw new IllegalStateException("State stack isn't empty!");
        
        this.top = null;
        this.states.pop();
    }
    
    public void push() {
        if (this.top == null)
            throw new IllegalStateException("Not in a state!");
        
        this.states.push(this.top);
        this.top = new GlState(this.top);
    }

    public void enableBlending() {
        this.top.blend = true;
        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    public void disableBlending() {
        this.top.blend = false;
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void blendFunc(int srcFactor, int dstFactor) {
        this.top.blendSrc = srcFactor;
        this.top.blendDst = dstFactor;

        Gdx.gl.glBlendFunc(this.top.blendSrc, this.top.blendDst);
    }

    public void blendFuncSeparate(int srcFactor, int dstFactor, int srcAlpha, int dstAlpha) {
        this.top.blendSrc = srcFactor;
        this.top.blendDst = dstFactor;
        this.top.blendSrcAlpha = srcAlpha;
        this.top.blendDstAlpha = dstAlpha;

        Gdx.gl.glBlendFuncSeparate(this.top.blendSrc, this.top.blendDst, this.top.blendSrcAlpha, this.top.blendDstAlpha);
    }

    public void blendEquationSeparate(int modeRGB, int modeAlpha) {
        this.top.blendEquation = modeRGB;
        this.top.blendEquationAlpha = modeAlpha;

        Gdx.gl.glBlendEquationSeparate(this.top.blendEquation, this.top.blendEquationAlpha);
    }

    public void enableDepthTest() {
        this.top.depthTest = true;
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    public void disableDepthTest() {
        this.top.depthTest = false;
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }
    public static class GlState {
        public float lineWidth;
        public boolean blend;
        public int blendSrc;
        public int blendDst;
        public int blendEquation;
        public Color blendColor;
        public int blendSrcAlpha;
        public int blendDstAlpha;
        public int blendEquationAlpha;
        public boolean depthMask;
        public boolean depthTest;
        public int depthFunc;
        public boolean cullFace;
        
        public GlState(GlState original) {
            this.lineWidth = original.lineWidth;
            this.blend = original.blend;
            this.blendSrc = original.blendSrc;
            this.blendDst = original.blendDst;
            this.blendEquation = original.blendEquation;
            this.blendColor = original.blendColor;
            this.blendSrcAlpha = original.blendSrcAlpha;
            this.blendDstAlpha = original.blendDstAlpha;
            this.blendEquationAlpha = original.blendEquationAlpha;
            this.depthMask = original.depthMask;
            this.depthTest = original.depthTest;
            this.depthFunc = original.depthFunc;
            this.cullFace = original.cullFace;
        }
        
        public GlState() {
            this.lineWidth = 1.0F;
            this.blend = false;
            this.blendSrc = GL20.GL_SRC_ALPHA;
            this.blendDst = GL20.GL_ONE_MINUS_SRC_ALPHA;
            this.blendEquation = GL20.GL_FUNC_ADD;
            this.blendColor = Color.WHITE;
            this.blendSrcAlpha = GL20.GL_SRC_ALPHA;
            this.blendDstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
            this.blendEquationAlpha = GL20.GL_FUNC_ADD;
            this.depthMask = true;
            this.depthTest = true;
            this.depthFunc = GL20.GL_LEQUAL;
            this.cullFace = false;
            
            Gdx.gl.glLineWidth(this.lineWidth);
        }
        
        public void apply() {
            Gdx.gl.glLineWidth(this.lineWidth);
            
            if (this.blend) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(this.blendSrc, this.blendDst);
                Gdx.gl.glBlendEquationSeparate(this.blendEquation, this.blendEquationAlpha);
                Gdx.gl.glBlendColor(this.blendColor.getRed(), this.blendColor.getGreen(), this.blendColor.getBlue(), this.blendColor.getAlpha());
            } else
                Gdx.gl.glDisable(GL20.GL_BLEND);
            
            if (this.depthTest) {
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(this.depthMask);
                Gdx.gl.glDepthFunc(this.depthFunc);
            } else
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            
            if (this.cullFace)
                Gdx.gl.glEnable(GL20.GL_CULL_FACE);
            else
                Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        }
    }
}
