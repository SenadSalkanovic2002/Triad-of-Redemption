package io.github.some_example_name.igra;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class BaseMap {
    protected TiledMap map;
    protected OrthogonalTiledMapRenderer renderer;
    protected MapObjects collisions, endZones, bridges, nextMapTriggers;
    protected TiledMapTileLayer pickupLayer, damageLayer;
    protected String nextMapPath;
    protected boolean switchMap;
    protected final Player player;

    protected float defaultZoom = 1f;
    protected boolean cameraTracksPlayer = true;

    public BaseMap(String mapPath, Player player) {
        this.player = player;
        map = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(map);

        switchMap = false;
        nextMapPath = null;

        collisions = getLayerObjects("wall");
        endZones = getLayerObjects("end");
        bridges = getLayerObjects("bridgee");
        nextMapTriggers = getLayerObjects("next_map");

        for (MapObject obj : map.getLayers().get("start").getObjects()) {
            if (obj instanceof RectangleMapObject && "Start".equals(obj.getName())) {
                Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                player.setPosition(rect.x, rect.y);
            }
        }

        setupAdditionalLayers();
    }

    protected abstract void setupAdditionalLayers();

    private MapObjects getLayerObjects(String name) {
        MapLayer layer = map.getLayers().get(name);
        return layer != null ? layer.getObjects() : new MapObjects();
    }

    public void update(float delta) {
        player.handleInput(delta);
        if (checkTransition()) switchMap = true;
    }

    private boolean checkTransition() {
        Rectangle bounds = player.getBounds();
        for (MapObject obj : nextMapTriggers) {
            if (obj instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                if (rect.overlaps(bounds)) {
                    String newMap = obj.getName();
                    if (newMap != null && newMap.endsWith(".tmx")) {
                        nextMapPath = "tiled/" + newMap;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void render(OrthographicCamera camera, SpriteBatch batch, BitmapFont font) {
        camera.update();
        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        font.draw(batch, "HEALTH: " + player.getHealth(), 10, 560);
        font.draw(batch, "SCORE: " + player.getScore(), 10, 540);
        if (player.isGameOver()) font.draw(batch, "GAME OVER", 400, 300);
        else if (player.isGameWon()) font.draw(batch, "YOU WIN!", 400, 300);
        batch.end();

        player.checkCollisions(collisions);
        player.checkEnd(endZones);
        player.checkBridge(bridges);
    }

    public boolean shouldSwitchMap() {
        return switchMap;
    }

    public String getNextMapPath() {
        return nextMapPath;
    }

    public float getDefaultZoom() {
        return defaultZoom;
    }

    public boolean shouldCameraTrackPlayer() {
        return cameraTracksPlayer;
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
