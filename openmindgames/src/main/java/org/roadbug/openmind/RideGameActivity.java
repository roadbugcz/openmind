package org.roadbug.openmind;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.design.widget.Snackbar;

import org.roadbug.openmind.ridegame.RideGameView;

public class RideGameActivity extends Activity {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int PERMISSION_REQUEST_MICROPHONE = 1;

    RideGameView rgview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );

        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.activity_ride_game);

        int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

        rgview = findViewById(R.id.rgview);
        requestPermissions();
    }

    @Override
    public void onDestroy() {
        if (rgview != null) {
            rgview.destroy();
        }
        super.onDestroy();
    }

    private void requestPermissions() {
        int permissionCheck = this.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start recording audio
            Log.i (LOG_TAG, "RECORD_AUDIO permission already granted.");
            rgview.setMicPermission(true);
            rgview.startSpeech2TextService();
        } else {
            // Permission is missing and must be requested.
            if (this.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // Display a SnackBar with a button to request the missing permission.
                Snackbar.make(rgview, "Microphone/Recording audio access is required for a speech development game.",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        RideGameActivity.this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_MICROPHONE);
                    }
                }).show();

            } else {
                Log.w(LOG_TAG, "Permission is not available. Requesting Record audio permission.");

                // Request the permission. The result will be received in onRequestPermissionResult().
               this.requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_MICROPHONE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_MICROPHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    rgview.setMicPermission(true);
                    rgview.startSpeech2TextService();

                } else {

                    rgview.setMicPermission(false);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
