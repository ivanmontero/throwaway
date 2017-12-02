package threw.dat.away;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

// GOOGLE DOESN'T WORK
// KEY: AIzaSyCrONcv4tfqELSnHmCaZUPu6mGNSEvlVvs
// https://vision.googleapis.com/v1/images:annotate?key=YOUR_API_KEY



public class Main extends AppCompatActivity {
//    public static String VISION_URL = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyCrONcv4tfqELSnHmCaZUPu6mGNSEvlVvs";
    boolean hasCam;
    final ClarifaiClient client = new ClarifaiBuilder("ceb0cdb7a26c4313baa45453b2dad949").buildSync();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasCam = checkCameraHardware(this);
//        testLabels();
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.sombrero);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapData = bos.toByteArray();
//            for(Label l : getLabels(byteBuffer.array())) {
//                Log.d("LABEL", "label=" + l.description + " score=" + l.score);
//            }
            client.getDefaultModels().generalModel().predict()
                            .withInputs(ClarifaiInput.forImage(bitmapData))
                            .executeAsync(new ClarifaiRequest.Callback<List<ClarifaiOutput<Concept>>>() {
                                @Override
                                public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
                                    int i = 0;
                                    for(ClarifaiOutput<Concept> co : clarifaiOutputs) {
                                        Log.d("INFO", "co=" + i++);
                                        for(Concept c : co.data())
                                            Log.d("INFO", c.name() + " " + c.value());
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testLabels() {
        JSONObject response = null;
        try {
             response = new JSONObject("{\n" +
                    "  \"responses\": [\n" +
                    "    {\n" +
                    "      \"labelAnnotations\": [\n" +
                    "        {\n" +
                    "          \"mid\": \"/m/0bt9lr\",\n" +
                    "          \"description\": \"dog\",\n" +
                    "          \"score\": 0.97346616\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"mid\": \"/m/09686\",\n" +
                    "          \"description\": \"vertebrate\",\n" +
                    "          \"score\": 0.85700572\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"mid\": \"/m/01pm38\",\n" +
                    "          \"description\": \"clumber spaniel\",\n" +
                    "          \"score\": 0.84881884\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"mid\": \"/m/04rky\",\n" +
                    "          \"description\": \"mammal\",\n" +
                    "          \"score\": 0.847575\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"mid\": \"/m/02wbgd\",\n" +
                    "          \"description\": \"english cocker spaniel\",\n" +
                    "          \"score\": 0.75829375\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JSONArray labelList = response.getJSONArray("responses").getJSONObject(0).getJSONArray("labelAnnotations");
            for(int i = 0; i < labelList.length(); i++) {
                JSONObject l = labelList.getJSONObject(i);
                Log.d("info",l.getString("description") + " " + l.getString("score"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public List<Label> getLabels(byte[] image) {
////        // Instantiate the RequestQueue.
////        RequestQueue queue = Volley.newRequestQueue(this);
////
////        String imageBinary = null;
////        JSONObject jsonBody = null;
////        try {
////            imageBinary = encodeImageToBase64Binary(image);
////            jsonBody = new JSONObject("{\n" +
////                    "  \"requests\": [\n" +
////                    "    {\n" +
////                    "      \"image\": {\n" +
////                    "        \"content\": \"" + imageBinary + "\"\n" +
////                    "      },\n" +
////                    "      \"features\": [\n" +
////                    "        {\n" +
////                    "          \"type\": \"LABEL_DETECTION\"\n" +
////                    "        }\n" +
////                    "      ]\n" +
////                    "    }\n" +
////                    "  ]\n" +
////                    "}");
////        } catch (Exception e) {
////            Log.d("Error.Image", e.getLocalizedMessage());
////        }
////
////        final List<Label> labels = new ArrayList<>();
////        JsonObjectRequest postRequest = new JsonObjectRequest(VISION_URL, jsonBody ,
////                new Response.Listener<JSONObject>() {
////                    @Override
////                    public void onResponse(JSONObject response) {
//////                        Log.d("Response", response);
////                        try {
////                            JSONArray labelList = response.getJSONArray("responses").getJSONObject(0).getJSONArray("labelAnnotations");
////                            for(int i = 0; i < labelList.length(); i++) {
////                                JSONObject l = labelList.getJSONObject(i);
////                                labels.add(new Label(l.getString("description"), l.getString("score")));
////                            }
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                    }
////                },
////                new Response.ErrorListener() {
////                    @Override
////                    public void onErrorResponse(VolleyError error) {
////                        // error
////                        Log.d("Error.Response", error.getLocalizedMessage());
////                    }
////                }
////        );
////        queue.add(postRequest);
////
////        return labels;
//        List<ClarifaiOutput<Concept>> concepts =
//                client.getDefaultModels().generalModel().predict()
//                .withInputs(ClarifaiInput.forImage(image))
//                .executeSync()
//                .get();
//
//    }

    public String encodeImageToBase64Binary(byte[] image) throws Exception {
//        return new String(Base64.getEncoder().encode(image), "UTF-8");
        return Base64.encodeBase64(image).toString();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    public byte[] fileToBytes(File f) {
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(f);
            byte[] bytes = new byte[(int) f.length()];
            fileInputStreamReader.read(bytes);
            return bytes;
        } catch(Exception e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private class Label {
        public String description;
        public String score;
        public Label(String d, String s) {description = d; score = s; }
    }
}
