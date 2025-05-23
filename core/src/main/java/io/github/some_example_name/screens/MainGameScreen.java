package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.MainGame;
import io.github.some_example_name.igra.*;
import io.github.some_example_name.igra.modalminigame.PortalModal;
import io.github.some_example_name.igra.modalminigame.QuestionMiniGame;
import io.github.some_example_name.igra.modalminigame.QuestionModal;

/**
 * Main game screen that implements the Screen interface Converted from MainGame ApplicationAdapter
 */
public class MainGameScreen implements Screen {
  private final MainGame game;
  private OrthographicCamera camera;
  private SpriteBatch batch;
  private TextureAtlas playerTexture;
  private Sound damageSound, pickupSound;
  private BitmapFont font;
  private BitmapFont fontSmaller;
  private Player player;
  private BaseMap currentMap;
  private EnemyManager enemyManager;
  private TextureAtlas enemyTexture;
  private Stage uiStage;
  private Skin skin;

  // Polje
  private QuestionModal questionModal;
  private PortalModal portalModal;

  public MainGameScreen(MainGame game) {
    this.game = game;
  }

  public BitmapFont getFont() {
    return font;
  }

  public BitmapFont getFontSmaller() {
    return fontSmaller;
  }

  @Override
  public void show() {
    // Initialize all resources - similar to create() in ApplicationAdapter
    camera = new OrthographicCamera();
    camera.setToOrtho(false, 1280, 720);

    // Use game's SpriteBatch if available or create a new one
    if (game.getBatch() != null) {
      batch = game.getBatch();
    } else {
      batch = new SpriteBatch();
    }

    // Initialize fonts
    font = new BitmapFont();
    fontSmaller =
        new BitmapFont(
            Gdx.files.internal("assets/lsans-small.fnt"),
            Gdx.files.internal("assets/lsans-small.png"),
            false);

    // Load textures and sounds
    playerTexture = new TextureAtlas("assets/player.txt");
    enemyTexture = new TextureAtlas("assets/enemy.txt");
    damageSound = Gdx.audio.newSound(Gdx.files.internal("tiled/hit.mp3"));
    pickupSound = Gdx.audio.newSound(Gdx.files.internal("tiled/pickup.mp3"));

    // Initialize game objects
    player = new Player(game, playerTexture, damageSound, pickupSound);
    enemyManager = new EnemyManager(enemyTexture, damageSound, pickupSound);
    currentMap = MapFactory.createMap("tiled/starting.tmx", player, enemyManager);
    camera.zoom = currentMap.getDefaultZoom();
    player.setMap(currentMap);

    questionModal = new QuestionModal();
    uiStage = new Stage(new ScreenViewport());
    skin = new Skin(Gdx.files.internal("uiskin/uiskin.json")); // Potrebuješ uiskin (glej spodaj)
    Gdx.input.setInputProcessor(uiStage);
    portalModal = new PortalModal();
    player.setQuestionModal(questionModal);
    player.setPortalModal(portalModal);
    player.setUI(uiStage, skin);
    // Log that we've initialized
    Gdx.app.log("MainGameScreen", "Game screen initialized");
  }

  public void gameover() {
    game.setScreen(new GameOverScreen(this.game));
  }

  @Override
  public void render(float delta) {
    // Clear the screen
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (!questionModal.isActive() && !portalModal.isActive()) {
      // Update game logic
      currentMap.update(delta);

      // Handle map switching
      if (currentMap.shouldSwitchMap()) {
        currentMap.dispose();
        currentMap = MapFactory.createMap(currentMap.getNextMapPath(), player, enemyManager);
        player.setMap(currentMap);
        camera.zoom = currentMap.getDefaultZoom();
      }

      // Camera tracking
      if (currentMap.shouldCameraTrackPlayer()) {
        camera.position.set(player.getX() + 32, player.getY() + 32, 0);
      }
    }
    // Update camera and render
    camera.update();

    // Render with appropriate font
    if (currentMap.isSmallerScale()) {
      currentMap.render(camera, batch, fontSmaller);
    } else {
      currentMap.render(camera, batch, font);
    }
    uiStage.act(Gdx.graphics.getDeltaTime());
    uiStage.draw();
    if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && !questionModal.isActive()) {
      QuestionMiniGame miniGame = new QuestionMiniGame();
      miniGame.load(); // Če je potrebno

      questionModal.show(
          uiStage,
          skin,
          miniGame,
          () -> System.out.println("Pravilen odgovor!"),
          () -> System.out.println("Napačen odgovor ali je potek čas!"));
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !portalModal.isActive()) {
      portalModal.show(
          uiStage,
          skin,
          () -> System.out.println("✅ Portal odprt!"),
          () -> System.out.println("❌ Portal zavrnjen!"));
    }
    // Tipka za prik
  }

  @Override
  public void resize(int width, int height) {
    // Handle screen resize
    camera.viewportWidth = width;
    camera.viewportHeight = height;
    camera.update();
  }

  @Override
  public void pause() {
    // Game paused (mobile devices)
    Gdx.app.log("MainGameScreen", "Game paused");
  }

  @Override
  public void resume() {
    // Game resumed from pause
    Gdx.app.log("MainGameScreen", "Game resumed");
  }

  @Override
  public void hide() {
    // Screen is no longer visible
    Gdx.app.log("MainGameScreen", "Game hidden");
  }

  @Override
  public void dispose() {
    // Dispose resources that were created in this screen
    // Don't dispose batch if it came from the game
    if (game.getBatch() == null && batch != null) {
      batch.dispose();
    }

    if (playerTexture != null) playerTexture.dispose();
    if (enemyTexture != null) enemyTexture.dispose();
    if (damageSound != null) damageSound.dispose();
    if (pickupSound != null) pickupSound.dispose();
    if (font != null) font.dispose();
    if (fontSmaller != null) fontSmaller.dispose();
    if (currentMap != null) currentMap.dispose();

    Gdx.app.log("MainGameScreen", "Game resources disposed");
  }
}
