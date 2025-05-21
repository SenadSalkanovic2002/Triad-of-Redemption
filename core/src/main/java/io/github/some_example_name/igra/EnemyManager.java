package io.github.some_example_name.igra;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class EnemyManager {
    private final List<Enemy> enemies = new ArrayList<>();
    private final TextureAtlas texture;
    private final Sound damageSound, pickupSound;
    private BaseMap currentMap;

    public EnemyManager(TextureAtlas texture, Sound damageSound, Sound pickupSound) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
    }

    public void setMap(BaseMap map) {
        this.currentMap = map;
        for (Enemy enemy : enemies) {
            enemy.setMap(map);
        }
    }

    public void addEnemy(float x, float y) {
        Enemy enemy = new Enemy(texture, damageSound, pickupSound, x, y);
        if (currentMap != null) {
            enemy.setMap(currentMap);
        }
        enemies.add(enemy);
    }

    public void update(Player player, float delta) {
        enemies.removeIf(enemy -> {
            enemy.update(player, delta);
            if (enemy.getBounds().overlaps(player.getBounds())) {
                enemy.attackPlayer(player);
            }
            return enemy.isDead();
        });
    }

    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
