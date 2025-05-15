package io.github.some_example_name.igra.modalminigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;

import io.github.some_example_name.igra.modalminigame.MechanicalPortalMiniGame;

public class PortalModal {

    private boolean active = false;
    private Dialog dialog;
    private MechanicalPortalMiniGame miniGame;
    private Timer.Task timeoutTask;

    public boolean isActive() {
        return active;
    }

    public void show(Stage uiStage, Skin skin, Runnable onSuccess, Runnable onFailure) {
        if (active) return;

        miniGame = new MechanicalPortalMiniGame();
        miniGame.loadRandomGame();

        String labelText = miniGame.getType() == MechanicalPortalMiniGame.Type.WORD_UNLOCK
            ? "Natipkaj besedo: " + miniGame.getTargetWord()
            : "Pritisni zaporedje: A, S, D";

        dialog = new Dialog("Mehanični portal (5s)", skin);
        dialog.text(new Label(labelText, skin));
        dialog.button("Odnehaj", false);
        dialog.show(uiStage);
        active = true;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (miniGame.getType() == MechanicalPortalMiniGame.Type.WORD_UNLOCK) {
                    miniGame.onKeyTyped(character);
                    if (miniGame.isWordCorrect()) {
                        finish(true, onSuccess, onFailure);
                    }
                }
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (miniGame.getType() == MechanicalPortalMiniGame.Type.BUTTON_PATTERN) {
                    miniGame.onKeyPressed(keycode);
                    if (miniGame.isPatternCorrect()) {
                        finish(true, onSuccess, onFailure);
                    }
                }
                return true;
            }
        });

        // Timeout
        timeoutTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                finish(false, onSuccess, onFailure);
            }
        }, 5);
    }

    private void finish(boolean success, Runnable onSuccess, Runnable onFailure) {
        if (!active) return;
        active = false;
        if (timeoutTask != null) timeoutTask.cancel();
        dialog.hide();
        if (success) onSuccess.run();
        else onFailure.run();
    }
}
