package io.github.some_example_name.igra.maps;

import com.badlogic.gdx.maps.MapObjects;

import io.github.some_example_name.igra.BaseMap;
import io.github.some_example_name.igra.EnemyManager;
import io.github.some_example_name.igra.Player;

public class SkeletonMap extends BaseMap {
  public SkeletonMap(Player player, EnemyManager enemyManager) {
    super("tiled/dungeonMap.tmx", player, enemyManager, true);
  }

  @Override
  protected void setupAdditionalLayers() {
  }
}
