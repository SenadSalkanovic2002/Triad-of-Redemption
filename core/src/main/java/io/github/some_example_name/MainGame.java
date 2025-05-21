package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import io.github.some_example_name.igra.*;

public class MainGame extends ApplicationAdapter {
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

        playerTexture = new TextureAtlas("assets/player.txt");
        enemyTexture = new TextureAtlas("assets/enemy.txt");
        damageSound = Gdx.audio.newSound(Gdx.files.internal("tiled/hit.mp3"));
        pickupSound = Gdx.audio.newSound(Gdx.files.internal("tiled/pickup.mp3"));

        player = new Player(playerTexture, damageSound, pickupSound);
        enemyManager = new EnemyManager(enemyTexture, damageSound, pickupSound);
        currentMap = MapFactory.createMap("tiled/starting.tmx", player, enemyManager);
        camera.zoom = currentMap.getDefaultZoom();
        player.setMap(currentMap);
    }

    @Override
    public void render() {
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

        camera.update();
        if (currentMap.isSmallerScale()){
            currentMap.render(camera, batch, fontSmaller);
        } else {
            currentMap.render(camera, batch, font);
        }

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
