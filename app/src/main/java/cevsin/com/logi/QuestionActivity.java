package cevsin.com.logi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cevsin.com.logi.library.JSONParser;

public class QuestionActivity extends AppCompatActivity {
    private TextView rem, soru, kalanhak, toplampuan;
    private Button sec1, sec2, sec3, sec4;
    private String question_id;
    private static final String PREFS_NAME = "cevsin_by";
    private static final String WS_URL = "http://telinkcenter.com/android/webservice.php";
    private static final String TAG_SUCCESS = "success";
    SharedPreferences cPrefs;
    CountDownTimer timer;

    @Override
    public void onBackPressed() {
        timer.cancel();
        QuestionActivity.super.onBackPressed();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        rem = (TextView) findViewById(R.id.rem_text);
        kalanhak = (TextView) findViewById(R.id.kalanhak);
        toplampuan = (TextView) findViewById(R.id.toplampuan);
        soru = (TextView) findViewById(R.id.soru_yazi);

        sec1 = (Button) findViewById(R.id.secenek1);
        sec2 = (Button) findViewById(R.id.secenek2);
        sec3 = (Button) findViewById(R.id.secenek3);
        sec4 = (Button) findViewById(R.id.secenek4);

        cPrefs = getSharedPreferences(PREFS_NAME, 0);

        if (isOnline()) {
            new QuestionTask().execute();
            new DashboardTask().execute(cPrefs.getString("cevsin_userid", "notfound"));
            // adnob reklam yükle
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            Toast.makeText(getApplicationContext(), "İnternet bağlantısı gereki", Toast.LENGTH_LONG).show();
        }

        sec1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                String sec1_value = sec1.getText().toString().trim();
                sec1.setEnabled(false);
                sec2.setEnabled(false);
                sec3.setEnabled(false);
                sec4.setEnabled(false);
                new AnswerCheckTask().execute(sec1_value, question_id);
            }
        });

        sec2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                String sec2_value = sec2.getText().toString().trim();
                sec1.setEnabled(false);
                sec2.setEnabled(false);
                sec3.setEnabled(false);
                sec4.setEnabled(false);
                new AnswerCheckTask().execute(sec2_value, question_id);
            }
        });

        sec3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                String sec3_value = sec3.getText().toString().trim();
                sec1.setEnabled(false);
                sec2.setEnabled(false);
                sec3.setEnabled(false);
                sec4.setEnabled(false);
                new AnswerCheckTask().execute(sec3_value, question_id);
            }
        });

        sec4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                String sec4_value = sec4.getText().toString().trim();
                sec1.setEnabled(false);
                sec2.setEnabled(false);
                sec3.setEnabled(false);
                sec4.setEnabled(false);
                new AnswerCheckTask().execute(sec4_value, question_id);
            }
        });
    }

    class AnswerCheckTask extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(QuestionActivity.this);
            pDialog.setMessage("Kontrol ediliyor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("d", "check_answer");
                params.put("chosen", args[0]);
                params.put("question_id", args[1]);
                params.put("userid", cPrefs.getString("cevsin_userid", "notfound"));

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

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            int success = 0;

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success > 0) {
                QuestionActivity.this.finish();
                startActivity(new Intent(QuestionActivity.this, CorrectAnswer.class));
                overridePendingTransition(0, 0);
            } else {
                QuestionActivity.this.finish();
                startActivity(new Intent(QuestionActivity.this, WrongAnswer.class));
                overridePendingTransition(0, 0);
            }
        }
    }

    class QuestionTask extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(QuestionActivity.this);
            pDialog.setMessage("Soru yükleniyor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("d", "get_question");
                params.put("userid", cPrefs.getString("cevsin_userid", "notfound"));

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

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            int success = 0;
            String question = "", options = "";

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    question = json.getString("question");
                    options = json.getString("options");
                    question_id = json.getString("question_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success > 0) {
                // soru
                soru.setText(question);
                // seçenekler
                String[] opts_get = options.split(",");
                List<String> optsList = Arrays.asList(opts_get);
                Collections.shuffle(optsList);
                String[] opts = optsList.toArray(new String[optsList.size()]);

                for (int i = 0; i < opts.length; i++) {
                    if (i == 0) sec1.setText(opts[i]);
                    if (i == 1) sec2.setText(opts[i]);
                    if (i == 2) sec3.setText(opts[i]);
                    if (i == 3) sec4.setText(opts[i]);
                }

                timer = new CountDownTimer(21000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        rem.setText("" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        finish();
                        startActivity(new Intent(QuestionActivity.this, WrongAnswer.class));
                        overridePendingTransition(0, 0);
                    }
                }.start();
            } else {
                Toast.makeText(getApplicationContext(), "soru veritabanından çekilemedi", Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(QuestionActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
            }
        }
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
            String message = "", points = "";

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString("message");
                    points = json.getString("points");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success > 0) {
                kalanhak.setText(message);
                toplampuan.setText(points);

            } else {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

}
