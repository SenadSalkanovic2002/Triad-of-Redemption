package io.github.some_example_name;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.some_example_name.igra.*;
import io.github.some_example_name.igra.modalminigame.PortalModal;
import io.github.some_example_name.igra.modalminigame.QuestionMiniGame;
import io.github.some_example_name.igra.modalminigame.QuestionModal;

public class MainGame extends ApplicationAdapter {

    private Stage uiStage;
    private Skin skin;

    // Polje
    private QuestionModal questionModal;
    private PortalModal portalModal;

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

    public BitmapFont getFont() {
        return font;
    }

    public BitmapFont getFontSmaller() {
        return fontSmaller;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        batch = new SpriteBatch();
        font = new BitmapFont();
        fontSmaller = new BitmapFont(Gdx.files.internal("assets/lsans-small.fnt"),
            Gdx.files.internal("assets/lsans-small.png"), false);
        questionModal = new QuestionModal();

        playerTexture = new TextureAtlas("assets/player.txt");
        enemyTexture = new TextureAtlas("assets/enemy.txt");
        damageSound = Gdx.audio.newSound(Gdx.files.internal("tiled/hit.mp3"));
        pickupSound = Gdx.audio.newSound(Gdx.files.internal("tiled/pickup.mp3"));

        player = new Player(playerTexture, damageSound, pickupSound);
        enemyManager = new EnemyManager(enemyTexture, damageSound, pickupSound);
        currentMap = MapFactory.createMap("tiled/starting.tmx", player, enemyManager);
        camera.zoom = currentMap.getDefaultZoom();
        player.setMap(currentMap);
        uiStage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json")); // Potrebuješ uiskin (glej spodaj)
        Gdx.input.setInputProcessor(uiStage);
        portalModal = new PortalModal();


    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Če ni odprt modal, izvajamo igro
        if (!questionModal.isActive() && !portalModal.isActive()) {
            currentMap.update(Gdx.graphics.getDeltaTime());

            if (currentMap.shouldSwitchMap()) {
                currentMap.dispose();
                currentMap = MapFactory.createMap(currentMap.getNextMapPath(), player, enemyManager);
                player.setMap(currentMap);
                camera.zoom = currentMap.getDefaultZoom();
            }

            if (currentMap.shouldCameraTrackPlayer()) {
                camera.position.set(player.getX() + 32, player.getY() + 32, 0);
            }
        }

        camera.update();

        if (currentMap.isSmallerScale()) {
            currentMap.render(camera, batch, fontSmaller);
        } else {
            currentMap.render(camera, batch, font);
        }

        // Risanje UI
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && !questionModal.isActive()) {
            QuestionMiniGame miniGame = new QuestionMiniGame();
            miniGame.load(); // Če je potrebno

            questionModal.show(uiStage, skin, miniGame,
                () -> System.out.println("Pravilen odgovor!"),
                () -> System.out.println("Napačen odgovor ali je potek čas!")
            );
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !portalModal.isActive()) {
            portalModal.show(uiStage, skin,
                () -> System.out.println("✅ Portal odprt!"),
                () -> System.out.println("❌ Portal zavrnjen!")
            );
        }
        // Tipka za prikaz modala

    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        damageSound.dispose();
        pickupSound.dispose();
        font.dispose();
        fontSmaller.dispose();
        currentMap.dispose();
        enemyTexture.dispose();
    }
}
