package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;
import io.github.some_example_name.MainGame;

public class GameOverScreen implements Screen {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Label leftText;
    private Label rightText;
    private Label gameOverLabel;
    private TextButton menuButton;
    private float animationTime = 0f;
    private final float COLLISION_TIME = 1.2f;
    private final float BUTTON_APPEAR_TIME = 2.0f;

    private Music gameOverMusic;
    private boolean animationComplete = false;

    public GameOverScreen(final MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        stage = new Stage(viewport, game.getBatch());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        try {
            gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("lose.wav"));
            gameOverMusic.setVolume(0.7f);
            gameOverMusic.setLooping(false);
            gameOverMusic.play();
        } catch (Exception e) {
            Gdx.app.log("GameOverScreen", "Failed to load game over music", e);
        }

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
            backgroundTexture = new Texture(Gdx.files.internal("video/ezgif-frame.jpg"));
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
        stage.addActor(rootTable);

        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.fontColor = Color.RED;


        leftText = new Label("GAME", skin);
        leftText.setStyle(new Label.LabelStyle(labelStyle));
        leftText.setFontScale(3.5f);
        leftText.setPosition(-300, viewport.getWorldHeight() / 2 + 50);
        stage.addActor(leftText);


        rightText = new Label("OVER", skin);
        rightText.setStyle(new Label.LabelStyle(labelStyle));
        rightText.setFontScale(3.5f);
        rightText.setPosition(viewport.getWorldWidth() + 100, viewport.getWorldHeight() / 2 + 50);
        stage.addActor(rightText);


        gameOverLabel = new Label("GAME OVER", skin);
        gameOverLabel.setStyle(new Label.LabelStyle(labelStyle));
        gameOverLabel.setFontScale(3.5f);
        gameOverLabel.setPosition(viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50, Align.center);
        gameOverLabel.getColor().a = 0;
        stage.addActor(gameOverLabel);


        menuButton = createStyledButton("Return to Main Menu", Color.BLACK);
        menuButton.setSize(380, 60);
        menuButton.setPosition(viewport.getWorldWidth() / 2 - 190, viewport.getWorldHeight() / 2 - 100);
        menuButton.getColor().a = 0; 
        stage.addActor(menuButton);


        menuButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new MainMenuScreen(game));
                }
            });


        leftText.addAction(
            Actions.moveTo(viewport.getWorldWidth() / 2 - 150, viewport.getWorldHeight() / 2 + 50,
                COLLISION_TIME, Interpolation.swingOut)
        );

        rightText.addAction(
            Actions.moveTo(viewport.getWorldWidth() / 2 + 30, viewport.getWorldHeight() / 2 + 50,
                COLLISION_TIME, Interpolation.swingOut)
        );
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


        if (animationTime >= COLLISION_TIME && gameOverLabel.getColor().a == 0) {
      
            leftText.addAction(Actions.fadeOut(0.3f));
            rightText.addAction(Actions.fadeOut(0.3f));

            gameOverLabel.addAction(Actions.sequence(
                Actions.fadeIn(0.2f),
                Actions.moveBy(5, 0, 0.05f),
                Actions.moveBy(-10, 0, 0.05f),
                Actions.moveBy(8, 0, 0.05f),
                Actions.moveBy(-6, 0, 0.05f),
                Actions.moveBy(4, 0, 0.05f),
                Actions.moveBy(-2, 0, 0.05f)
            ));
        }

        if (animationTime >= BUTTON_APPEAR_TIME && menuButton.getColor().a == 0) {
            menuButton.addAction(Actions.sequence(
                Actions.fadeIn(0.5f),
                Actions.moveBy(0, 10, 0.2f, Interpolation.swing),
                Actions.moveBy(0, -10, 0.2f, Interpolation.swing)
            ));

            animationComplete = true;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateAnimation(delta);

        stage.act(Math.min(delta, 1/30f));
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
    public void hide() {
        if (gameOverMusic != null) {
            gameOverMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (gameOverMusic != null) {
            gameOverMusic.stop();
            gameOverMusic.dispose();
        }
        stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
