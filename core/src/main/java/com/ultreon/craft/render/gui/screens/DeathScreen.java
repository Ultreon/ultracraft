package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.libs.translations.v0.Language;

public class DeathScreen extends Screen {
    private Button respawnButton;
    private Button exitWorldButton;

    public DeathScreen() {
        super(Language.translate("craft/screen/death/title"));

        this.titleLayout.setText(largeFont, title);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.respawnButton.setPos(width / 2 - 100, height - height / 3 + 5);
        this.exitWorldButton.setPos(width / 2 - 100, height - height / 3 - 25);
    }

    @Override
    public void show() {
        super.show();

        this.respawnButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 + 5, 200, Language.translate("craft/screen/death/respawn"), this::respawn));
        this.exitWorldButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 - 25, 200, Language.translate("craft/screen/pause/exit_world"), this::exitWorld));
        this.exitWorldButton.setColor(Color.red);
        this.exitWorldButton.setTextColor(Color.white);
    }

    private void respawn(Button button) {
        this.game.respawn();
        this.game.showScreen(null);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        Batch batch = renderer.getBatch();
        largeFont.setColor(Color.white.darker().darker().toGdx());
        largeFont.draw(batch, title, (int)((float) width / 2 - titleLayout.width / 2), (int)((float) (height - 40 - 2)));
        largeFont.setColor(Color.white.toGdx());
        largeFont.draw(batch, title, (int)((float) width / 2 - titleLayout.width / 2), (int)((float) (height - 40)));
    }

    private void exitWorld(Button caller) {
        this.game.exitWorld();
    }

    public Button getRespawnButton() {
        return respawnButton;
    }

    public Button getExitWorldButton() {
        return exitWorldButton;
    }

    @Override
    public boolean canCloseOnEsc() {
        return false;
    }
}
