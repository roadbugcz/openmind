package org.roadbug.openmind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class SelectGameActivity extends Activity {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView imgv_abc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature( Window.FEATURE_NO_TITLE );

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
    }
}
