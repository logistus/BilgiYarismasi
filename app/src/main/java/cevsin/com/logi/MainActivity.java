package cevsin.com.logi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import cevsin.com.logi.library.JSONParser;

public class MainActivity extends AppCompatActivity {
    String msg, email_text, password_text;
    TextView register;
    Button login;
    CheckBox autologin;
    EditText email, password;
    private static final String PREFS_NAME = "cevsin_by";
    private static final String WS_URL = "http://telinkcenter.com/android/webservice.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // admob reklam yükle
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            msg = extras.getString("message");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }

        // check user logged in
        SharedPreferences cPrefs = getSharedPreferences(PREFS_NAME, 0);
        int get_autologin = cPrefs.getInt("cevsin_autologin", 0);

        if (get_autologin > 0) {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        register = (TextView) findViewById(R.id.register);
        login = (Button) findViewById(R.id.login);
        autologin = (CheckBox) findViewById(R.id.auto_login);
        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.length() > 0 &&  password.length() > 4) {
                    password_text = password.getText().toString();
                    email_text = email.getText().toString();
                    new LoginTask().execute(email_text, password_text);
                }
            }
        });
    }

    class LoginTask extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Giriş yapılıyor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("d", "login");
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
                SharedPreferences cPrefs = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = cPrefs.edit();
                if (autologin.isChecked()) {
                    editor.putInt("cevsin_autologin", 1);
                }
                editor.putString("cevsin_userid", message);
                editor.commit();
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
