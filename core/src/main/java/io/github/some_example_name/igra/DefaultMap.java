package io.github.some_example_name.igra;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class DefaultMap extends BaseMap {
  public DefaultMap(String mapPath, Player player, EnemyManager enemyManager) {
    super(mapPath, player, enemyManager, false);
  }

  @Override
  protected void setupAdditionalLayers() {
    pickupLayer = (TiledMapTileLayer) map.getLayers().get("pickup");
    damageLayer = (TiledMapTileLayer) map.getLayers().get("pickup_damage");
  }

  @Override
  public void render(
      com.badlogic.gdx.graphics.OrthographicCamera camera,
      com.badlogic.gdx.graphics.g2d.SpriteBatch batch,
      com.badlogic.gdx.graphics.g2d.BitmapFont font) {
    super.render(camera, batch, font);
    player.checkPickup(pickupLayer, damageLayer);
  }
}
