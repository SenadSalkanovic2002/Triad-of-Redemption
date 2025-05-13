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
    private Rectangle bounds;
    private final TextureAtlas texture;
    private final Sound damageSound, pickupSound;
    private int health, score;
    private boolean gameOver, gameWon, inSlowZone;
    private BaseMap map;
    private Animation<TextureRegion> idle, walkSide, walkUp, walkDown, attackSide, altAttackSide, attackUp, attackDown, idleUp, idleDown;
    private TextureRegion currentFrame; // current part of the animation drawn
    private float animationTime; // used to know which animation frame to play
    private CharacterState currentState; // remembers what animation is currently being played
    private CharacterDirection lastDirection; // will be used for the attack directions
    private boolean isAttacking = false; // makes the animation play fully when space is pressed
    private boolean isSmallerPlayer = false;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer(); // debug thingy for the bounds rectangle

    public Player(TextureAtlas texture, Sound damageSound, Sound pickupSound) {
        this.texture = texture;
        this.damageSound = damageSound;
        this.pickupSound = pickupSound;
        this.speed = GameConfig.PLAYER_SPEED;
        this.health = GameConfig.PLAYER_HEALTH;
        this.score = 0;
        this.gameOver = false;
        this.gameWon = false;
        setBoundsByTypeOfPlayer();
        setSpeedByTypeOfPlayer();

        idle = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.PLAYER_IDLE),
            Animation.PlayMode.LOOP
        );
        idleDown = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.PLAYER_IDLE_DOWN),
            Animation.PlayMode.LOOP
        );
        idleUp = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.PLAYER_IDLE_UP),
            Animation.PlayMode.LOOP
        );

        walkSide = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_WALKING),
            Animation.PlayMode.LOOP
        );
        walkUp = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_WALKING_UP),
            Animation.PlayMode.LOOP
        );
        walkDown = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_WALKING_DOWN),
            Animation.PlayMode.LOOP
        );

        attackSide = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING),
            Animation.PlayMode.NORMAL
        );
        altAttackSide = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING_ALTERNATIVE),
            Animation.PlayMode.NORMAL
        );
        attackUp = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING_UP),
            Animation.PlayMode.NORMAL
        );
        attackDown = new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING_DOWN),
            Animation.PlayMode.NORMAL
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


    public void handleInput(float delta) {
        if (currentState == CharacterState.ATTACKING)
            return; // don't allow movement while attacking

        //also updates the attack direction
        float dx = 0, dy = 0;
        float currentSpeed = inSlowZone ? speed / 2 : speed;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += currentSpeed * delta;
            lastDirection = CharacterDirection.UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= currentSpeed * delta;
            lastDirection = CharacterDirection.DOWN;
        }
        // This makes the animation/attacking priority be to the side
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= currentSpeed * delta;
            lastDirection = CharacterDirection.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += currentSpeed * delta;
            lastDirection = CharacterDirection.RIGHT;
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
        int tileX = (int) (x / pickup.getTileWidth());
        int tileY = (int) (y / pickup.getTileHeight());

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
        if (isSmallerPlayer) {
            batch.draw(currentFrame, x - (bounds.width), y - (bounds.height), currentFrame.getRegionWidth() / 2.3f, currentFrame.getRegionHeight() / 2.3f);
        } else {
            batch.draw(currentFrame, x - (bounds.width / 2f), y - (bounds.height / 2f));
        }

        //debug rendering for the bounds rectangle
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();

        batch.begin();
    }

    private float dx() {
        return Gdx.input.isKeyPressed(Input.Keys.A) ? -speed * Gdx.graphics.getDeltaTime() :
            Gdx.input.isKeyPressed(Input.Keys.D) ? speed * Gdx.graphics.getDeltaTime() : 0;
    }

    private float dy() {
        return Gdx.input.isKeyPressed(Input.Keys.W) ? speed * Gdx.graphics.getDeltaTime() :
            Gdx.input.isKeyPressed(Input.Keys.S) ? -speed * Gdx.graphics.getDeltaTime() : 0;
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setTypeOfPlayer(boolean isSmallerPlayer) {
        this.isSmallerPlayer = isSmallerPlayer;
        setBoundsByTypeOfPlayer();
        setSpeedByTypeOfPlayer();
    }

    public void setBoundsByTypeOfPlayer(){
        if (isSmallerPlayer) {
            this.bounds = new Rectangle(0, 0, GameConfig.PLAYER_WIDTH_SMALLER, GameConfig.PLAYER_HEIGHT_SMALLER);
        } else {
            this.bounds = new Rectangle(0, 0, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        }
    }

    public void setSpeedByTypeOfPlayer(){
        if (isSmallerPlayer){
            this.speed = GameConfig.PLAYER_SPEED_SMALLER;
        } else {
            this.speed = GameConfig.PLAYER_SPEED;
        }
    }

    public void chooseAnimation() {
        boolean isMoving = Gdx.input.isKeyPressed(Input.Keys.W) ||
            Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.S) ||
            Gdx.input.isKeyPressed(Input.Keys.D);

        CharacterState newState;

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || isAttacking) {
            newState = CharacterState.ATTACKING;
            isAttacking = true;
        } else if (isMoving) {
            newState = CharacterState.WALKING;
        } else {
            newState = CharacterState.IDLE;
        }

        if (!newState.equals(currentState)) {
            currentState = newState;
            animationTime = 0f; // reset animation when state changes
        }

        // Update current frame
        switch (currentState) {
            case WALKING: {
                switch (lastDirection) {
                    case UP:
                        currentFrame = walkUp.getKeyFrame(animationTime, true);
                        break;
                    case DOWN:
                        currentFrame = walkDown.getKeyFrame(animationTime, true);
                        break;
                    case LEFT:
                    case RIGHT:
                        currentFrame = walkSide.getKeyFrame(animationTime, true);
                        boolean shouldFaceLeft = lastDirection.equals(CharacterDirection.LEFT);
                        if (currentFrame.isFlipX() != shouldFaceLeft) {
                            currentFrame.flip(true, false);
                        }
                        //facingLeft = shouldFaceLeft;
                        break;
                }
                break;
            }
            case ATTACKING: {
                switch (lastDirection) {
                    case UP:
                        currentFrame = attackUp.getKeyFrame(animationTime, false);
                        break;
                    case DOWN:
                        currentFrame = attackDown.getKeyFrame(animationTime, false);
                        break;
                    case LEFT:
                    case RIGHT:
                        currentFrame = attackSide.getKeyFrame(animationTime, false);
                        boolean shouldFaceLeft = lastDirection.equals(CharacterDirection.LEFT);
                        if (currentFrame.isFlipX() != shouldFaceLeft) {
                            currentFrame.flip(true, false);
                        }
                        break;
                }
                if (attackSide.isAnimationFinished(animationTime)) {
                    isAttacking = false;
                    currentState = CharacterState.IDLE;
                    animationTime = 0;
                }
                break;
            }
            default: {
                switch (lastDirection) {
                    case UP:
                        currentFrame = idleUp.getKeyFrame(animationTime, true);
                        break;
                    case DOWN:
                        currentFrame = idleDown.getKeyFrame(animationTime, true);
                        break;
                    case LEFT:
                    case RIGHT:
                        currentFrame = idle.getKeyFrame(animationTime, true);
                        boolean shouldFaceLeft = lastDirection.equals(CharacterDirection.LEFT);
                        if (currentFrame.isFlipX() != shouldFaceLeft) {
                            currentFrame.flip(true, false);
                        }
                        break;
                }
                break;
            }
        }
    }
}
