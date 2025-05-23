package io.github.some_example_name.igra.modalminigame;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface MiniGame {
    void render(SpriteBatch batch, BitmapFont font, OrthographicCamera camera);
    boolean isFinished();
}
