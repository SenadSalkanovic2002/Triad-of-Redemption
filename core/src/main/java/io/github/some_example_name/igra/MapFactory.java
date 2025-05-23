package io.github.some_example_name.igra;

import io.github.some_example_name.igra.maps.BossArenaMap;
import io.github.some_example_name.igra.maps.SkeletonMap;

public class MapFactory {
  public static BaseMap createMap(String mapPath, Player player, EnemyManager enemyManager) {

    if (mapPath.endsWith("labirint.tmx")) return new BossArenaMap(player, enemyManager);
    if (mapPath.endsWith("finalBoss.tmx")) return new BossArenaMap(player, enemyManager);
    if (mapPath.endsWith("dungeonMap.tmx")) return new SkeletonMap(player, enemyManager);

    return new DefaultMap(mapPath, player, enemyManager); // Fallback
  }
}
