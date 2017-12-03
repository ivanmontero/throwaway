package threw.dat.away;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final Object labelsMutex = new Object();
    private volatile int labelCalls = 0;
    private List<Label> labels = new ArrayList<>();

    public void addLabels(Bitmap image) {
       addLabels(toByteArray(image));
    }

    public void addLabels(File file) {
        client.getDefaultModels().generalModel().predict()
                .withInputs(ClarifaiInput.forImage(file))
                .executeAsync(new ClarifaiRequest.Callback<List<ClarifaiOutput<Concept>>>() {
                    @Override
                    public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
                        synchronized (labelsMutex) {
                            for (Concept c : clarifaiOutputs.get(0).data()) {
                                labels.add(new Label(c.name(), c.value()));
                            }
                        }
                        labelCalls++;
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

    public void addLabels(byte[] image) {
        client.getDefaultModels().generalModel().predict()
                .withInputs(ClarifaiInput.forImage(image))
                .executeAsync(new ClarifaiRequest.Callback<List<ClarifaiOutput<Concept>>>() {
                    @Override
                    public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
                        synchronized (labelsMutex) {
                            for (Concept c : clarifaiOutputs.get(0).data()) {
                                labels.add(new Label(c.name(), c.value()));
                            }
                        }
                        labelCalls++;
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

    public List<Label> getFrequency() {
        Map<String, Double> freq = new HashMap<>();
        synchronized (labelsMutex) {
            for(Label l : labels) {
                if(freq.containsKey(l.description)) {
                    freq.put(l.description, freq.get(l.description) + l.score / labelCalls);
                } else {
                    freq.put(l.description, l.score / labelCalls);
                }
            }

        }
        List<Label> ls = new ArrayList<>();
        for (String key : freq.keySet()) {
            ls.add(new Label(key, freq.get(key)));
        }
        Collections.sort(ls, new Comparator<Label>() {
            @Override
            public int compare(Label label, Label t1) {
                return (int) (100 * (t1.score - label.score));
            }
        });
        return ls;
    }

    public void clear() {
        synchronized (labelsMutex) {
            labels.clear();
        }
        labelCalls = 0;
    }

    public class Label {
        public String description;
        public double score;
        public Label(String d, double s) {description = d; score = s; }
    }

    private static final ObjectRecognition or = new ObjectRecognition();
    public static ObjectRecognition getInstance() { return or; }
}
