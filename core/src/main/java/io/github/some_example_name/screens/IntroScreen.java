package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.MainGame;

public class IntroScreen implements Screen {
  private final MainGame game;
  private SpriteBatch batch;
  private Array<Texture> frames;
  private float frameTime = 0;
  private int currentFrame = 0;
  private float totalDuration = 5f; // 5 seconds for the intro

  public IntroScreen(MainGame game) {
    this.game = game;
    frames = new Array<>();
  }

  @Override
  public void show() {
    batch = new SpriteBatch();

    // Load all the frames
    loadFrames();
  }

  private void loadFrames() {
    try {
      // Assuming you have frames from 1 to 150
      int totalFrames = 99;
      for (int i = 1; i <= totalFrames; i++) {
        String framePath = "";
        if (i < 10) {
          framePath = "video/ezgif-frame-00" + i + ".jpg";
        } else {
          framePath = "video/ezgif-frame-0" + i + ".jpg";
        }
        if (Gdx.files.internal(framePath).exists()) {
          frames.add(new Texture(Gdx.files.internal(framePath)));
        } else {
          Gdx.app.log("IntroScreen", "Couldn't find frame: " + framePath);
        }
      }

      Gdx.app.log("IntroScreen", "Loaded " + frames.size + " frames");

      if (frames.size == 0) {
        // No frames were loaded, go to the main game screen
        game.setScreen(new MainMenuScreen(game));
      }
    } catch (Exception e) {
      Gdx.app.error("IntroScreen", "Error loading frames: " + e.getMessage());
      game.setScreen(new MainMenuScreen(game));
    }
  }

  @Override
  public void render(float delta) {
    ScreenUtils.clear(0, 0, 0, 1);

    if (frames.size > 0) {
      // Calculate which frame to show based on time
      frameTime += delta;
      float frameRate = totalDuration / frames.size;
      currentFrame = Math.min((int) (frameTime / frameRate), frames.size - 1);

      batch.begin();
      // Draw the current frame
      batch.draw(frames.get(currentFrame), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      batch.end();

      // If we've reached the end of the frames, go to the main game screen
      if (currentFrame >= frames.size - 1) {
        game.setScreen(new MainMenuScreen(game));
      }
    }
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {
    dispose();
  }

  @Override
  public void dispose() {
    if (batch != null) {
      batch.dispose();
    }

    // Dispose all textures
    for (Texture texture : frames) {
      texture.dispose();
    }
    frames.clear();
  }
}
