package io.github.some_example_name.igra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private CharacterState currentState;
    private CharacterDirection lastDirection;
    private boolean isAttacking = false;
    private boolean isFacingLeft = false;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public Enemy(TextureAtlas texture, Sound damageSound, Sound pickupSound, float startX, float startY) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
        this.bounds = new Rectangle(startX, startY, GameConfig.ENEMY_WIDTH, GameConfig.ENEMY_HEIGHT);
        this.x = startX;
        this.y = startY;
        this.speed = GameConfig.ENEMY_SPEED;
        this.health = GameConfig.ENEMY_HEALTH;
        this.score = 0;
        this.gameOver = false;
        this.gameWon = false;

        // Initialize animations (same as before)
        idle = new Animation<>(
            GameConfig.ENEMY_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.ENEMY_IDLE),
            Animation.PlayMode.LOOP
        );
        walking = new Animation<>(
            GameConfig.ENEMY_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.ENEMY_WALKING),
            Animation.PlayMode.LOOP
        );
        hurt = new Animation<>(
            GameConfig.ENEMY_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.ENEMY_HURT),
            Animation.PlayMode.LOOP
        );
        attack = new Animation<>(
            GameConfig.ENEMY_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.ENEMY_ATTACKING),
            Animation.PlayMode.LOOP
        );
        jump = new Animation<>(
            GameConfig.ENEMY_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.ENEMY_JUMPING),
            Animation.PlayMode.LOOP
        );
        death = new Animation<>(
            GameConfig.ENEMY_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.ENEMY_DEAD),
            Animation.PlayMode.LOOP
        );

        currentState = CharacterState.IDLE;
        lastDirection = CharacterDirection.RIGHT;
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

    public void update(Player player, float delta) {
        animationTime += delta;

        // Get direction vector to player
        float directionX = player.getX() - x;
        float directionY = player.getY() - y;

        // Calculate distance to player
        float distanceToPlayer = (float) Math.sqrt(directionX * directionX + directionY * directionY);

        // Only take action if player is within vision radius
        if (distanceToPlayer <= GameConfig.ENEMY_VISION_DISTANCE) {
            // Player is within vision radius

            // Set the state based on distance and movement
            if (distanceToPlayer > GameConfig.ENEMY_ATTACK_DISTANCE) { // Not too close to the player
                // Normalize direction vector
                directionX /= distanceToPlayer;
                directionY /= distanceToPlayer;

                isFacingLeft = directionX < 0;

                // Move toward player
                x += directionX * GameConfig.ENEMY_SPEED * delta;
                y += directionY * GameConfig.ENEMY_SPEED * delta;

                currentState = CharacterState.WALKING;
            } else { // Close enough to attack
                currentState = CharacterState.ATTACKING;
            }
        } else {
            // Player is outside vision radius, enemy stays idle
            currentState = CharacterState.IDLE;
        }

        // Set the current frame based on the state
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

        // Update collision bounds
        bounds.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        animationTime += Gdx.graphics.getDeltaTime();

        // Calculate draw parameters based on facing direction
        float drawX = isFacingLeft ? bounds.x + bounds.width : bounds.x;
        float drawWidth = isFacingLeft ? -bounds.width : bounds.width;

        // Draw the sprite with appropriate flipping
        batch.draw(currentFrame, drawX, bounds.y, drawWidth, bounds.height);


//        batch.end();
//
//        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        shapeRenderer.setColor(Color.RED);
//        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
//        shapeRenderer.end();
//
//        batch.begin();
    }
}
