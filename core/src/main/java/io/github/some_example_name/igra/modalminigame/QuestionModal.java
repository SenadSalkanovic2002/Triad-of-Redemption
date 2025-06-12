package io.github.some_example_name.igra.modalminigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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

        Dialog dialog = new Dialog("Vprašanje", skin) {
            @Override
            protected void result(Object object) {
                active = false;
                if (timeoutTask != null) timeoutTask.cancel();
                Gdx.input.setInputProcessor(previousProcessor); // povrni prejšnji processor

                int selectedIndex = (int) object;
                if (selectedIndex == correctIndex) {
                    onCorrect.run();
                } else {
                    onWrong.run();
                }
            }
        };

        // Padding
        dialog.pad(20);
        dialog.getContentTable().pad(15);
        dialog.getButtonTable().pad(15);
        TextButton.TextButtonStyle style = skin.get(TextButton.TextButtonStyle.class);

        dialog.text(new Label(question, skin));
        for (int i = 0; i < answers.length; i++) {
            String label = answers[i];
            if (i == 0) label = "[ Y ] " + label;
            else if (i == 1) label = "[ X ] " + label;
            else if (i == 2) label = "[ C ] " + label;
            TextButton btn = new TextButton(label, style);
            btn.pad(100); // <- tukaj dodaš padding znotraj gumba

            dialog.button(label, i);
        }

        dialog.key(Input.Keys.ESCAPE, -1);
        dialog.show(uiStage);
        active = true;

        // Shranimo prejšnji InputProcessor
        previousProcessor = Gdx.input.getInputProcessor();

        // Uporabimo svojega
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (!active) return false;

                int selectedIndex = -1;
                if (keycode == Input.Keys.Z) selectedIndex = 0;
                else if (keycode == Input.Keys.X) selectedIndex = 1;
                else if (keycode == Input.Keys.C) selectedIndex = 2;

                if (selectedIndex >= 0 && selectedIndex < answers.length) {
                    active = false;
                    if (timeoutTask != null) timeoutTask.cancel();
                    Gdx.input.setInputProcessor(previousProcessor); // povrni prejšnji
                    if (selectedIndex == correctIndex) {
                        onCorrect.run();
                    } else {
                        onWrong.run();
                    }
                    dialog.hide();
                    return true;
                }

                return false;
            }
        });

        // Timeout po 10 sekundah
        timeoutTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (active) {
                    active = false;
                    Gdx.input.setInputProcessor(previousProcessor); // povrni prejšnji
                    dialog.hide();
                    onWrong.run();
                }
            }
        }, 10);
    }

    private InputProcessor previousProcessor;
}
