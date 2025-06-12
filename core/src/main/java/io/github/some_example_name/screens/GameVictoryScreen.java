package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.MainGame;

public class GameVictoryScreen implements Screen {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private TextButton menuButton;
    private Music victoryMusic;
    private Group letterGroup;
    private ParticleEffect confettiEffect;
    private ParticleEffect starEffect;
    private float animationTime = 0f;
    private final float LETTERS_COMPLETE_TIME = 2.5f;
    private final float BUTTON_APPEAR_TIME = 3.0f;
    private final String VICTORY_TEXT = "VICTORY!";
    private Array<Label> letterLabels;
    private boolean animationComplete = false;

    public GameVictoryScreen(final MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        stage = new Stage(viewport, game.getBatch());
        letterLabels = new Array<>();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // Load and play victory music
        try {
            victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("victory.wav"));
            victoryMusic.setVolume(0.7f);
            victoryMusic.setLooping(false);
            victoryMusic.play();
        } catch (Exception e) {
            Gdx.app.log("GameVictoryScreen", "Failed to load victory music", e);
        }

        // Load skin
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

        // Load background
        try {
            backgroundTexture = new Texture(Gdx.files.internal("video/ezgif-frame.jpg"));
        } catch (Exception e) {
            try {
                backgroundTexture = new Texture(Gdx.files.internal("backgrounds/menu-background.png"));
            } catch (Exception e2) {
                Gdx.app.log("GameVictoryScreen", "Background image not found");
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
        stage.addActor(rootTable);

        // Create letter group to hold all the victory letters
        letterGroup = new Group();
        letterGroup.setPosition(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
        stage.addActor(letterGroup);

        // Create letters with different colors for animation
        Color[] colors = {
                Color.GOLD,
                Color.ORANGE,
                Color.YELLOW,
                Color.CHARTREUSE,
                Color.SKY,
                Color.ROYAL,
                Color.PURPLE,
                Color.CORAL
        };

        float startX = -VICTORY_TEXT.length() * 20;

        for (int i = 0; i < VICTORY_TEXT.length(); i++) {
            String letter = VICTORY_TEXT.substring(i, i + 1);
            Label.LabelStyle style = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
            style.fontColor = colors[i % colors.length];

            Label letterLabel = new Label(letter, style);
            letterLabel.setFontScale(4.0f);

            // Set initial position off-screen (top)
            letterLabel.setPosition(startX + (i * 40), 300);
            letterLabel.getColor().a = 0;

            letterLabels.add(letterLabel);
            letterGroup.addActor(letterLabel);

            // Create letter entry animation with offset timing
            float delay = i * 0.15f;
            letterLabel.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                            Actions.fadeIn(0.3f),
                            Actions.moveTo(startX + (i * 40), 0, 0.5f, Interpolation.bounceOut)
                    ),
                    Actions.forever(Actions.sequence(
                            Actions.moveBy(0, 10, 0.5f, Interpolation.sine),
                            Actions.moveBy(0, -10, 0.5f, Interpolation.sine)
                    ))
            ));
        }

        // Menu button (initially hidden)
        menuButton = createStyledButton("Return to Main Menu", Color.WHITE);
        menuButton.setSize(380, 60);
        menuButton.setPosition(viewport.getWorldWidth() / 2 - 190, viewport.getWorldHeight() / 2 - 150);
        menuButton.getColor().a = 0; // Start invisible
        stage.addActor(menuButton);

        // Add button listener
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

    private void updateAnimation(float delta) {
        if (animationComplete) return;

        animationTime += delta;

        // Rotate letters occasionally for extra effect
        if (MathUtils.randomBoolean(0.02f)) {
            int randomIndex = MathUtils.random(letterLabels.size - 1);
            letterLabels.get(randomIndex).addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.scaleTo(1.3f, 1.3f, 0.2f),
                            Actions.rotateBy(360, 0.5f)
                    ),
                    Actions.scaleTo(1.0f, 1.0f, 0.2f)
            ));
        }

        // Show the menu button after the animation
        if (animationTime >= BUTTON_APPEAR_TIME && menuButton.getColor().a == 0) {
            menuButton.addAction(Actions.sequence(
                    Actions.fadeIn(0.5f),
                    Actions.moveBy(0, 20, 0.3f, Interpolation.swingOut),
                    Actions.moveBy(0, -10, 0.2f, Interpolation.swing)
            ));

            animationComplete = true;
        }

        // Update particle effects
        if (confettiEffect != null) {
            confettiEffect.update(delta);
        }

        if (starEffect != null) {
            starEffect.update(delta);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateAnimation(delta);

        stage.act(Math.min(delta, 1/30f));
        stage.draw();

        // Draw particle effects
        game.getBatch().begin();
        if (confettiEffect != null) {
            confettiEffect.draw(game.getBatch());
        }
        if (starEffect != null) {
            starEffect.draw(game.getBatch());
        }
        game.getBatch().end();
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
    public void hide() {
        if (victoryMusic != null) {
            victoryMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (victoryMusic != null) {
            victoryMusic.stop();
            victoryMusic.dispose();
        }
        if (confettiEffect != null) {
            confettiEffect.dispose();
        }
        if (starEffect != null) {
            starEffect.dispose();
        }
        stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
