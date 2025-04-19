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

public class Player {
    private float x, y, speed;
    private final Rectangle bounds;
    private final TextureAtlas texture;
    private final Sound damageSound, pickupSound;
    private int health, score;
    private boolean gameOver, gameWon, inSlowZone;
    private BaseMap map;
    private Animation<TextureRegion> idle, walkSide, walkUp, walkDown, attackSide, altAttackSide, attackUp, attackDown;
    private TextureRegion currentFrame; // current part of the animation drawn
    private float animationTime; // used to know which animation frame to play
    private String currentState; // remembers what animation is currently being played
    private String lastDirection; // will be used for the attack directions
    private boolean isAttacking = false; // makes the animation play fully when space is pressed

    private final ShapeRenderer shapeRenderer = new ShapeRenderer(); // debug thingy for the bounds rectangle

    public Player(TextureAtlas texture, Sound damageSound, Sound pickupSound) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
        this.bounds = new Rectangle(0, 0, 96, 96);
        this.speed = 100f;
        this.health = 100;
        this.score = 0;
        this.gameOver = false;
        this.gameWon = false;

        idle = new Animation<TextureRegion>(0.2f, texture.findRegions("idle"), Animation.PlayMode.LOOP);
        walkSide = new Animation<TextureRegion>(0.1f, texture.findRegions("walk"), Animation.PlayMode.LOOP);
        walkUp = new Animation<TextureRegion>(0.1f, texture.findRegions("walk_u"), Animation.PlayMode.LOOP);
        walkDown = new Animation<TextureRegion>(0.1f, texture.findRegions("walk_d"), Animation.PlayMode.LOOP);
        attackSide = new Animation<TextureRegion>(0.1f, texture.findRegions("atk"), Animation.PlayMode.NORMAL);
        altAttackSide = new Animation<TextureRegion>(0.1f, texture.findRegions("alt_atk"), Animation.PlayMode.NORMAL);
        attackUp = new Animation<TextureRegion>(0.1f, texture.findRegions("atk_u"), Animation.PlayMode.NORMAL);
        attackDown = new Animation<TextureRegion>(0.1f, texture.findRegions("atk_d"), Animation.PlayMode.NORMAL);
        currentState = "idle";
        lastDirection = "right";
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


    public void handleInput(float delta) {
        //also updates the attack direction
        float dx = 0, dy = 0;
        float currentSpeed = inSlowZone ? speed / 2 : speed;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += currentSpeed * delta;
            lastDirection = "up";
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= currentSpeed * delta;
            lastDirection = "down";
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= currentSpeed * delta;
            lastDirection = "left";
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += currentSpeed * delta;
            lastDirection = "right";
        }
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
        animationTime += Gdx.graphics.getDeltaTime();
        batch.draw(currentFrame, x-(bounds.width/2f), y-(bounds.height/2f));
        /*//debug rendering for the bounds rectangle
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();

        batch.begin();*/
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

    public void chooseAnimation() {
        boolean moving = Gdx.input.isKeyPressed(Input.Keys.W) ||
            Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.S) ||
            Gdx.input.isKeyPressed(Input.Keys.D);

        String newState;

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || isAttacking) {
            newState = "attack";
            isAttacking = true;
        } else if (moving) {
            newState = "walk";
        } else {
            newState = "idle";
        }

        if (!newState.equals(currentState)) {
            currentState = newState;
            animationTime = 0f; // reset animation when state changes
        }

        // Update current frame
        switch (currentState) {
            case "walk":
                currentFrame = walkSide.getKeyFrame(animationTime, true);
                break;
            case "attack":
                currentFrame = attackSide.getKeyFrame(animationTime, false);
                if (attackSide.isAnimationFinished(animationTime)) {
                    isAttacking = false;
                    currentState = "idle";
                    animationTime = 0;
                }
                break;
            default:
                currentFrame = idle.getKeyFrame(animationTime, true);
                break;
        }
    }
}
