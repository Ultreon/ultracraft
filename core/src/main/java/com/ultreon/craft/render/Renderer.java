/////////////////////
//     Package     //
/////////////////////
package com.ultreon.craft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.google.common.base.Preconditions;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec4i;
import com.ultreon.libs.text.v0.TextObject;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Renderer class.
 *
 * @author Qboi
 */
@SuppressWarnings("unused")
public class Renderer {
    ////////////////////
    //     Fields     //
    ////////////////////
    private final UltreonCraft game = UltreonCraft.get();
    private final Vector3 globalTranslation = new Vector3();
    private final GL20 gl20;
    private final GL30 gl30;
    private final Batch batch;
    private final ShapeDrawer shapes;
    private float strokeWidth;
    private Texture curTexture;
    private BitmapFont font;
    private final MatrixStack matrixStack;
    private Color textureColor = Color.rgb(0xffffff);

    /**
     * @param shapes shape drawer instance from {@link UltreonCraft}
     */
    public Renderer(ShapeDrawer shapes) {
        this(shapes, new MatrixStack());
    }

    /**
     * @param shapes shape drawer instance from {@link UltreonCraft}
     * @param matrixStack current matrix stack.
     */
    public Renderer(ShapeDrawer shapes, MatrixStack matrixStack) {
        this.font = game.getBitmapFont();
        this.gl20 = Gdx.gl20;
        this.gl30 = Gdx.gl30;
        this.batch = shapes.getBatch();
        this.shapes = shapes;
        this.matrixStack = matrixStack;

        // Projection matrix.
        Consumer<Matrix4> projectionMatrixSetter = matrix -> {
//            shapes.getBatch().setTransformMatrix(matrix);
        };
        this.matrixStack.onPush = projectionMatrixSetter;
        this.matrixStack.onPop = projectionMatrixSetter;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setColor(Color c) {
        if (c == null) return;
        font.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        shapes.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }

    public void setColor(int r, int g, int b) {
        setColor(Color.rgb(r, g, b));
    }

    public void setColor(float r, float g, float b) {
        setColor(Color.rgb(r, g, b));
    }

    public void setColor(int r, int g, int b, int a) {
        setColor(Color.rgba(r, g, b, a));
    }

    public void setColor(float r, float g, float b, float a) {
        setColor(Color.rgba(r, g, b, a));
    }

    public void setColor(int argb) {
        setColor(Color.argb(argb));
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
        setColor(Color.hex(hex));
    }

    public void clearColor(Color color) {
        gl20.glClearColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public void clearColor(int red, int green, int blue) {
        clearColor(Color.rgb(red, green, blue));
    }

    public void clearColor(float red, float green, float blue) {
        clearColor(Color.rgb(red, green, blue));
    }

    public void clearColor(int red, int green, int blue, int alpha) {
        clearColor(Color.rgba(red, green, blue, alpha));
    }

    public void clearColor(float red, float green, float blue, float alpha) {
        clearColor(Color.rgba(red, green, blue, alpha));
    }

    public void clearColor(int argb) {
        clearColor(Color.argb(argb));
    }

    public void clearColor(String hex) {
        clearColor(Color.hex(hex));
    }

    public void outline(Rectangle2D rect) {
        rectLine((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
    }

    public void outline(Ellipse2D ellipse) {
        ovalLine((float) ellipse.getX(), (float) ellipse.getY(), (float) ellipse.getWidth(), (float) ellipse.getHeight());
    }

    public void outline(Line2D s) {
        line((float) s.getX1(), (float) s.getY1(), (float) s.getX2(), (float) s.getY2());
    }

    public void circle(float x, float y, float radius) {
        y = game.getHeight() + radius - y;
        shapes.filledCircle(x, y, radius);
    }

    public void circleLine(float x, float y, float radius) {
        y = game.getHeight() + radius - y;
        shapes.circle(x, y, radius);
    }

    public void fill(Rectangle2D rect) {
        rect((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
    }

    public void fill(Ellipse2D ellipse) {
        oval((float) ellipse.getX(), (float) ellipse.getY(), (float) ellipse.getWidth(), (float) ellipse.getHeight());
    }

    public void fill(Line2D line) {
        line((float) line.getX1(), (float) line.getY1(), (float) line.getX2(), (float) line.getY2());
    }

    public void fill(Vec4i r) {
        rect(r.x, r.y, r.z, r.w);
    }

    public void line(int x1, int y1, int x2, int y2) {
        shapes.line(x1, y1, x2, y2);
    }

    public void line(float x1, float y1, float x2, float y2) {
        shapes.line(x1, y1, x2, y2);
    }

    public void rectLine(int x, int y, int width, int height) {
        shapes.rectangle(x, y, width, height, strokeWidth);
    }

    public void rectLine(float x, float y, float width, float height) {
        shapes.rectangle(x, y, width, height, strokeWidth);
    }

    public void rect(int x, int y, int width, int height) {
        shapes.filledRectangle(x, y, width, height);
    }

    public void rect(float x, float y, float width, float height) {
        shapes.filledRectangle(x, y, width, height);
    }

    public void roundRectLine(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        shapes.rectangle(x, y, width, height, strokeWidth);
    }

    public void roundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        shapes.rectangle(x, y, width, height);
    }

    public void rect3DLine(int x, int y, int width, int height, boolean raised) {
        shapes.rectangle(x, y, width, height, strokeWidth);
    }

    public void rect3D(int x, int y, int width, int height, boolean raised) {
        shapes.filledRectangle(x, y, width, height);
    }

    public void ovalLine(int x, int y, int width, int height) {
        shapes.ellipse(x, y, width, height);
    }

    public void oval(int x, int y, int width, int height) {
        shapes.filledEllipse(x, y, width, height);
    }

    public void ovalLine(float x, float y, float width, float height) {
        shapes.ellipse(x, y, width, height);
    }

    public void oval(float x, float y, float width, float height) {
        shapes.filledEllipse(x, y, width, height);
    }

    public void arcLine(int x, int y, int width, int height, int startAngle, int arcAngle) {
        shapes.arc(x, y, width, startAngle, arcAngle);
    }

    public void arc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        shapes.arc(x, y, width, startAngle, arcAngle);
    }

    ///////////////////
    //     Image     //
    ///////////////////
    public void texture(TextureRegion tex, float x, float y) {
        batch.setColor(textureColor.toGdx());
        batch.draw(tex, x, y);
    }

    public void texture(TextureRegion tex, float x, float y, float width, float height) {
        batch.setColor(textureColor.toGdx());
        batch.draw(tex, x, y, width, height);
    }

    public void texture(Texture tex, float x, float y) {
        batch.setColor(textureColor.toGdx());
        batch.draw(tex, x, y);
    }


    public void texture(Texture tex, float x, float y, Color backgroundColor) {
        setColor(backgroundColor);
        rect(x, y, tex.getWidth(), tex.getHeight());
        batch.setColor(textureColor.toGdx());
        batch.draw(tex, x, y);
    }

    public void texture(Texture tex, float x, float y, float width, float height, Color backgroundColor) {
        texture(tex, x, y, width, height, 0.0F, 0.0F, backgroundColor);
    }

    public void texture(Texture tex, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        texture(tex, x, y, width, height, u, v, width, height, backgroundColor);
    }

    public void texture(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        texture(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
    }

    public void texture(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        setColor(backgroundColor);
        rect(x, y, width, height);
        batch.setColor(textureColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        batch.draw(textureRegion, x, y, width, height);
    }

    public void texture(Texture tex, float x, float y, float width, float height) {
        texture(tex, x, y, width, height, 0.0F, 0.0F);
    }

    public void texture(Texture tex, float x, float y, float width, float height, float u, float v) {
        texture(tex, x, y, width, height, u, v, width, height);
    }

    public void texture(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        texture(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
    }

    public void texture(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        batch.setColor(textureColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, 1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        batch.draw(textureRegion, x, y, width, height);
    }

    //////////////////
    //     Text     //
    //////////////////
    public void text(String str, int x, int y) {
        font.draw(batch, str, x, y);
    }

    public void text(String str, float x, float y) {
        font.draw(batch, str, x, y);
    }

    public void text(String str, float x, float y, float maxWidth,  String truncate) {
        font.draw(batch, str, x, y, 0, str.length(), maxWidth, 0, false, truncate);
    }

    public void text(String str, float x, float y, float maxWidth, boolean wrap, String truncate) {
        font.draw(batch, str, x, y, 0, str.length(), maxWidth, 0, wrap);
    }

    public void text(TextObject str, int x, int y) {
        font.draw(batch, str.getText(), x, y);
    }

    public void text(TextObject str, float x, float y) {
        font.draw(batch, str.getText(), x, y);
    }

    public void multiLineText(String str, int x, int y) {
        y -= font.getLineHeight();

        for (String line : str.split("\n"))
            text(line, x, y += font.getLineHeight());
    }

    public void tabString(String str, int x, int y) {
        for (String line : str.split("\t"))
            text(line, x += font.getLineHeight(), y);
    }

    public void clear() {
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    ////////////////////////////
    //     Transformation     //
    ////////////////////////////
    public void translate(float tx, float ty) {
        globalTranslation.add(tx, ty, 0);
        matrixStack.translate(tx, ty);
        batch.setProjectionMatrix(matrixStack.last());
    }

    public void translate(int x, int y) {
        globalTranslation.add(x, y, 0);
        matrixStack.translate((float) x, (float) y);
        batch.setProjectionMatrix(matrixStack.last());
    }

    public void rotate(double x, double y) {
        matrixStack.rotate(new Quaternion(1, 0, 0, (float) x));
        matrixStack.rotate(new Quaternion(0, 1, 0, (float) y));
        batch.setProjectionMatrix(matrixStack.last());
    }

    public void scale(double sx, double sy) {
        matrixStack.scale((float) sx, (float) sy);
        batch.setProjectionMatrix(matrixStack.last());
    }

    public Matrix4 getTransform() {
        return matrixStack.last();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public Color getColor() {
        var color = new com.badlogic.gdx.graphics.Color();
        com.badlogic.gdx.graphics.Color.abgr8888ToColor(color, shapes.getPackedColor());
        return Color.gdx(color);
    }

    public BitmapFont getFont() {
        return font;
    }

    ///////////////////////////
    //     Miscellaneous     //
    ///////////////////////////
    public void subInstance(int x, int y, int width, int height, Consumer<Renderer> consumer) {
        var rectangle = new com.badlogic.gdx.math.Rectangle(x, y, width, height);
        boolean doPop = false;
        if (!Objects.equals(ScissorStack.peekScissors(), rectangle)) {
            doPop = true;
            ScissorStack.pushScissors(rectangle);
        }
        matrixStack.push();
        matrixStack.translate(x, y);
        batch.setProjectionMatrix(matrixStack.last());

        consumer.accept(this);

        matrixStack.pop();
        if (doPop) {
            ScissorStack.popScissors();
        }
    }

    @Override
    public String toString() {
        return "Renderer{}";
    }

    public void blit(int x, int y) {
        batch.draw(curTexture, x, y);
    }

    public void blit(int x, int y, int width, int height) {
        batch.draw(curTexture, x, y, width, height);
    }

    public void texture(Identifier texture) {
        this.curTexture = game.getTextureManager().getTexture(texture);
    }

    public void setFont(BitmapFont font) {
        this.font = font;
    }

    public void setTextureColor(Color color) {
        this.textureColor = color;
    }

    public Color getTextureColor() {
        return textureColor;
    }

    public void pushMatrix() {
        matrixStack.push();
    }

    public void popMatrix() {
        matrixStack.pop();
    }
}
