package com.talview.android.proview.sample.ui.assessment.config;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.talview.android.proview.sample.R;
import com.talview.android.sdk.proview.view.monitoring.ProviewMonitoringEditText;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questionList;

    private QuestionSubmitListener questionSubmitListener;

    public QuestionAdapter(List<Question> questionList, QuestionSubmitListener questionSubmitListener) {
        this.questionList = questionList;
        this.questionSubmitListener = questionSubmitListener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_question_item, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.bindData(questionList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView questionTextView;
        ProviewMonitoringEditText proviewMonitoringEditText;
        AppCompatButton submitButton;
        AppCompatTextView questionLabelTextView;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.tvQuestion);
            proviewMonitoringEditText = itemView.findViewById(R.id.proviewAnswerEditText);
            submitButton = itemView.findViewById(R.id.btnSubmitAnswer);
            questionLabelTextView = itemView.findViewById(R.id.tvLabelQuestion);
        }

        @SuppressLint("SetTextI18n")
        void bindData(Question question, int position) {

            // Setting question number
            questionLabelTextView.setText((position + 1) + " " + itemView.getContext().getString(R.string.question));

            // Setting question
            questionTextView.setText(question.getQuestion());

            // Saving Answers
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    question.setAnswer(proviewMonitoringEditText.getEditableText().toString());
                    questionSubmitListener.onAnswerSubmit(question, position == questionList.size() - 1, position);
                }
            });
        }
    }

    public interface QuestionSubmitListener {
        void onAnswerSubmit(Question question, boolean isLastQuestion, int position);
    }

}
