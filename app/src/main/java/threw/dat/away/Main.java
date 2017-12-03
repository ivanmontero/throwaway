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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

// https://github.com/pikanji/CameraPreviewSample

public class Main extends AppCompatActivity implements View.OnClickListener {
    final ClarifaiClient client = new ClarifaiBuilder("ceb0cdb7a26c4313baa45453b2dad949").buildSync();
    Object labelsMutex = new Object();
    List<Label> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labels = new ArrayList<>();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 11);

        Button b1 = (Button) findViewById(R.id.button_sample);
        b1.setOnClickListener(this);

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.banana);
        getLabels(toByteArray(bitmap));
    }

    public byte[] toByteArray(Bitmap bitmap) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getLabels(byte[] image) {
        client.getDefaultModels().generalModel().predict()
                .withInputs(ClarifaiInput.forImage(image))
                .executeAsync(new ClarifaiRequest.Callback<List<ClarifaiOutput<Concept>>>() {
                    @Override
                    public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
                        synchronized (labelsMutex) {
                            for (Concept c : clarifaiOutputs.get(0).data()) {
                                labels.add(new Label(c.name(), c.value()));
                                System.out.println(c.name() + " " + c.value());
                            }
                        }
                    }
                    @Override
                    public void onClarifaiResponseUnsuccessful(int errorCode) {
                        Log.d("ERROR", errorCode + "");
                    }
                    @Override
                    public void onClarifaiResponseNetworkError(IOException e) {
                        Log.d("ERROR", e.getMessage());
                    }
                });
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
    private class Label {
        public String description;
        public double score;
        public Label(String d, double s) {description = d; score = s; }
    }
}
