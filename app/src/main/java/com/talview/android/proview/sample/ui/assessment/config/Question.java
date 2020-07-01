package com.talview.android.proview.sample.ui.assessment.config;

import java.util.ArrayList;
import java.util.List;

public class Question {

    private String question;
    private String answer = "";

    public Question(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public static List<Question> dummyQuestionList() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is your favourite color?"));
        questionList.add(new Question("which is your favourite movie?"));
        questionList.add(new Question("What is your favourite bird?"));
        questionList.add(new Question("How many languages you can speak?"));
        questionList.add(new Question("Write down your personal achievements"));
        return questionList;
    }
}
