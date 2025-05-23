package io.github.some_example_name.igra.modalminigame;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;

public class QuestionModal {

  private boolean active = false;
  private Timer.Task timeoutTask;

  public boolean isActive() {
    return active;
  }

  public void show(
      Stage uiStage, Skin skin, QuestionMiniGame miniGame, Runnable onCorrect, Runnable onWrong) {
    if (active) return;

    String question = miniGame.getQuestion();
    String[] answers = miniGame.getOptions();
    int correctIndex = miniGame.getCorrectIndex();

    Dialog dialog =
        new Dialog("Vprašanje", skin) {
          @Override
          protected void result(Object object) {
            active = false;

            if (timeoutTask != null) timeoutTask.cancel();
            int selectedIndex = (int) object;
            if (selectedIndex == correctIndex) {
              onCorrect.run();
            } else {
              onWrong.run();
            }
          }
        };

    dialog.text(new Label(question, skin));

    for (int i = 0; i < answers.length; i++) {
      final int index = i;
      dialog.button(answers[i], index);
    }

    dialog.key(Input.Keys.ESCAPE, -1); // -1 pomeni prekinjeno

    dialog.show(uiStage);
    active = true;
    // Timeout po 10 sekundah
    timeoutTask =
        Timer.schedule(
            new Timer.Task() {
              @Override
              public void run() {
                if (active) {
                  dialog.hide(); // skrij modal
                  active = false;
                  onWrong.run(); // šteje kot napačen odgovor
                }
              }
            },
            10); // 10 sekund
  }
}
