package com.talview.android.proview.sample;

import android.app.Application;

import com.talview.android.sdk.proview.Proview;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize your Proview in Application class.
        Proview.init(this, BuildConfig.DEBUG);
    }
}
