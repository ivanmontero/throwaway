package threw.dat.away;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TakePhoto extends Activity {
    private CameraPreview mPreview;
    private RelativeLayout mLayout;
    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.take_photo);
        // Hide status-bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide title-bar, must be before setContentView
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Requires RelativeLayout.
        mLayout = new RelativeLayout(this);
        //mLayout = (RelativeLayout)findViewById(R.id.take_photo);
        //Button takePic = (Button) findViewById(R.id.button);


        setContentView(mLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the second argument by your choice.
        // Usually, 0 for back-facing camera, 1 for front-facing camera.
        // If the OS is pre-gingerbreak, this does not have any effect.
        mPreview = new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent);
        LayoutParams previewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // Un-comment below lines to specify the size.
        //previewLayoutParams.height = 500;
        //previewLayoutParams.width = 500;

        // Un-comment below line to specify the position.
        //mPreview.setCenterPosition(270, 130);

        mLayout.addView(mPreview, 0, previewLayoutParams);
        Button pic = new Button(this);
        pic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                mPreview.mCamera.takePicture(null, null, mPicture);
            }
        });

        pic.setText("Take Picture");
        mLayout.addView(pic);
        tv = new TextView(this);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150);
        tv.setTextColor(Color.WHITE);
        tv.setVisibility(View.INVISIBLE);
        mLayout.addView(tv);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mLayout.removeView(mPreview); // This is necessary.
        mPreview = null;
    }

    Timer orTimer;

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (Exception e) {

            }

            String photoPath = pictureFile.getAbsolutePath();
            galleryAddPic(photoPath);

            ObjectRecognition.getInstance().addLabels(pictureFile);

            pictureFile.delete();

            for(ObjectRecognition.Label l : ObjectRecognition.getInstance().getFrequency()) {
                Log.d("RECOGNITION", l.description + " " + l.score);
            }








            tv.setVisibility(View.VISIBLE);

            orTimer = new Timer();
            orTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(ObjectRecognition.getInstance().labelCalls() == 1) {
                    boolean checked = false;
                    List<ObjectRecognition.Label> ls = ObjectRecognition.getInstance().getFrequency();
                    for(int i = 0; i < 5; i++) {
                        if(ls.get(i).description.equals("can") || ls.get(i).description.equals("bottle")) {
                            tv.setText("RECYCLE");
                            checked = true;
                            break;
                        } else if (ls.get(i).description.equals("food")) {
                            tv.setText("COMPOST");
                            checked = true;
                            break;
                        }
                    }
                    if(!checked)
                        tv.setText("TRASH");
                    ObjectRecognition.getInstance().clear();
                    orTimer.cancel();
                }
            }
        }, 0, 500);







        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void galleryAddPic(String thePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(thePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}