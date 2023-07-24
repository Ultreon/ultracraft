package com.ultreon.craft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;

import java.util.Stack;

public class ScissorStack {

	public static Stack<Rectangle> scissorStack = new Stack<>();

	public static void pushScissors(Rectangle rectangle) {
		float x = rectangle.x;
		float y = rectangle.y;
		float width = rectangle.width;
		float height = rectangle.height;

		if (scissorStack.size() > 0) {
			Rectangle scissor = scissorStack.peek();
			x = Math.max(scissor.x, x);
			y = Math.max(scissor.y, y);
			width = x + width > scissor.x + scissor.width ? scissor.x + scissor.width - x : width;
			height = y + height > scissor.y + scissor.height ? scissor.y + scissor.height - y : height;
		} else {
			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		}

		Gdx.gl.glScissor((int) x, (int) y, (int) width, (int) height);
		scissorStack.push(new Rectangle(x, y, width, height));
	}

	public static void popScissors() {
		if (!scissorStack.isEmpty()) {
			scissorStack.pop();
		}
		restoreScissors();
	}

	public static Rectangle peekScissors() {
		return !scissorStack.isEmpty() ? scissorStack.peek() : null;
	}

	private static void restoreScissors() {
		if (!scissorStack.isEmpty()) {
			Rectangle scissor = scissorStack.peek();
			Gdx.gl.glScissor((int) scissor.x, (int) scissor.y, (int) scissor.width, (int) scissor.height);
		} else {
			Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		}
	}

	public static boolean isEmpty() {
		return scissorStack.isEmpty();
	}
}
