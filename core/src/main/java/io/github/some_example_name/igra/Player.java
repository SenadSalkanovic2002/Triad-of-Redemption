package io.github.some_example_name.igra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
  private Animation<TextureRegion> idle,
      walkSide,
      walkUp,
      walkDown,
      attackSide,
      altAttackSide,
      attackUp,
      attackDown,
      idleUp,
      idleDown;
  private TextureRegion currentFrame; // current part of the animation drawn
  private float animationTime; // used to know which animation frame to play
  private CharacterState currentState; // remembers what animation is currently being played
  private CharacterDirection lastDirection; // will be used for the attack directions
  private boolean isAttacking = false; // makes the animation play fully when space is pressed
  private boolean isSmallerPlayer = false;
  private Texture visionMask;
  private Rectangle
      attackHitbox; // Hitbox rectangle, is exposed with a method to check for collision with
  // enemies

  private final ShapeRenderer shapeRenderer =
      new ShapeRenderer(); // debug thingy for the bounds rectangle

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

    idle =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.PLAYER_IDLE),
            Animation.PlayMode.LOOP);
    idleDown =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.PLAYER_IDLE_DOWN),
            Animation.PlayMode.LOOP);
    idleUp =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_IDLE_DURATION,
            texture.findRegions(RegionNames.PLAYER_IDLE_UP),
            Animation.PlayMode.LOOP);

    walkSide =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_WALKING),
            Animation.PlayMode.LOOP);
    walkUp =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_WALKING_UP),
            Animation.PlayMode.LOOP);
    walkDown =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_WALKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_WALKING_DOWN),
            Animation.PlayMode.LOOP);

    attackSide =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING),
            Animation.PlayMode.NORMAL);
    altAttackSide =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING_ALTERNATIVE),
            Animation.PlayMode.NORMAL);
    attackUp =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING_UP),
            Animation.PlayMode.NORMAL);
    attackDown =
        new Animation<TextureRegion>(
            GameConfig.PLAYER_ANIMATION_ATTACKING_DURATION,
            texture.findRegions(RegionNames.PLAYER_ATTACKING_DOWN),
            Animation.PlayMode.NORMAL);

    generateVisionMask(550);

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
    if (currentState == CharacterState.ATTACKING) return; // don't allow movement while attacking

    // also updates the attack direction
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

  public void attackEnemies(EnemyManager enemyManager) {
    if (currentState == CharacterState.ATTACKING) {
      for (Enemy enemy : enemyManager.getEnemies()) {
        if (bounds.overlaps(enemy.getBounds())) {
          enemy.takeDamage((int) GameConfig.PLAYER_DAMAGE);
        }
      }
    }

    // Reset the attack state when the animation ends
    if (attackSide.isAnimationFinished(animationTime)) {
      isAttacking = false;
      currentState = CharacterState.IDLE;
      animationTime = 0;
    }
  }

  public void takeDamage(int damage) {
    this.health -= damage;
    if (health <= 0) {
      gameOver = true;
    }
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

  public void checkTrap(MapObjects traps, long timeSinceLoad) {
    for (MapObject obj : traps) {
      if (obj instanceof RectangleMapObject) {
        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
        if (bounds.overlaps(rect) && timeSinceLoad % 2800 >= 2780) {
          this.health -= 10;
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

  private void generateVisionMask(int diameter) {
    Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
    int radius = diameter / 2;

    for (int y = 0; y < diameter; y++) {
      for (int x = 0; x < diameter; x++) {
        float dx = x - radius;
        float dy = y - radius;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float alpha = Math.min(1f, dist / radius);
        alpha = alpha * alpha;
        // Z tem lahko nastavljas črnobo okoli igralca npr. manjsi krog bolj temen Math.min(1f,
        // alpha * 10f)
        pixmap.setColor(0f, 0f, 0f, Math.min(0.91f, alpha * 5f));
        pixmap.drawPixel(x, y);
      }
    }

    visionMask = new Texture(pixmap);
    pixmap.dispose();
  }

  public void render(SpriteBatch batch) {
    animationTime += Gdx.graphics.getDeltaTime();

    if (isSmallerPlayer) {
      batch.draw(
          currentFrame,
          x - bounds.width,
          y - bounds.height,
          currentFrame.getRegionWidth() / 2.3f,
          currentFrame.getRegionHeight() / 2.3f);
    } else {
      batch.draw(currentFrame, x - (bounds.width / 2f), y - (bounds.height / 2f));
    }

    batch.end();

    // Debug rectangle
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.RED);
    shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

    if (isAttacking && attackHitbox != null) { // Draws the attack hitbox
      shapeRenderer.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
    }

    shapeRenderer.end();

    if (isSmallerPlayer && visionMask != null) {
      float maskSize = visionMask.getWidth();
      float centerX = x + bounds.width / 2f;
      float centerY = y + bounds.height / 2f;

      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

      batch.begin();
      batch.draw(visionMask, centerX - maskSize / 2f, centerY - maskSize / 2f);
      batch.end();

      Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    batch.begin();
  }

  private float dx() {
    return Gdx.input.isKeyPressed(Input.Keys.A)
        ? -speed * Gdx.graphics.getDeltaTime()
        : Gdx.input.isKeyPressed(Input.Keys.D) ? speed * Gdx.graphics.getDeltaTime() : 0;
  }

  private float dy() {
    return Gdx.input.isKeyPressed(Input.Keys.W)
        ? speed * Gdx.graphics.getDeltaTime()
        : Gdx.input.isKeyPressed(Input.Keys.S) ? -speed * Gdx.graphics.getDeltaTime() : 0;
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

  public void setIfSmallerPlayer(boolean isSmallerPlayer) {
    this.isSmallerPlayer = isSmallerPlayer;
    setBoundsByTypeOfPlayer();
    setSpeedByTypeOfPlayer();
  }

  public void setBoundsByTypeOfPlayer() {
    if (isSmallerPlayer) {
      this.bounds =
          new Rectangle(0, 0, GameConfig.PLAYER_WIDTH_SMALLER, GameConfig.PLAYER_HEIGHT_SMALLER);
    } else {
      this.bounds = new Rectangle(0, 0, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
    }
  }

  public void setSpeedByTypeOfPlayer() {
    if (isSmallerPlayer) {
      this.speed = GameConfig.PLAYER_SPEED_SMALLER;
    } else {
      this.speed = GameConfig.PLAYER_SPEED;
    }
  }

  public void chooseAnimation(EnemyManager enemyManager) {
    boolean isMoving =
        Gdx.input.isKeyPressed(Input.Keys.W)
            || Gdx.input.isKeyPressed(Input.Keys.A)
            || Gdx.input.isKeyPressed(Input.Keys.S)
            || Gdx.input.isKeyPressed(Input.Keys.D);

    CharacterState newState;

    if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || isAttacking) {
      newState = CharacterState.ATTACKING;
      isAttacking = true;
      attackEnemies(enemyManager);
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
      case WALKING:
        {
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
              // facingLeft = shouldFaceLeft;
              break;
          }
          break;
        }
      case ATTACKING:
        {
          this.attack();
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
            attackHitbox = null;
          }
          break;
        }
      default:
        {
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

  // Creates the rectangle for the attack hitbox, is called every frame while the attack is
  // happening
  private void attack() {
    float hitboxWidth = bounds.getWidth() / 2f;
    float hitboxHeight = bounds.getHeight() / 2f;
    float hitboxX = x;
    float hitboxY = y;

    switch (lastDirection) {
      case LEFT:
        hitboxHeight = bounds.getHeight();
        if (isSmallerPlayer) {
          hitboxHeight *= 1.5f;
          hitboxWidth *= 1.5f;
          hitboxY -= bounds.getHeight() * 0.25f;
        }
        hitboxX -= hitboxWidth;
        break;
      case RIGHT:
        hitboxHeight = bounds.getHeight();
        if (isSmallerPlayer) {
          hitboxHeight *= 1.5f;
          hitboxWidth *= 1.5f;
          hitboxY -= bounds.getHeight() * 0.25f;
        }
        hitboxX += bounds.getWidth();
        break;
      case UP:
        hitboxWidth = bounds.getWidth();
        if (isSmallerPlayer) {
          hitboxHeight *= 1.5f;
          hitboxWidth *= 1.5f;
          hitboxX -= bounds.getWidth() * 0.25f;
        }
        hitboxY += bounds.getHeight();
        break;
      case DOWN:
        hitboxWidth = bounds.getWidth();
        if (isSmallerPlayer) {
          hitboxHeight *= 1.5f;
          hitboxWidth *= 1.5f;
          hitboxX -= bounds.getWidth() * 0.25f;
        }
        hitboxY -= hitboxHeight;
        break;
    }

    attackHitbox = new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
  }

  // Will be used to return the hitbox to check if the player has successfully hit an enemy
  public Rectangle getAttackHitbox() {
    return isAttacking ? attackHitbox : null;
  }
}
