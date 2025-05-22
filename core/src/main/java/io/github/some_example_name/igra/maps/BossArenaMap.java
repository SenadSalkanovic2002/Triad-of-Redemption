package io.github.some_example_name.igra.maps;

import io.github.some_example_name.igra.BaseMap;
import io.github.some_example_name.igra.EnemyManager;
import io.github.some_example_name.igra.Player;

public class BossArenaMap extends BaseMap {
    public BossArenaMap(Player player, EnemyManager enemyManager) {
        super("tiled/BossArena/finalBoss.tmx", player, enemyManager, true);
    }

    @Override
    protected void setupAdditionalLayers() {

    }
}
