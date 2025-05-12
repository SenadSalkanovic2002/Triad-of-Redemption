package io.github.some_example_name.igra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    private float x, y, speed;
    private final Rectangle bounds;
    private final TextureAtlas texture;
    private final Sound damageSound, pickupSound;
    private int health, score;
    private boolean gameOver, gameWon, inSlowZone;
    private BaseMap map;
    private Animation<TextureRegion> idle, walking, hurt, attack, jump, death;
    private TextureRegion currentFrame;
    private float animationTime;
    private PlayerState currentState;
    private PlayerDirection lastDirection;
    private boolean isAttacking = false;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public Enemy(TextureAtlas texture, Sound damageSound, Sound pickupSound, float startX, float startY) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
        this.bounds = new Rectangle(startX, startY, 64, 64);
        this.x = startX;
        this.y = startY;
        this.speed = GameConfig.PLAYER_SPEED;
        this.health = GameConfig.PLAYER_HEALTH;
        this.score = 0;
        this.gameOver = false;
        this.gameWon = false;

        // Initialize animations (same as before)
        idle = new Animation<>(GameConfig.PLAYER_ANIMATION_IDLE_DURATION, texture.findRegions(RegionNames.ENEMY_IDLE), Animation.PlayMode.LOOP);
        walking = new Animation<>(GameConfig.PLAYER_ANIMATION_WALKING_DURATION, texture.findRegions(RegionNames.ENEMY_WALKING), Animation.PlayMode.LOOP);
        hurt = new Animation<>(GameConfig.PLAYER_ANIMATION_IDLE_DURATION, texture.findRegions(RegionNames.ENEMY_HURT), Animation.PlayMode.LOOP);
        attack = new Animation<>(GameConfig.PLAYER_ANIMATION_ATTACKING_SPEED, texture.findRegions(RegionNames.ENEMY_ATTACKING), Animation.PlayMode.LOOP);
        jump = new Animation<>(GameConfig.PLAYER_ANIMATION_IDLE_DURATION, texture.findRegions(RegionNames.ENEMY_JUMPING), Animation.PlayMode.LOOP);
        death = new Animation<>(GameConfig.PLAYER_ANIMATION_IDLE_DURATION, texture.findRegions(RegionNames.ENEMY_DEAD), Animation.PlayMode.LOOP);



        currentState = PlayerState.IDLE;
        lastDirection = PlayerDirection.RIGHT;
        animationTime = 0f;
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

    public void update(float delta) {
        animationTime += delta;

        // Example logic to cycle through states for testing
        if (animationTime < 2) {
            currentState = PlayerState.IDLE;
        } else if (animationTime < 4) {
            currentState = PlayerState.WALKING;
        } else if (animationTime < 6) {
            currentState = PlayerState.ATTACKING;
        }
         else {
            animationTime = 0; // Reset time to loop states
        }

        // Example logic to set the current frame based on the state
        switch (currentState) {
            case IDLE:
                currentFrame = idle.getKeyFrame(animationTime);
                break;
            case WALKING:
                currentFrame = walking.getKeyFrame(animationTime);
                break;
            case ATTACKING:
                currentFrame = attack.getKeyFrame(animationTime);
                break;
            default:
                currentFrame = idle.getKeyFrame(animationTime);
        }

        // Example movement logic
        x += delta * 10;
        bounds.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        animationTime += Gdx.graphics.getDeltaTime();
        batch.draw(currentFrame, bounds.x, bounds.y, bounds.width, bounds.height); // Align texture with rectangle
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();

        batch.begin();
    }
}
