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
import com.talview.android.sdk.proview.session.internal.callbacks.ProviewAbortSessionListener;
import com.talview.android.sdk.proview.session.internal.callbacks.ProviewStartSessionListener;
import com.talview.android.sdk.proview.session.internal.callbacks.ProviewStopSessionListener;
import com.talview.android.sdk.proview.view.ProctorCameraView;
import com.talview.android.sdk.proview.view.listeners.ProctorCameraListener;
import com.talview.android.sdk.proview.view.listeners.ProctorVideoUploadListener;

import org.jetbrains.annotations.NotNull;

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
     * Step 1 : Initialize Proview session with required details.
     */
    private void initializeProview() {

        progressBar.setVisibility(View.VISIBLE);

        // Give unique candidate_id and external_id for uniques session.
        Proview.get().initializePreFlight(
                BuildConfig.TALVIEW_PROVIEW_TOKEN,
                Constants.CANDIDATE_ID,
                Constants.EXTERNAL_ID,
                Constants.ASSESSMENT_TITLE,
                new PreFlightInitializeCallback() {
                    @Override
                    public void onPreFlightInitialized(@NotNull String sessionId) {
                        // Save this unique sessionID on your server.
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
     * Step 2 : Start PreFlight for checking candidate's phone hardware.
     */
    private void startTestWithProviewPreflight() {
        Proview.get().startPreflight(this, new PreFlightCallback() {
            @Override
            public void onPreFlightComplete() {
                // Initialize your ProctorCameraView and start your assessment.
                initializeProctorCameraView();
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
     * Step 3 : First Initialize your [ProctorCameraView] and in onInitializeSuccess callback, start proctoring.
     * Note - Step 1 and 2 is required to initialize your [ProctorCameraView] otherwise it will throw
     * error.
     */
    private void initializeProctorCameraView() {
        proctorCameraView.init(this, new ProctorCameraListener() {
            @Override
            public void onInitializeSuccess() {
                // startProctoring session and your assessment to capture events.
                startProctorSessionAndAssessment();
            }

            @Override
            public void onError(int errorCode) {
                Toast.makeText(SampleAssessmentActivity.this, "ProviewCameraView Init OnError Code " + errorCode, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Step 4 : Start session and in onSuccess callback startProctoring to capture all events.
     */
    private void startProctorSessionAndAssessment() {

        // starting the session and proctoring.
        Proview.get().startSession(new ProviewStartSessionListener() {
            @Override
            public void onSuccess() {
                proctorCameraView.startProctoring();
                startYourAssessment();
            }

            @Override
            public void onFailure(int errorCode) {
                Toast.makeText(SampleAssessmentActivity.this, "OnStartSession Failure ErrorCode : " + errorCode, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // [Optional but recommended] to show video upload progress to user at the end of your assessment.
        // so user can wait till the gets uploaded.
        Proview.get().listenForVideoUploads(new ProctorVideoUploadListener() {

            @Override
            public void onSessionComplete() {
                runOnUiThread(() -> {
                    Toast.makeText(SampleAssessmentActivity.this, "Video Uploaded and Session Completed successfully.", Toast.LENGTH_SHORT).show();
                });
                finish();
            }

            @Override
            public void onError(int errorCode) {
                runOnUiThread(() -> {
                    Toast.makeText(SampleAssessmentActivity.this, "CurrentSessionVideoProgress OnFailure ErrorCode " + errorCode, Toast.LENGTH_SHORT).show();
                });
                finish();
            }

            @Override
            public void uploadProgress(int progress) {
                // Upload progress callback.
                runOnUiThread(() -> {
                    uploadPercentageTextView.setText("Uploading " + progress + "%");
                });
            }
        });
    }

    private void startYourAssessment() {
        questionRecyclerView.setAdapter(questionAdapter);
        progressBar.setVisibility(View.GONE);
        uploadPercentageTextView.setVisibility(View.GONE);
        questionRecyclerView.setVisibility(View.VISIBLE);
        proctorCameraView.setVisibility(View.VISIBLE);
    }

    /**
     * If you reach at end of the your assessment, call stopProctoring and stopSession to stop capturing
     * proview related events and videos.
     */
    @Override
    public void onAnswerSubmit(Question question, boolean isLastQuestion, int position) {
        if (isLastQuestion) {
            // stop proctoring and session at the end of the assessment.
            proctorCameraView.stopProctoring();

            Proview.get().stopSession(new ProviewStopSessionListener() {
                @Override
                public void onSuccess() {
                    // [Optional but recommended] to show video upload progress to user at the end of your assessment.
                    showUploadProgressScreen();
                }

                @Override
                public void onFailure(int errorCode) {
                    Toast.makeText(SampleAssessmentActivity.this, "ProviewStop OnFailure ErrorCode " + errorCode, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            questionRecyclerView.getLayoutManager().scrollToPosition(position + 1);
        }
    }

    public void showUploadProgressScreen() {
        progressBar.setVisibility(View.GONE);
        questionRecyclerView.setVisibility(View.GONE);
        uploadPercentageTextView.setVisibility(View.GONE);
        proctorCameraView.setVisibility(View.GONE);
        uploadPercentageTextView.setVisibility(View.VISIBLE);
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

                    // abortSession will clear your current session.
                    Proview.get().abortSession(new ProviewAbortSessionListener() {
                        @Override
                        public void onSuccess() {
                            finish();
                        }

                        @Override
                        public void onFailure(int errorCode) {
                            Toast.makeText(SampleAssessmentActivity.this, "OnAbort Failure ErrorCode " + errorCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancel), ((dialog, which) -> {
                    dialog.cancel();
                })).show();
    }
}