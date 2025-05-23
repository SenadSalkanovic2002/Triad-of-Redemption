package io.github.some_example_name.igra.modalminigame;

import com.badlogic.gdx.Input;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MechanicalPortalMiniGame {

  public enum Type {
    WORD_UNLOCK,
    BUTTON_PATTERN
  }

  private Type type;
  private String targetWord;
  private StringBuilder inputWord = new StringBuilder();

  private List<Integer> buttonPattern = new ArrayList<>();
  private List<Integer> playerPressed = new ArrayList<>();

  private float timeLimitSeconds;
  private float timeLeft;

  public void loadRandomGame() {
    Random rnd = new Random();
    if (rnd.nextBoolean()) {
      type = Type.WORD_UNLOCK;
      targetWord = "ODPRI";
      inputWord.setLength(0);
    } else {
      type = Type.BUTTON_PATTERN;
      buttonPattern.clear();
      buttonPattern.add(Input.Keys.A);
      buttonPattern.add(Input.Keys.S);
      buttonPattern.add(Input.Keys.D);
      playerPressed.clear();
    }
    timeLimitSeconds = 5f;
    timeLeft = timeLimitSeconds;
  }

  public Type getType() {
    return type;
  }

  public float getTimeLeft() {
    return timeLeft;
  }

  public void update(float delta) {
    timeLeft -= delta;
  }

  public boolean isTimeUp() {
    return timeLeft <= 0;
  }

  public String getTargetWord() {
    return targetWord;
  }

  public void onKeyTyped(char character) {
    if (type != Type.WORD_UNLOCK) return;
    inputWord.append(Character.toUpperCase(character));
  }

  public boolean isWordCorrect() {
    return inputWord.toString().equals(targetWord);
  }

  public void onKeyPressed(int keycode) {
    if (type != Type.BUTTON_PATTERN) return;
    if (playerPressed.size() < buttonPattern.size()) {
      playerPressed.add(keycode);
    }
  }

  public boolean isPatternCorrect() {
    if (playerPressed.size() != buttonPattern.size()) return false;
    for (int i = 0; i < buttonPattern.size(); i++) {
      if (!playerPressed.get(i).equals(buttonPattern.get(i))) return false;
    }
    return true;
  }
}
