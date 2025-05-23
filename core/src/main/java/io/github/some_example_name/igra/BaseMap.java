package io.github.some_example_name.igra;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.Date;

public abstract class BaseMap {
  protected TiledMap map;
  protected OrthogonalTiledMapRenderer renderer;
  protected MapObjects collisions, endZones, bridges, nextMapTriggers, traps;
  protected MapProperties tmp;
  protected TiledMapTileLayer pickupLayer, damageLayer, trapLayer;
  protected String nextMapPath;
  protected boolean switchMap;
  protected final Player player;
  protected EnemyManager enemyManager;

  protected float defaultZoom = 1f;
  protected boolean cameraTracksPlayer = true;
  protected boolean isSmallerScale = false;
  protected long timeOfLoad = 0;

  public BaseMap(String mapPath, Player player, EnemyManager enemyManager, boolean isSmallerScale) {
    this.player = player;
    this.enemyManager = enemyManager;
    this.isSmallerScale = isSmallerScale;
    timeOfLoad = new Date().getTime();
    map = new TmxMapLoader().load(mapPath);
    renderer = new OrthogonalTiledMapRenderer(map);

    if (isSmallerScale) {
      player.setIfSmallerPlayer(true);
      setDefaultZoom(0.4f);
    } else {
      player.setIfSmallerPlayer(false);
    }

    switchMap = false;
    nextMapPath = null;

    collisions = getLayerObjects("wall");
    traps = getLayerObjects("traps");
    endZones = getLayerObjects("end");
    bridges = getLayerObjects("bridgee");
    nextMapTriggers = getLayerObjects("next_map");

    player.setMap(this);
    enemyManager.setMap(this);

    for (MapObject obj : map.getLayers().get("start").getObjects()) {
      if (obj instanceof RectangleMapObject && "Start".equals(obj.getName())) {
        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
        player.setPosition(rect.x, rect.y);
        // Only spawn enemies in SkeletonMap
        if (this instanceof io.github.some_example_name.igra.maps.SkeletonMap) {
          enemyManager.addEnemy(rect.x, rect.y);
        }
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
    player.chooseAnimation(
        enemyManager); // updates the current animation on every render, should be used in other
    // maps as well
    enemyManager.update(player, delta);
    if (checkTransition()) switchMap = true;
  }

  private boolean checkTransition() {
    Rectangle bounds = player.getBounds();
    for (MapObject obj : nextMapTriggers) {
      if (obj instanceof RectangleMapObject) {
        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
        if (rect.overlaps(bounds)) {
          // Only allow next_map if all skeletons are dead in SkeletonMap
          if (this instanceof io.github.some_example_name.igra.maps.SkeletonMap) {
            if (!enemyManager.getEnemies().isEmpty()) {
              return false;
            }
          }
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

  public void setDefaultZoom(float defaultZoom) {
    this.defaultZoom = defaultZoom;
  }

  public boolean isSmallerScale() {
    return isSmallerScale;
  }

  public void render(OrthographicCamera camera, SpriteBatch batch, BitmapFont font) {
    camera.update();
    renderer.setView(camera);
    renderer.render();

    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    player.render(batch);
    enemyManager.render(batch);

    float fontX = camera.position.x - camera.viewportWidth / (2 / camera.zoom);
    float fontYHealth = camera.position.y - camera.viewportHeight / (2 / camera.zoom);
    float fontYScore = fontYHealth;

    if (isSmallerScale) {
      fontX += 5;
      fontYScore += 20;
      fontYHealth += 30;
    } else {
      fontX += 10;
      fontYScore += 40;
      fontYHealth += 60;
    }

    font.draw(batch, "HEALTH: " + player.getHealth(), fontX, fontYHealth);
    font.draw(batch, "SCORE: " + player.getScore(), fontX, fontYScore);

    if (player.isGameOver()) font.draw(batch, "GAME OVER", 400, 300);
    else if (player.isGameWon()) font.draw(batch, "YOU WIN!", 400, 300);
    batch.end();

    player.checkFinalBoss();
    player.checkCollisions(collisions);
    player.checkTrap(traps, timeSinceLoad());
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

  public long timeSinceLoad() {
    return new Date().getTime() - timeOfLoad;
  }

  public boolean shouldCameraTrackPlayer() {
    return cameraTracksPlayer;
  }

  public void dispose() {
    map.dispose();
    renderer.dispose();
  }
}
