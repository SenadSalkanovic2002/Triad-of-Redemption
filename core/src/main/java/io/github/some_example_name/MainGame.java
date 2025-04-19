package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import io.github.some_example_name.igra.BaseMap;
import io.github.some_example_name.igra.MapFactory;
import io.github.some_example_name.igra.Player;

public class MainGame extends ApplicationAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TextureAtlas playerTexture;
    private Sound damageSound, pickupSound;
    private BitmapFont font;
    private Player player;
    private BaseMap currentMap;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        batch = new SpriteBatch();
        font = new BitmapFont();
        playerTexture = new TextureAtlas("assets/player.txt");
        damageSound = Gdx.audio.newSound(Gdx.files.internal("tiled/hit.mp3"));
        pickupSound = Gdx.audio.newSound(Gdx.files.internal("tiled/pickup.mp3"));

        player = new Player(playerTexture, damageSound, pickupSound);
        currentMap = MapFactory.createMap("tiled/starting.tmx", player);
        camera.zoom = currentMap.getDefaultZoom();
        player.setMap(currentMap);
    }

    @Override
    public void render() {
        currentMap.update(Gdx.graphics.getDeltaTime());

        if (currentMap.shouldSwitchMap()) {
            currentMap.dispose();
            currentMap = MapFactory.createMap(currentMap.getNextMapPath(), player);
            player.setMap(currentMap);
            camera.zoom = currentMap.getDefaultZoom();
        }

        if (currentMap.shouldCameraTrackPlayer()) {
            camera.position.set(player.getX() + 32, player.getY() + 32, 0);
        }

        camera.update();
        currentMap.render(camera, batch, font);
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        damageSound.dispose();
        pickupSound.dispose();
        font.dispose();
        currentMap.dispose();
    }
}
