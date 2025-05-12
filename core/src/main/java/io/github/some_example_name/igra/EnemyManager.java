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

    public EnemyManager(TextureAtlas texture, Sound damageSound, Sound pickupSound) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
    }

    public void addEnemy(float x, float y) {
        enemies.add(new Enemy(texture, damageSound, pickupSound, x, y));
    }

    public void update(Player player, float delta) {
        for (Enemy enemy : enemies) {
            enemy.update(player, delta);
        }
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
