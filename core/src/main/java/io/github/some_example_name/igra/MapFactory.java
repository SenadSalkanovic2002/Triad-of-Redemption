package io.github.some_example_name.igra;

import io.github.some_example_name.igra.maps.BossArenaMap;
import io.github.some_example_name.igra.maps.LabyrinthMap;

public class MapFactory {
  public static BaseMap createMap(String mapPath, Player player, EnemyManager enemyManager) {
    //if (mapPath.endsWith("forest.tmx")) return new ForestMap(player);
    if (mapPath.endsWith("labirint.tmx")) return new LabyrinthMap(player, enemyManager);
    if (mapPath.endsWith("finalBoss.tmx")) return new BossArenaMap(player, enemyManager);

        return new DefaultMap(mapPath, player, enemyManager); // Fallback
    }
}
