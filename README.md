# Proview Integrated sample assessment app for android
Learn to integrate `proview-android-sdk` and build proctor enabled android application - Sample Project by [Talview](https://www.talview.com/)

![Android CI](https://github.com/talview/proview-sample-android/workflows/Android%20CI/badge.svg)

<p align="center">
    <img src="https://user-images.githubusercontent.com/11706971/86244322-6b37ec80-bbc5-11ea-84d8-a7ef1066a999.png">
</p>
<br>

## About proview-sample-android project
Proview is an automated cognitive remote proctoring solution powered by [Talview](https://www.talview.com/) that uses advanced video and audio analytics to detect candidate impersonation and suspicious activities during a remote online test. This document explains in detail how to integrate `proview-android-sdk` and build a proctor-enabled android application.

## Building the project
* Clone the project, the `master` branch has the latest code. 
* This app uses a proview token for proctoring and personal access token for accessing proview-android-sdk. if you are an existing proview customer, please reach out to someone in your organization who may already have the proview token and personal access token. if you are a new proview customer, please reach out to the Talview team using [this link](https://proviewsupport.freshdesk.com/support/tickets/new) to get the proview token and personal access token. Once you have the proview token and personal access token, please add it to the `local.properties` file in your project, local.properties will like below:
```
sdk.dir=PATH_TO_ANDROID_SDK_ON_YOUR_LOCAL_MACHINE    
proviewToken=YOUR_PROVIEW_TOEKN
personalAccessToken=YOUR_PERSONAL_ACCESS_TOKEN
``` 
## Integrate Proview-Android-SDK in your Android application
* Switch to project view in android studio 
* Update [build.gradle](build.gradle)(Project: project-name)
```gradle
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:3.5.1"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72"
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
        maven { url "https://s3.amazonaws.com/repo.commonsware.com" }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/talview/proview-android-sdk")
            credentials {
                username = ""
                password = "YOUR_PERSONAL_ACCESS_TOKEN"
            }
        }
    }
}
```
* Update [build.gradle](/app/build.gradle)(Module: module-name)
    * Update default config
    ```gradle
    defaultConfig{
        minSdkVersion 21
        targetSdkVersion 29
        ...
        vectorDrawables.useSupportLibrary = true
    }
    ```
    * Add compile options
    ```gradle
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    ```
    * Add Proview-Android-SDK dependency
    ```gradle
    dependencies {
        // proview-android-sdk
        implementation "com.talview.proview:proview-android-sdk:1.0.0-alpha"
    }
    ```
* Update your [AndroidManifest.xml](app/src/main/AndroidManifest.xml)
    * Add uses-feature required for `proview-android-sdk`
    ```xml
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.talview.android.proview.sample">
    
        <uses-feature android:name="android.hardware.camera" />
        <uses-feature android:name="android.hardware.camera.autofocus" />
    
    </manifest>
    ```
    * Add permissions required for `proview-android-sdk`
    ```xml
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.talview.android.proview.sample">
    
        <uses-permission android:name="android.permission.RECORD_AUDIO" />
        <uses-permission android:name="android.permission.CALL_PHONE" />
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        <uses-permission android:name="android.permission.CAMERA" />
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />
        <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    
    </manifest>
    ```
* Create your application class and extends android.app.Application like [SampleApplication](app/src/main/java/com/talview/android/proview/sample/SampleApplication.java)
```java
public class SampleApplication extends Application {
}
```
* Override onCreate() method and initialize Proview
```java
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Proview init function 2nd paramater required BuildConfig.DEBUG type to enable logs
        Proview.init(this, BuildConfig.DEBUG);
    }
}
``` 
* Register your Application class to your AndroidManifest.xml file in `<application>` tag
```xml
<application
    android:name=".SampleApplication">
    .......
</application>
``` 
* Override the `proview-android-sdk` color configuration to apply your app theme
```xml
<resources>
    <color name="proviewColorPrimary">@color/colorPrimary</color>
    <color name="proviewColorPrimaryDark">@color/colorPrimaryDark</color>
    <color name="proviewColorSecondary">#5990cc</color>
    <color name="proviewColorSecondaryLight">#265990cc</color>
</resources>
```
* Checkout [SampleAssessmentActivity](app/src/main/java/com/talview/android/proview/sample/ui/assessment/SampleAssessmentActivity.java) to start proctoring for your assessment
    * Step 1: Create `Activity/Fragment` or if you have an `Activity/Fragment` for your assessment goto Step 2
    * Step 2: Replace your Answer Section EditText to `ProviewMonitoringEditText` [see an example here](app/src/main/res/layout/layout_question_item.xml)
    * Step 3: Add `ProctorCameraView` to your assessment host `Activity/Fragment` [see an example here](app/src/main/res/layout/activity_sample_assessment.xml)
    * Step 4: Initialize `proctorCameraView` in your UI component initialization 
    ```java
    public class SampleAssessmentActivity extends AppCompatActivity {  
      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_sample_assessment);
    
          proctorCameraView = findViewById(R.id.proctorCameraView);
      }
    }
    ```
    * Step 5: Proview `initializePreFlight` with mandatory parameters like proviewToken (Talview Proview Token), candidateId (for candidate profile), externalId(for uniques session) and assessmentTitle (refers to proview assessment title) 
        * Start PreFlight for checking candidate hw check on `onPreFlightInitialized` callback
        * Start your assessment and start proctoring also set proctoring upload listeners `ProctorSessionListener`
        * Stop proctoring session once your assessment ends `proctorCameraView.stopSession();`
    ```java
    public class SampleAssessmentActivity extends AppCompatActivity {  
      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_sample_assessment);
    
          initializeProview();
      }
      private void initializeProview() {
          Proview.get().initializePreFlight(
              /*TALVIEW_PROVIEW_TOKEN*/ "PROVIEW_TOKEN",
              /*CANDIDATE_ID*/ 1001,
              /*EXTERNAL_ID*/ 9001,
              /*ASSESSMENT_TITLE*/ "Sample Proctor Assessment",
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
      
      private void startYourAssessment() { 
        setListenersForProctoring();
        proctorCameraView.startSession(this);
        proctorCameraView.startProctoring();
      }
    
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
    }
    ```
  
## Proview-Android-SDK API Reference for this project
Proview Android SDK is a proctoring sdk.

* In Proview, we have three methods:
    * `init()` : To initialize Proview
    * `initializePreFlight(String proviewToken, int candidateId, int externalId, String assessmentTitle)` : proviewToken is required, candidateId is a unique identifier for candidate profile, externalId for unique proview session, assessmentTitle for identify assessment taken
    * `startPreflight(context, PreFlightCallback)` : Start PreFlight for checking candidate hw check etc.

* In ProctorCamera, we have custom views like `ProctorCameraView`
```
<com.talview.android.sdk.proview.view.ProctorCameraView
    android:id="@+id/proctorCameraView"
    android:layout_width="70dp"
    android:layout_height="70dp"/>
```
* In ProviewMonitoring, we have custom views like `ProviewMonitoringEditText`
```
<com.talview.android.sdk.proview.view.monitoring.ProviewMonitoringEditText
    android:id="@+id/proviewMonitoringEditText"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

### Find this project useful? 

* Support it by clicking the :star: button on the upper right of this page. :v:

### Feedback
Your feedback helps make proview-android-sdk better. Please let us know if you discover an issue or have an idea for improving this SDK. Feel free to report [here](https://github.com/talview/proview-sample-android/issues)

### License
```
   Copyright (C) 2020 Talview

```