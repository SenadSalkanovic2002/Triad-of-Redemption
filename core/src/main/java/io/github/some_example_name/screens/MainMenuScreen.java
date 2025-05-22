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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.MainGame;

public class MainMenuScreen implements Screen {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Texture logoTexture;

    public MainMenuScreen(final MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        stage = new Stage(viewport, game.getBatch());
    }

    @Override
    public void show() {
        // Allow stage to receive input
        Gdx.input.setInputProcessor(stage);

        // Load skin for UI components
        try {
            skin = new Skin(Gdx.files.internal("uiskin.json"));
        } catch (Exception e) {
            // Create a basic skin if uiskin.json is not available
            skin = new Skin();
            BitmapFont font = new BitmapFont();
            skin.add("default-font", font);

            // Create default text button style
            TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
            textButtonStyle.font = font;
            textButtonStyle.fontColor = Color.WHITE;
            textButtonStyle.downFontColor = Color.LIGHT_GRAY;
            skin.add("default", textButtonStyle);

            // Create default label style
            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = font;
            labelStyle.fontColor = Color.WHITE;
            skin.add("default", labelStyle);
        }

        // Try to load background image
        try {
            backgroundTexture = new Texture(Gdx.files.internal("video/ezgif-frame-100.jpg"));
        } catch (Exception e) {
            try {
                // Try a different location
                backgroundTexture = new Texture(Gdx.files.internal("backgrounds/menu-background.png"));
            } catch (Exception e2) {
                Gdx.app.log("MainMenuScreen", "Background image not found, using blank background");
            }
        }


        setupUI();
    }

    private void setupUI() {
        // Clear previous actors
        stage.clear();

        // Add background if available
        if (backgroundTexture != null) {
            Image background = new Image(backgroundTexture);
            background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
            background.setPosition(0, 0);
            stage.addActor(background);
        }

        // Create main container table
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // Create button table
        Table buttonTable = new Table();

        // Create buttons with colors
        TextButton startButton = createStyledButton("Start Game", Color.BLACK);

        // Row 1: Start Game button
        buttonTable.add(startButton).pad(10).width(380).height(60).row();

        // Row 2: Settings and About buttons
        Table row2Table = new Table();
        TextButton settingsButton = createStyledButton("Settings", Color.BLACK);
        TextButton aboutButton = createStyledButton("About", Color.BLACK);

        row2Table.add(settingsButton).pad(10).width(180).height(60);
        row2Table.add(aboutButton).pad(10).width(180).height(60);
        buttonTable.add(row2Table).row();

        // Row 3: Exit button
        TextButton exitButton = createStyledButton("Exit Game", Color.BLACK);
        buttonTable.add(exitButton).pad(10).width(380).height(60).padTop(20);

        // Add button table to root table
        // Position it in the lower left corner with padding
        rootTable.align(Align.bottom | Align.left);
        rootTable.add(buttonTable).pad(0, 130, 75, 0);

        // Add root table to stage
        stage.addActor(rootTable);

        // Add button listeners
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainGameScreen(game));
            }
        });

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // You can add a settings screen later
                Gdx.app.log("MainMenuScreen", "Settings button clicked");
            }
        });

        aboutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // You can add an about screen later
                Gdx.app.log("MainMenuScreen", "About button clicked");
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    private TextButton createStyledButton(String text, Color color) {
        TextButton button = new TextButton(text, skin);

        // Style the button
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
        style.fontColor = color;
        style.downFontColor = color.cpy().mul(0.8f); // Darker when pressed
        button.setStyle(style);

        return button;
    }

    @Override
    public void render(float delta) {
        // Clear screen with a blue tint for atmosphere
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();

        // Draw the stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
    }
}
