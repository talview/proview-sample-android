package com.talview.android.proview.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.talview.android.proview.sample.R;
import com.talview.android.proview.sample.ui.assessment.SampleAssessmentActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start SampleAssessmentActivity for new assessment
        findViewById(R.id.btnStartDemoTest).setOnClickListener(v ->
                SampleAssessmentActivity.startActivity(this)
        );
    }
}