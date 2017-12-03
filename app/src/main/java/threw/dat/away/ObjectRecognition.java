package threw.dat.away;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

public class ObjectRecognition {
    private final ClarifaiClient client = new ClarifaiBuilder("ceb0cdb7a26c4313baa45453b2dad949").buildSync();
    private Object labelsMutex = new Object();
    private int labelCalls = 0;
    private List<Label> labels = new ArrayList<>();

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
        labelCalls++;
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

    public int labelCalls() {
        return labelCalls;
    }

    public Map<String, Double> getFrequency() {
        Map<String, Double> freq = new HashMap<>();



        return null;
    }

    public class Label {
        public String description;
        public double score;
        public Label(String d, double s) {description = d; score = s; }
    }
}
