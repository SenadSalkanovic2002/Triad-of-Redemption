package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.MainGame;

public class GameOverScreen implements Screen {
  private final MainGame game;
  private OrthographicCamera camera;
  private Viewport viewport;
  private Stage stage;
  private Skin skin;
  private Texture backgroundTexture;

  public GameOverScreen(final MainGame game) {
    this.game = game;
    camera = new OrthographicCamera();
    viewport = new FitViewport(1280, 720, camera);
    stage = new Stage(viewport, game.getBatch());
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(stage);

    // Load skin (same as main menu)
    try {
      skin = new Skin(Gdx.files.internal("uiskin.json"));
    } catch (Exception e) {
      skin = new Skin();
      BitmapFont font = new BitmapFont();
      skin.add("default-font", font);

      TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
      textButtonStyle.font = font;
      textButtonStyle.fontColor = Color.WHITE;
      textButtonStyle.downFontColor = Color.LIGHT_GRAY;
      skin.add("default", textButtonStyle);

      Label.LabelStyle labelStyle = new Label.LabelStyle();
      labelStyle.font = font;
      labelStyle.fontColor = Color.WHITE;
      skin.add("default", labelStyle);
    }

    // Load background (same as main menu)
    try {
      backgroundTexture = new Texture(Gdx.files.internal("video/ezgif-frame-100.jpg"));
    } catch (Exception e) {
      try {
        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/menu-background.png"));
      } catch (Exception e2) {
        Gdx.app.log("GameOverScreen", "Background image not found");
      }
    }

    setupUI();
  }

  private void setupUI() {
    stage.clear();

    if (backgroundTexture != null) {
      Image background = new Image(backgroundTexture);
      background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
      stage.addActor(background);
    }

    Table rootTable = new Table();
    rootTable.setFillParent(true);

    // Game Over text
    Label gameOverLabel = new Label("GAME OVER", skin);
    gameOverLabel.setStyle(new Label.LabelStyle(gameOverLabel.getStyle()));
    gameOverLabel.getStyle().fontColor = Color.RED;
    gameOverLabel.setFontScale(3f);
    Table buttonTable = new Table();

    // Create buttons with colors
    TextButton menuButton = createStyledButton("Return to Main Menu", Color.BLACK);
    // Return to main menu button

    // Layout
    rootTable.add(gameOverLabel).padBottom(100).row();
    rootTable.add(menuButton).pad(10).width(380).height(60).row();

    stage.addActor(rootTable);

    menuButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            game.setScreen(new MainMenuScreen(game));
          }
        });
  }

  private TextButton createStyledButton(String text, Color color) {
    TextButton button = new TextButton(text, skin);
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
    style.fontColor = color;
    style.downFontColor = color.cpy().mul(0.8f);
    button.setStyle(style);
    return button;
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    stage.dispose();
    if (skin != null) skin.dispose();
    if (backgroundTexture != null) backgroundTexture.dispose();
  }
}
