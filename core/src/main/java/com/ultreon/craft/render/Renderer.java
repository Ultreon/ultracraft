/////////////////////
//     Package     //
/////////////////////
package com.ultreon.craft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.craft.TextureManager;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.font.Font;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec4i;
import com.ultreon.libs.text.v0.TextObject;
import org.jetbrains.annotations.ApiStatus;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * Renderer class.
 *
 * @author XyperCode
 */
@SuppressWarnings("unused")
public class Renderer {
    ////////////////////
    //     Fields     //
    ////////////////////
    private final UltreonCraft game = UltreonCraft.get();
    private final Stack<Vector3> globalTranslation = new Stack<>();
    private final Batch batch;
    private final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private float strokeWidth = 1;
    private Texture curTexture;
    private Font font;
    private final MatrixStack matrixStack;
    private Color textureColor = Color.rgb(0xffffff);
    private final Vector2 tmp2A = new Vector2();
    private final Vector3 tmp3A = new Vector3();

    /**
     * @param shapes shape drawer instance from {@link UltreonCraft}
     */
    public Renderer(ShapeDrawer shapes) {
        this(shapes, new MatrixStack());
    }

    /**
     * @param shapes      shape drawer instance from {@link UltreonCraft}
     * @param matrixStack current matrix stack.
     */
    public Renderer(ShapeDrawer shapes, MatrixStack matrixStack) {
        this.globalTranslation.push(new Vector3());
        this.font = this.game.font;
        GL30 gl30 = Gdx.gl30;
        this.batch = shapes.getBatch();
        this.shapes = shapes;
        this.matrixStack = matrixStack;
        this.textureManager = this.game.getTextureManager();
        if (this.textureManager == null) throw new IllegalArgumentException("Texture manager isn't initialized yet!");

        // Projection matrix.
        this.matrixStack.onEdit = matrix -> shapes.getBatch().setTransformMatrix(matrix);
    }

    public MatrixStack getMatrixStack() {
        return this.matrixStack;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setColor(Color c) {
        if (c == null) return;
        if (this.font != null) this.font.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        this.shapes.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }

    public void setColor(int r, int g, int b) {
        this.setColor(Color.rgb(r, g, b));
    }

    public void setColor(float r, float g, float b) {
        this.setColor(Color.rgb(r, g, b));
    }

    public void setColor(int r, int g, int b, int a) {
        this.setColor(Color.rgba(r, g, b, a));
    }

    public void setColor(float r, float g, float b, float a) {
        this.setColor(Color.rgba(r, g, b, a));
    }

    public void setColor(int argb) {
        this.setColor(Color.argb(argb));
    }

    /**
     * Sets current color from a color hex.
     * Examples:
     * <code>
     * color("#f70")
     * color("#fff7")
     * color("#ffd500")
     * color("#aab70077")
     * </code>
     *
     * @param hex a color hex.
     */
    public void setColor(String hex) {
        this.setColor(Color.hex(hex));
    }

    public void clearColor(Color color) {
        Gdx.gl.glClearColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public void clearColor(int red, int green, int blue) {
        this.clearColor(Color.rgb(red, green, blue));
    }

    public void clearColor(float red, float green, float blue) {
        this.clearColor(Color.rgb(red, green, blue));
    }

    public void clearColor(int red, int green, int blue, int alpha) {
        this.clearColor(Color.rgba(red, green, blue, alpha));
    }

    public void clearColor(float red, float green, float blue, float alpha) {
        this.clearColor(Color.rgba(red, green, blue, alpha));
    }

    public void clearColor(int argb) {
        this.clearColor(Color.argb(argb));
    }

    public void clearColor(String hex) {
        this.clearColor(Color.hex(hex));
    }

    public void outline(Rectangle2D rect) {
        this.rectLine((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
    }

    public void outline(Ellipse2D ellipse) {
        this.ovalLine((float) ellipse.getX(), (float) ellipse.getY(), (float) ellipse.getWidth(), (float) ellipse.getHeight());
    }

    public void outline(Line2D s) {
        this.line((float) s.getX1(), (float) s.getY1(), (float) s.getX2(), (float) s.getY2());
    }

    public void circle(float x, float y, float radius) {
        this.shapes.filledCircle(x, y, radius);
    }

    public void circleLine(float x, float y, float radius) {
        this.shapes.circle(x, y, radius);
    }

    public void fill(Rectangle2D rect) {
        this.rect((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
    }

    public void fill(Ellipse2D ellipse) {
        this.oval((float) ellipse.getX(), (float) ellipse.getY(), (float) ellipse.getWidth(), (float) ellipse.getHeight());
    }

    public void fill(Line2D line) {
        this.line((float) line.getX1(), (float) line.getY1(), (float) line.getX2(), (float) line.getY2());
    }

    public void fill(Vec4i r) {
        this.rect(r.x, r.y, r.z, r.w);
    }

    public void line(int x1, int y1, int x2, int y2) {
        this.shapes.line(x1, y1, x2, y2);
    }

    public void line(float x1, float y1, float x2, float y2) {
        this.shapes.line(x1, y1, x2, y2);
    }

    public void rectLine(int x, int y, int width, int height) {
        this.shapes.rectangle(x + strokeWidth / 2f, y + strokeWidth / 2f, width - strokeWidth, height - strokeWidth, this.strokeWidth);
    }

    public void rectLine(float x, float y, float width, float height) {
        this.shapes.rectangle(x + strokeWidth / 2f, y + strokeWidth / 2f, width - strokeWidth, height - strokeWidth, this.strokeWidth);
    }

    public void rect(int x, int y, int width, int height) {
        this.shapes.filledRectangle(x, y, width, height);
    }

    public void rect(float x, float y, float width, float height) {
        this.shapes.filledRectangle(x, y, width, height);
    }

    public void roundRectLine(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        this.shapes.rectangle(x, y, width, height, this.strokeWidth);
    }

    public void roundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        this.shapes.rectangle(x, y, width, height);
    }

    public void rect3DLine(int x, int y, int width, int height, boolean raised) {
        this.shapes.rectangle(x, y, width, height, this.strokeWidth);
    }

    public void rect3D(int x, int y, int width, int height, boolean raised) {
        this.shapes.filledRectangle(x, y, width, height);
    }

    public void ovalLine(int x, int y, int width, int height) {
        this.shapes.ellipse(x, y, width, height);
    }

    public void oval(int x, int y, int width, int height) {
        this.shapes.filledEllipse(x, y, width, height);
    }

    public void ovalLine(float x, float y, float width, float height) {
        this.shapes.ellipse(x, y, width, height);
    }

    public void oval(float x, float y, float width, float height) {
        this.shapes.filledEllipse(x, y, width, height);
    }

    public void arcLine(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.shapes.arc(x, y, width, startAngle, arcAngle);
    }

    public void arc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.shapes.arc(x, y, width, startAngle, arcAngle);
    }

    ///////////////////
    //     Image     //
    ///////////////////
    public void blit(TextureRegion tex, float x, float y) {
        if (tex == null) tex = TextureManager.DEFAULT_TEX_REG;
        this.batch.setColor(this.textureColor.toGdx());
        this.batch.draw(tex, x, y + tex.getRegionHeight(), tex.getRegionWidth(), -tex.getRegionHeight());
    }

    public void blit(TextureRegion tex, float x, float y, float width, float height) {
        if (tex == null) tex = TextureManager.DEFAULT_TEX_REG;
        this.batch.setColor(this.textureColor.toGdx());
        this.batch.draw(tex, x, y + tex.getRegionHeight(), tex.getRegionWidth(), -tex.getRegionHeight());
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y) {
        this.batch.setColor(this.textureColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
    }


    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, tex.getWidth(), tex.getHeight());
        this.batch.setColor(this.textureColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, Color backgroundColor) {
        this.blit(tex, x, y, width, height, 0.0F, 0.0F, backgroundColor);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        this.blit(tex, x, y, width, height, u, v, width, height, backgroundColor);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        this.blit(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, width, height);
        this.batch.setColor(this.textureColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        this.batch.draw(textureRegion, x, y + height, width, -height);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height) {
        this.blit(tex, x, y, width, height, 0.0F, 0.0F);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, float u, float v) {
        this.blit(tex, x, y, width, height, u, v, width, height);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        this.blit(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
    }

    @ApiStatus.Internal
    public void blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        this.batch.setColor(this.textureColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, 1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        this.batch.draw(textureRegion, x, y + height, width, -height);
    }

    @Deprecated(forRemoval = true)
    public void blit(Identifier id, float x, float y) {
        this.batch.setColor(this.textureColor.toGdx());
        Texture tex = this.textureManager.getTexture(id);
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
    }


    @Deprecated(forRemoval = true)
    public void blit(Identifier id, float x, float y, Color backgroundColor) {
        this.setColor(backgroundColor);
        Texture tex = this.textureManager.getTexture(id);
        this.rect(x, y, tex.getWidth(), tex.getHeight());
        this.batch.setColor(this.textureColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
    }

    public void blit(Identifier id, float x, float y, float width, float height, Color backgroundColor) {
        this.blit(id, x, y, width, height, 0.0F, 0.0F, backgroundColor);
    }

    public void blit(Identifier id, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        Texture texture = this.textureManager.getTexture(id);
        this.blit(id, x, y, width, height, u, v, 256, 256, backgroundColor);
    }

    public void blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        Texture texture = this.textureManager.getTexture(id);
        this.blit(id, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
    }

    public void blit(Identifier id, float x, float y, float width, float height) {
        this.blit(id, x, y, width, height, 0.0F, 0.0F);
    }

    public void blit(Identifier id, float x, float y, float width, float height, float u, float v) {
        this.blit(id, x, y, width, height, u, v, width, height);
    }

    public void blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        this.blit(id, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
    }

    public void blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        this.batch.setColor(this.textureColor.toGdx());
        Texture tex = this.textureManager.getTexture(id);
        TextureRegion textureRegion = new TextureRegion(tex, 1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        this.batch.draw(textureRegion, x, y + height, width, -height);
    }

    public void blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, width, height);
        Texture tex = this.textureManager.getTexture(id);
        this.batch.setColor(this.textureColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        this.batch.draw(textureRegion, x, y + height, width, -height);
    }

    //////////////////
    //     Text     //

    //////////////////
    public void drawText(String text, int x, int y) {
        this.drawText(text, x, y, Color.WHITE);
    }

    public void drawText(String text, int x, int y, Color color) {
        this.drawText(text, x, y, color, true);
    }

    public void drawText(String text, int x, int y, boolean shadow) {
        this.drawText(text, x, y, Color.WHITE, shadow);
    }

    public void drawText(String text, int x, int y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
    }

    public void drawText(String text, float x, float y) {
        this.drawText(text, x, y, Color.WHITE);
    }

    public void drawText(String text, float x, float y, Color color) {
        this.drawText(text, x, y, color, true);
    }

    public void drawText(String text, float x, float y, boolean shadow) {
        this.drawText(text, x, y, Color.WHITE, shadow);
    }

    public void drawText(String text, float x, float y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
    }

    public void drawText(String text, float x, float y, float maxWidth, String truncate) {
        this.drawText(text, x, y, Color.WHITE, maxWidth, truncate);
    }

    public void drawText(String text, float x, float y, Color color, float maxWidth, String truncate) {
        this.drawText(text, x, y, color, true, maxWidth, truncate);
    }

    public void drawText(String text, float x, float y, boolean shadow, float maxWidth, String truncate) {
        this.drawText(text, x, y, Color.WHITE, shadow, maxWidth, truncate);
    }

    public void drawText(String text, float x, float y, Color color, boolean shadow, float maxWidth, String truncate) {
        this.font.drawText(this, text, x, y, color, shadow);
    }

    public void drawText(String text, float x, float y, float maxWidth, boolean wrap, String truncate) {
        this.drawText(text, x, y, Color.WHITE, maxWidth, wrap, truncate);
    }

    public void drawText(String text, float x, float y, Color color, float maxWidth, boolean wrap, String truncate) {
        this.drawText(text, x, y, color, true, maxWidth, wrap, truncate);
    }

    public void drawText(String text, float x, float y, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.drawText(text, x, y, Color.WHITE, shadow, maxWidth, wrap, truncate);
    }

    public void drawText(String text, float x, float y, Color color, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.font.drawText(this, text, x, y, color, shadow);
    }

    public void drawText(TextObject text, int x, int y) {
        this.drawText(text, x, y, Color.WHITE);
    }

    public void drawText(TextObject text, int x, int y, Color color) {
        this.drawText(text, x, y, color, true);
    }

    public void drawText(TextObject text, int x, int y, boolean shadow) {
        this.drawText(text, x, y, Color.WHITE, shadow);
    }

    public void drawText(TextObject text, int x, int y, Color color, boolean shadow) {
        this.font.drawText(this, text.getText(), x, y, color, shadow);
    }

    public void drawText(TextObject text, float x, float y) {
        this.drawText(text, x, y, Color.WHITE);
    }

    public void drawText(TextObject text, float x, float y, Color color) {
        this.drawText(text, x, y, color, true);
    }

    public void drawText(TextObject text, float x, float y, boolean shadow) {
        this.drawText(text, x, y, Color.WHITE, shadow);
    }

    public void drawText(TextObject text, float x, float y, Color color, boolean shadow) {
        this.font.drawText(this, text.getText(), x, y, color, shadow);
    }

    public void drawCenteredText(String text, int x, int y) {
        this.drawText(text, x - this.font.width(text) / 2, y);
    }

    public void drawCenteredText(String text, int x, int y, Color color) {
        this.drawText(text, x - this.font.width(text) / 2, y, color);
    }

    public void drawCenteredText(String text, int x, int y, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, shadow);
    }

    public void drawCenteredText(String text, int x, int y, Color color, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, color, shadow);
    }

    public void drawCenteredText(String text, float x, float y) {
        this.drawText(text, x - this.font.width(text) / 2, y);
    }

    public void drawCenteredText(String text, float x, float y, Color color) {
        this.drawText(text, x - this.font.width(text) / 2, y, color);
    }

    public void drawCenteredText(String text, float x, float y, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, shadow);
    }

    public void drawCenteredText(String text, float x, float y, Color color, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, color, shadow);
    }

    public void drawCenteredText(TextObject text, int x, int y) {
        this.drawText(text, x - this.font.width(text) / 2, y);
    }

    public void drawCenteredText(TextObject text, int x, int y, Color color) {
        this.drawText(text, x - this.font.width(text) / 2, y, color);
    }

    public void drawCenteredText(TextObject text, int x, int y, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, shadow);
    }

    public void drawCenteredText(TextObject text, int x, int y, Color color, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, color, shadow);
    }

    public void drawCenteredText(TextObject text, float x, float y) {
        this.drawText(text, x - this.font.width(text) / 2, y);
    }

    public void drawCenteredText(TextObject text, float x, float y, Color color) {
        this.drawText(text, x - this.font.width(text) / 2, y, color);
    }

    public void drawCenteredText(TextObject text, float x, float y, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, shadow);
    }

    public void drawCenteredText(TextObject text, float x, float y, Color color, boolean shadow) {
        this.drawText(text, x - this.font.width(text) / 2, y, color, shadow);
    }

    public void drawTextScaled(String text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(String text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, shadow);
        this.popMatrix();
    }

    public void drawTextScaled(TextObject text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(String text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, shadow);
        this.popMatrix();
    }

    public void drawCenteredTextScaled(TextObject text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.drawText(text, x / scale - this.font.width(text) / scale * 1.5f, y / scale, color, shadow);
        this.popMatrix();
    }

    public void multiLineText(String text, int x, int y) {
        this.multiLineText(text, x, y, Color.WHITE);
    }

    public void multiLineText(String text, int x, int y, Color color) {
        this.multiLineText(text, x, y, color, true);
    }

    public void multiLineText(String text, int x, int y, boolean shadow) {
        this.multiLineText(text, x, y, Color.WHITE, shadow);
    }

    public void multiLineText(String text, int x, int y, Color color, boolean shadow) {
        y -= this.font.lineHeight;

        for (String line : text.split("\n"))
            this.drawText(line, x, y += this.font.lineHeight, color, shadow);
    }

    public void tabString(String text, int x, int y) {
        this.tabString(text, x, y, Color.WHITE);
    }

    public void tabString(String text, int x, int y, Color color) {
        this.tabString(text, x, y, color, true);
    }

    public void tabString(String text, int x, int y, boolean shadow) {
        this.tabString(text, x, y, Color.WHITE, shadow);
    }

    public void tabString(String text, int x, int y, Color color, boolean shadow) {
        for (String line : text.split("\t")) {
            //noinspection SuspiciousNameCombination
            this.drawText(line, x += this.font.lineHeight, y, color, shadow);
        }
    }

    public void clear() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    ////////////////////////////
    //     Transformation     //
    ////////////////////////////
    public void translate(float x, float y) {
        this.globalTranslation.peek().add(x, y, 0);
        this.matrixStack.translate(x, y);
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public void translate(int x, int y) {
        this.globalTranslation.peek().add(x, y, 0);
        this.matrixStack.translate((float) x, (float) y);
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public void translate(float x, float y, float z) {
        this.globalTranslation.peek().add(x, y, z);
        this.matrixStack.translate(x, y, z);
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public void translate(int x, int y, int z) {
        this.globalTranslation.peek().add(x, y, z);
        this.matrixStack.translate((float) x, (float) y, (float) z);
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public void rotate(double x, double y) {
        this.matrixStack.rotate(new Quaternion(1, 0, 0, (float) x));
        this.matrixStack.rotate(new Quaternion(0, 1, 0, (float) y));
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public void scale(double sx, double sy) {
        this.matrixStack.scale((float) sx, (float) sy);
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public Matrix4 getTransform() {
        return this.matrixStack.last();
    }

    public float getStrokeWidth() {
        return this.strokeWidth;
    }

    public Color getColor() {
        com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
        com.badlogic.gdx.graphics.Color.abgr8888ToColor(color, this.shapes.getPackedColor());
        return Color.gdx(color);
    }

    public Font getFont() {
        return this.font;
    }

    ///////////////////////////
    //     Miscellaneous     //
    ///////////////////////////
    @ApiStatus.Experimental
    public void drawRegion(int x, int y, int width, int height, Consumer<Renderer> consumer) {
        this.pushMatrix();
        this.translate(x, y);
        if (this.pushScissors(x, y, width, height)) {
            consumer.accept(this);
            this.popScissors();
        }
        this.popMatrix();
    }

    @ApiStatus.Internal
    public boolean pushScissorsRaw(int x, int y, int width, int height) {
        return this.pushScissorsInternal(new Rectangle(x, y, width, height));
    }

    @CheckReturnValue
    private boolean pushScissorsInternal(Rectangle rect) {
        rect.getPosition(this.tmp2A);
        this.tmp3A.set(this.globalTranslation.peek());
        rect.setPosition(this.tmp2A.add(this.tmp3A.x, this.tmp3A.y));

        if (rect.x < 0) {
            rect.width = Math.max(rect.width + rect.x, 0);
            rect.x = 0;
        }

        if (rect.y < 0) {
            rect.height = Math.max(rect.height + rect.y, 0);
            rect.y = 0;
        }

        if (rect.width < 1) return false;
        if (rect.height < 1) return false;

        rect.y = Gdx.graphics.getHeight() - rect.y - rect.height;

        this.flush();
        return ScissorStack.pushScissors(rect);
    }

    @CheckReturnValue
    public boolean pushScissors(int x, int y, int width, int height) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                x * this.game.getGuiScale(), y * this.game.getGuiScale(),
                width * this.game.getGuiScale(), height * this.game.getGuiScale())
        );
    }

    @CheckReturnValue
    public boolean pushScissors(float x, float y, float width, float height) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                x * this.game.getGuiScale(), y * this.game.getGuiScale(),
                width * this.game.getGuiScale(), height * this.game.getGuiScale())
        );
    }

    @CheckReturnValue
    public boolean pushScissors(Rectangle rect) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                rect.x * this.game.getGuiScale(), rect.y * this.game.getGuiScale(),
                rect.width * this.game.getGuiScale(), rect.height * this.game.getGuiScale())
        );
    }

    @CanIgnoreReturnValue
    public Rectangle popScissors() {
        this.flush();
        return ScissorStack.popScissors();
    }

    public void flush() {
        this.batch.flush();
        Gdx.gl.glFlush();
    }

    @ApiStatus.Experimental
    public void clearScissors() {
        while (ScissorStack.peekScissors() != null) {
            ScissorStack.popScissors();
        }
    }

    @Override
    public String toString() {
        return "Renderer{}";
    }

    @Deprecated(forRemoval = true)
    public void blit(int x, int y) {
        this.batch.draw(this.curTexture, x, y);
    }

    @Deprecated(forRemoval = true)
    public void blit(int x, int y, int width, int height) {
        this.batch.draw(this.curTexture, x, y, width, height);
    }

    @Deprecated
    public void setTexture(Identifier texture) {
        this.curTexture = this.game.getTextureManager().getTexture(texture);
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setTextureColor(Color color) {
        this.textureColor = color;
    }

    public Color getTextureColor() {
        return this.textureColor;
    }

    public void pushMatrix() {
        this.globalTranslation.push(this.globalTranslation.peek().cpy());
        this.matrixStack.push();
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public void popMatrix() {
        this.globalTranslation.pop();
        this.matrixStack.pop();
        this.batch.setTransformMatrix(this.matrixStack.last());
    }

    public Batch getBatch() {
        return this.batch;
    }

    public Vector3 getGlobalTranslation() {
        return this.globalTranslation.peek().cpy();
    }

    public void fill(int x, int y, int width, int height, Color rgb) {
        this.setColor(rgb);
        this.rect(x, y, width, height);
    }

    public void box(int x, int y, int width, int height, Color rgb) {
        this.setColor(rgb);
        this.rectLine(x, y, width, height);
    }

    public void draw9PatchTexture(Texture texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight) {
        this.blit(texture, x, y + height - vHeight, uWidth, vHeight, u, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y + height - vHeight, uWidth, vHeight, u + uWidth * 2, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x, y, uWidth, vHeight, u, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y, uWidth, vHeight, u + uWidth * 2, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        for (int dx = x + uWidth; dx < width - uWidth; dx += uWidth) {
            int maxX = Math.min(dx + uWidth, width - uWidth);
            int uW = maxX - dx;
            this.blit(texture, dx, y + height - vHeight, uW, vHeight, u + uWidth, v, uW, vHeight, texWidth, texHeight);
            this.blit(texture, dx, y, uW, vHeight, u + uWidth, v + vHeight * 2, uW, vHeight, texWidth, texHeight);
        }

        for (int dy = y + vHeight; dy < height - vHeight; dy += vHeight) {
            int maxX = Math.min(dy + vHeight, height - vHeight);
            int vH = maxX - dy;
            this.blit(texture, x, dy, uWidth, vH, u, v + uWidth, uWidth, vH, texWidth, texHeight);
            this.blit(texture, x + width - uWidth, dy, uWidth, vH, u + uWidth * 2, u + uWidth, uWidth, vH, texWidth, texHeight);
        }
    }

    public void setShader(ShaderProgram program) {
        this.batch.setShader(program);
    }

    public void unsetShader() {
        this.batch.setShader(null);
    }

    public void model(Runnable block) {
        boolean drawing = this.batch.isDrawing();
        if (drawing) this.batch.end();
        block.run();
        if (drawing) this.batch.begin();
    }

    public void enableInvert() {
//        this.flush();
        this.batch.setBlendFunctionSeparate(GL20.GL_ONE_MINUS_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR, GL20.GL_ONE, GL20.GL_ZERO);
    }

    public void disableInvert() {
//        this.flush();
        this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
