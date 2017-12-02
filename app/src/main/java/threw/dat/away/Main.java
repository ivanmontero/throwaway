package threw.dat.away;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        post();
    }

    public void post() {
        final TextView text  = (TextView) findViewById(R.id.hello);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://www.google.com";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        text.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                text.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public String encodeFileToBase64Binary(File f) {
        String encoded = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(f);
            byte[] bytes = new byte[(int)f.length()];
            fileInputStreamReader.read(bytes);
            byte[] encodedBytes = Base64.getEncoder().encode(bytes);
            encoded = new String(encodedBytes);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

        return encoded;



    }



}
