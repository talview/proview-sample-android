package com.talview.android.proview.sample.ui.assessment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.talview.android.proview.sample.BuildConfig;
import com.talview.android.proview.sample.R;
import com.talview.android.proview.sample.ui.assessment.config.Question;
import com.talview.android.proview.sample.ui.assessment.config.QuestionAdapter;
import com.talview.android.proview.sample.ui.assessment.config.SnapHelperOneByOne;
import com.talview.android.proview.sample.util.Constants;
import com.talview.android.sdk.proview.Proview;
import com.talview.android.sdk.proview.callbacks.PreFlightCallback;
import com.talview.android.sdk.proview.callbacks.PreFlightInitializeCallback;
import com.talview.android.sdk.proview.view.ProctorCameraView;
import com.talview.android.sdk.proview.view.listeners.ProctorSessionListener;
import com.talview.android.sdk.proview.view.listeners.ProctorVideoUploadListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SampleAssessmentActivity extends AppCompatActivity implements QuestionAdapter.QuestionSubmitListener {

    ProctorCameraView proctorCameraView;
    RecyclerView questionRecyclerView;
    AppCompatTextView uploadPercentageTextView;
    ProgressBar progressBar;
    QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_assessment);

        initUI();
        initializeProview();
    }

    private void initUI() {
        proctorCameraView = findViewById(R.id.proctorCameraView);
        questionRecyclerView = findViewById(R.id.questionRecyclerView);
        uploadPercentageTextView = findViewById(R.id.tvUploadingPercentage);
        progressBar = findViewById(R.id.progressBar);
        SnapHelper snapHelper = new SnapHelperOneByOne();
        snapHelper.attachToRecyclerView(questionRecyclerView);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        questionAdapter = new QuestionAdapter(Question.dummyQuestionList(), this);
    }

    /**
     * Step 1 : Initialize proview with candidate and session details.
     */
    private void initializeProview() {

        progressBar.setVisibility(View.VISIBLE);

        //Please give unique candidate_id for candidate profile,
        // and external id for uniques session.
        Proview.get().initializePreFlight(
                BuildConfig.TALVIEW_PROVIEW_TOKEN,
                Constants.CANDIDATE_ID,
                Constants.EXTERNAL_ID,
                Constants.ASSESSMENT_TITLE,
                new PreFlightInitializeCallback() {
                    @Override
                    public void onPreFlightInitialized(@NotNull String sessionId) {
                        // Save this unique sessionID for this user.
                        startTestWithProviewPreflight();
                    }

                    @Override
                    public void onError(@NotNull String errorMessage) {
                        //Exiting the activity on error.
                        Toast.makeText(SampleAssessmentActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    /**
     * Step 2 : Start PreFlight for checking candidate hw check etc.
     */
    private void startTestWithProviewPreflight() {
        Proview.get().startPreflight(this, new PreFlightCallback() {
            @Override
            public void onPreFlightComplete() {
                //Start proctoring and start your assessment.
                startYourAssessment();
            }

            @Override
            public void onPreFlightFailure(@NotNull String errorMessage) {
                //Exiting the activity on error.
                Toast.makeText(SampleAssessmentActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onPreFlightCancelled() {
                //Exiting the activity on preFlightCancelled.
                Toast.makeText(SampleAssessmentActivity.this, "Pre Flight cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Step 3 : Start your assessment and start proctoring.
     * set upload listeners also.
     */
    private void startYourAssessment() {
        setListenersForProctoring();
        proctorCameraView.startSession(this);
        proctorCameraView.startProctoring();
    }

    /**
     * Set Listeners on proctorCameraView only initializing the proview session which it STEP -1 ;
     */
    private void setListenersForProctoring() {
        proctorCameraView.initializeSession(new ProctorSessionListener() {
            @Override
            public void onProctorSessionStart() {
                showQuestions();
            }

            @Override
            public void onProctorSessionStop() {
                Toast.makeText(SampleAssessmentActivity.this, "Session Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode, @NotNull String message) {
                Toast.makeText(SampleAssessmentActivity.this, "Error code : " +errorCode+" "+message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        proctorCameraView.setProctorVideoListener(new ProctorVideoUploadListener() {
            @Override
            public void onProctorUploadSuccess() {
                runOnUiThread(()->{
                    Toast.makeText(SampleAssessmentActivity.this, "All videos uploaded successfully and test completed.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void uploadProgress(int progress) {
                //TODO: need to remove runOnUiThread.
                runOnUiThread(() -> {
                    uploadPercentageTextView.setVisibility(View.VISIBLE);
                    uploadPercentageTextView.setText(String.format(Locale.ENGLISH, "%s%d%%", getString(R.string.uploading), progress));
                });
            }

            @Override
            public void uploadStarted() {
                // Empty method
            }
        });
    }

    private void showQuestions() {
        questionRecyclerView.setAdapter(questionAdapter);
        progressBar.setVisibility(View.GONE);
        uploadPercentageTextView.setVisibility(View.GONE);
        questionRecyclerView.setVisibility(View.VISIBLE);
        proctorCameraView.setVisibility(View.VISIBLE);
    }

    /**
     * Assessment Question Answer Submit Callback
     * checks for last Question or End of Assessment to stop {@link ProctorCameraView} session and
     * Show proctored video upload progress
     */
    @Override
    public void onAnswerSubmit(Question question, boolean isLastQuestion, int position) {
        if (isLastQuestion) {
            // Stop session
            proctorCameraView.stopSession();

            // Show uploading progress
            showUploadProgressScreen();
        } else {
            questionRecyclerView.getLayoutManager().scrollToPosition(position + 1);
        }
    }

    /**
     * Show proctored video upload progress status
     */
    public void showUploadProgressScreen() {
        progressBar.setVisibility(View.GONE);
        questionRecyclerView.setVisibility(View.GONE);
        uploadPercentageTextView.setVisibility(View.GONE);
        proctorCameraView.setVisibility(View.GONE);
    }

    /**
     * Start {@link SampleAssessmentActivity}
     *
     * @param context required activity context
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SampleAssessmentActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit))
                .setMessage(getString(R.string.exit_message))
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    // stop session
                    proctorCameraView.stopSession();
                    finish();
                })
                .setNegativeButton(getString(R.string.cancel), ((dialog, which) -> {
                    dialog.cancel();
                })).show();
    }
}