package io.github.some_example_name.igra.modalminigame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionMiniGame {

  public static class Question {
    public final String text;
    public final String[] options;
    public final int correctIndex;

    public Question(String text, String[] options, int correctIndex) {
      this.text = text;
      this.options = options;
      this.correctIndex = correctIndex;
    }
  }

  private Question currentQuestion;
  private static final List<Question> questionPool = new ArrayList<>();
  private static final Random random = new Random();

  public void load() {
    if (questionPool.isEmpty()) {
      fillQuestionPool();
    }

    currentQuestion = questionPool.get(random.nextInt(questionPool.size()));
  }

  private void fillQuestionPool() {
    questionPool.add(new Question("Koliko je 2 + 2?", new String[] {"3", "4", "5"}, 1));
    questionPool.add(
        new Question("Glavno mesto Francije je?", new String[] {"Berlin", "Madrid", "Pariz"}, 2));
    questionPool.add(new Question("Koliko nog ima pajek?", new String[] {"6", "8", "10"}, 1));
    questionPool.add(
        new Question(
            "Katera snov je tekoča pri sobni temperaturi?",
            new String[] {"Voda", "Led", "Para"},
            0));
    questionPool.add(
        new Question(
            "Največji planet v Osončju je?", new String[] {"Zemlja", "Jupiter", "Mars"}, 1));
    questionPool.add(new Question("Kaj je rezultat 3 * 5?", new String[] {"15", "10", "20"}, 0));
    questionPool.add(
        new Question(
            "Katero leto je začetek 2. svetovne vojne?", new String[] {"1939", "1945", "1914"}, 0));
    questionPool.add(
        new Question(
            "Katere barve ni v slovenski zastavi?", new String[] {"Rdeča", "Zelena", "Modra"}, 1));
    questionPool.add(new Question("Koliko kontinentov ima svet?", new String[] {"5", "6", "7"}, 2));
    questionPool.add(
        new Question(
            "Katero je največje morje?", new String[] {"Jadransko", "Sredozemsko", "Baltsko"}, 1));
  }

  public String getQuestion() {
    return currentQuestion.text;
  }

  public String[] getOptions() {
    return currentQuestion.options;
  }

  public int getCorrectIndex() {
    return currentQuestion.correctIndex;
  }
}
