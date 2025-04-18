package io.github.some_example_name.igra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private float x, y, speed;
    private final Rectangle bounds;
    private final Texture texture;
    private final Sound damageSound, pickupSound;
    private int health, score;
    private boolean gameOver, gameWon, inSlowZone;
    private BaseMap map;

    public Player(Texture texture, Sound damageSound, Sound pickupSound) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
        this.bounds = new Rectangle(0, 0, 64, 64);
        this.speed = 100f;
        this.health = 100;
        this.score = 0;
        this.gameOver = false;
        this.gameWon = false;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.setPosition(x, y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setMap(BaseMap map) {
        this.map = map;
    }


    public void handleInput(float delta) {
        float dx = 0, dy = 0;
        float currentSpeed = inSlowZone ? speed / 2 : speed;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += currentSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= currentSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= currentSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += currentSpeed * delta;
        x += dx;
        y += dy;
        bounds.setPosition(x, y);
    }

    public void checkCollisions(MapObjects objects) {
        for (MapObject obj : objects) {
            if (obj instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                if (bounds.overlaps(rect)) {
                    x -= dx();
                    y -= dy();
                    bounds.setPosition(x, y);
                    return;
                }
            }
        }
    }

    public void checkEnd(MapObjects endObjects) {
        for (MapObject obj : endObjects) {
            if (obj instanceof RectangleMapObject) {
                if (((RectangleMapObject) obj).getRectangle().overlaps(bounds)) {
                    gameWon = true;
                }
            }
        }
    }

    public void checkPickup(TiledMapTileLayer pickup, TiledMapTileLayer damage) {
        int tileX = (int)(x / pickup.getTileWidth());
        int tileY = (int)(y / pickup.getTileHeight());

        if (pickup.getCell(tileX, tileY) != null) {
            pickup.setCell(tileX, tileY, null);
            score += 10;
            pickupSound.play();
        }

        if (damage.getCell(tileX, tileY) != null) {
            damage.setCell(tileX, tileY, null);
            health -= 34;
            damageSound.play();
        }

        if (health <= 0) gameOver = true;
    }

    public void checkBridge(MapObjects bridgeObjects) {
        inSlowZone = false;
        for (MapObject obj : bridgeObjects) {
            if (obj instanceof RectangleMapObject && "speed".equals(obj.getName())) {
                if (((RectangleMapObject) obj).getRectangle().overlaps(bounds)) {
                    inSlowZone = true;
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, bounds.getWidth(), bounds.getHeight());
    }

    private float dx() {
        return Gdx.input.isKeyPressed(Input.Keys.A) ? -speed * Gdx.graphics.getDeltaTime() :
            Gdx.input.isKeyPressed(Input.Keys.D) ?  speed * Gdx.graphics.getDeltaTime() : 0;
    }

    private float dy() {
        return Gdx.input.isKeyPressed(Input.Keys.W) ?  speed * Gdx.graphics.getDeltaTime() :
            Gdx.input.isKeyPressed(Input.Keys.S) ? -speed * Gdx.graphics.getDeltaTime() : 0;
    }

    public int getHealth() { return health; }
    public int getScore() { return score; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
