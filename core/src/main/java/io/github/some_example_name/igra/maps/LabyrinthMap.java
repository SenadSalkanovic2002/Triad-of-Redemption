package io.github.some_example_name.igra.maps;


import io.github.some_example_name.igra.BaseMap;
import io.github.some_example_name.igra.EnemyManager;
import io.github.some_example_name.igra.Player;

public class LabyrinthMap extends BaseMap {
  public LabyrinthMap(Player player, EnemyManager enemyManager) {
    super("tiled/Labyrinth/Tiled_files/labirint.tmx", player, enemyManager, true);
  }

  @Override
  protected void setupAdditionalLayers() {}
}
