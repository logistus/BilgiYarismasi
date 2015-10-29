package cevsin.com.logi;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cevsin.com.logi.library.JSONParser;

public class CorrectAnswer extends AppCompatActivity {
    Button new_question, main_screen;
    TextView hak, puan;
    String get_hak = "", get_points = "";
    private static final String PREFS_NAME = "cevsin_by";
    private static final String WS_URL = "http://telinkcenter.com/android/webservice.php";
    private static final String TAG_SUCCESS = "success";
    SharedPreferences cPrefs;

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct_answer);
        
        // admob reklam yükle
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        new_question = (Button) findViewById(R.id.correct_new_question);
        main_screen = (Button) findViewById(R.id.correct_ana_ekran);
        hak = (TextView) findViewById(R.id.correct_rem);
        puan = (TextView) findViewById(R.id.correct_points);
        cPrefs = getSharedPreferences(PREFS_NAME, 0);

        if (isOnline()) {
            new DashboardTask().execute(cPrefs.getString("cevsin_userid", "notfound"));
        } else {
            Toast.makeText(getApplicationContext(), "İnternet bağlantısı gereki", Toast.LENGTH_LONG).show();
            hak.setText("-");
            puan.setText("-");
            new_question.setEnabled(false);
        }

        main_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(CorrectAnswer.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        new_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"0".equals(get_hak)) {
                    finish();
                    startActivity(new Intent(CorrectAnswer.this, QuestionActivity.class));
                    overridePendingTransition(0, 0);
                } else {
                    finish();
                    startActivity(new Intent(CorrectAnswer.this, DashboardActivity.class));
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    class DashboardTask extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("d", "get_infos");
                params.put("userid", args[0]);

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

            if (json != null) {
                try {
                    success = json.getInt("success");
                    get_hak = json.getString("message");
                    get_points = json.getString("points");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success > 0) {
                hak.setText(get_hak);
                puan.setText(get_points);

            } else {
                Toast.makeText(getApplicationContext(), "bir hata oluştu.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
