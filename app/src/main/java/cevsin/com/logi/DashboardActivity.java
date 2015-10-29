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
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import cevsin.com.logi.library.JSONParser;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DashboardActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "cevsin_by";
    private static final String WS_URL = "http://telinkcenter.com/android/webservice.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private Button logout, start;
    private TextView hak, puan;

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        logout = (Button) findViewById(R.id.logout_button);
        start = (Button) findViewById(R.id.start_contest);
        final SharedPreferences cPrefs = getSharedPreferences(PREFS_NAME, 0);
        hak = (TextView) findViewById(R.id.hak);
        puan = (TextView) findViewById(R.id.puan);

        if (isOnline()) {
            new DashboardTask().execute(cPrefs.getString("cevsin_userid", "notfound"));
            // admob reklam yükle
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            Toast.makeText(getApplicationContext(), "İnternet bağlantısı gereki", Toast.LENGTH_LONG).show();
            hak.setText("-");
            puan.setText("-");
            start.setEnabled(false);
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent question = new Intent(DashboardActivity.this, QuestionActivity.class);
                startActivity(question);
                overridePendingTransition(0, 0);

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = cPrefs.edit();
                editor.remove("cevsin_autologin");
                editor.commit();
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    class DashboardTask extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(DashboardActivity.this);
            pDialog.setMessage("İstatistikler yükleniyor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
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
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            int success = 0;
            String message = "", points = "";

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                    points = json.getString("points");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success > 0) {
                hak.setText(message);
                if ("0".equals(message)) {
                    start.setEnabled(false);
                }
                puan.setText(points);
            } else {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
