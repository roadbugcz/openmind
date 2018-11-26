package org.roadbug.openmind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toolbar;

public class SelectGameActivity extends Activity {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView imgv_abc;
    private ImageView imgv_ride;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature( Window.FEATURE_NO_TITLE );

        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.activity_select_game);


        imgv_abc = (ImageView)findViewById(R.id.game_abc);
        imgv_abc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Starting Read words game");
                Intent intent = new Intent(SelectGameActivity.this, ReadWordsActivity.class);
                startActivity(intent);
            }
        });

        imgv_ride = (ImageView)findViewById(R.id.game_ride);
        imgv_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Starting Riding game");
                Intent intent = new Intent(SelectGameActivity.this, RideGameActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:

                Log.i(LOG_TAG, "Starting Settings");
                Intent intent = new Intent(SelectGameActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
