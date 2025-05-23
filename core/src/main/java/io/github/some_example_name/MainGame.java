package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.screens.IntroScreen;

/**
 * Main game class that extends Game to support screens Converted from ApplicationAdapter to Game
 */
public class MainGame extends Game {
  // Shared resources
  private SpriteBatch batch;
  private BitmapFont font;
  private BitmapFont fontSmaller;

  // Getters for shared resources
  public SpriteBatch getBatch() {
    return batch;
  }

  public BitmapFont getFont() {
    return font;
  }

  public BitmapFont getFontSmaller() {
    return fontSmaller;
  }

  @Override
  public void create() {
    // Initialize shared resources
    batch = new SpriteBatch();
    font = new BitmapFont();
    fontSmaller =
        new BitmapFont(
            Gdx.files.internal("assets/lsans-small.fnt"),
            Gdx.files.internal("assets/lsans-small.png"),
            false);

    // Start with the intro screen
    setScreen(new IntroScreen(this));

    Gdx.app.log("MainGame", "Game initialized");
  }

  @Override
  public void render() {
    // Let the screen system handle rendering
    super.render();

  }

  @Override
  public void dispose() {
    // Dispose shared resources
    if (batch != null) {
      batch.dispose();
    }

    if (font != null) {
      font.dispose();
    }

    if (fontSmaller != null) {
      fontSmaller.dispose();
    }

    // Dispose current screen if exists
    if (getScreen() != null) {
      getScreen().dispose();
    }

    Gdx.app.log("MainGame", "Game disposed");
  }
}
