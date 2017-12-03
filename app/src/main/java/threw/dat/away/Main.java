package threw.dat.away;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// https://github.com/pikanji/CameraPreviewSample

public class Main extends AppCompatActivity implements View.OnClickListener {
    ObjectRecognition or;
    Timer orTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);

        Button b1 = (Button) findViewById(R.id.button_sample);
        b1.setOnClickListener(this);

        orTimer = new Timer();

        // IVAN'S PLAYGROUND
        or = new ObjectRecognition();
        or.getLabels(BitmapFactory.decodeResource(this.getResources(), R.drawable.pie));
        or.getLabels(BitmapFactory.decodeResource(this.getResources(), R.drawable.sombrero));
        or.getLabels(BitmapFactory.decodeResource(this.getResources(), R.drawable.banana));

        orTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(or.labelCalls() == 3) {
                    List<ObjectRecognition.Label> freq = or.getFrequency();
                    for (ObjectRecognition.Label l : freq) {
                        Log.d("OBJECT RECOGNITION", l.description + " " + l.score);
                    }
                    orTimer.cancel();
                }
            }
        }, 0, 500);


    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.button_sample:
                intent = new Intent(this, TakePhoto.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}
