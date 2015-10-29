package cevsin.com.logi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import cevsin.com.logi.library.JSONParser;

public class RegisterActivity extends AppCompatActivity {
    EditText email, password1, password2;
    String email_text, password_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // admob reklam yükle
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Button register = (Button) findViewById(R.id.register_button);
        email = (EditText) findViewById(R.id.register_email);
        password1 = (EditText) findViewById(R.id.register_password);
        password2 = (EditText) findViewById(R.id.register_password2);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.length() > 0 && password1.length() > 4 && password2.length() > 4) {
                    password_text = password2.getText().toString();
                    email_text = email.getText().toString();
                    new RegisterTask().execute(email_text, password_text);
                }
            }
        });
    }

    class RegisterTask extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String WS_URL = "http://telinkcenter.com/android/webservice.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Kaydediliyor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("d", "register");
                params.put("email", args[0]);
                params.put("password", args[1]);

                JSONObject json = jsonParser.makeHttpRequest(
                        WS_URL, "POST", params);

                if (json != null) {
                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {

            int success = 0;
            String message = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success > 0) {
                Log.d("Success!", message);
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.putExtra("message", message);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
