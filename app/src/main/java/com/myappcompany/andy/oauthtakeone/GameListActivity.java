package com.myappcompany.andy.oauthtakeone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class GameListActivity extends AppCompatActivity {

    TextView tempTextView;
    String apiKey;
    JSONObject gameListJson;
    JSONObject playerInfoJson;
    JSONObject jsonReturnFinal;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        Intent intent = getIntent();
        apiKey = getString(R.string.STEAM_API_KEY);
        Log.i("api-key", apiKey);
        tempTextView = findViewById(R.id.tempTextView);

        String gameListUrl = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/" +
                "?key="+ apiKey + "&" +
                "steamid=" + intent.getStringExtra("steamId");
        String playerInfoUrl = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/" +
                "?key="+ apiKey + "&" +
                "steamids=" + intent.getStringExtra("steamId");
        new JsonReturn().execute(playerInfoUrl);
        playerInfoJson = jsonReturnFinal;
        new JsonReturn().execute(gameListUrl);
        gameListJson = jsonReturnFinal;
    }

    private class JsonReturn extends AsyncTask<String, String, String>{
        protected void onPreExecute() {
            super.onPreExecute();

            if(pd == null || !pd.isShowing()) {
                pd = new ProgressDialog(GameListActivity.this);
                pd.setMessage("Please wait");
                pd.setCancelable(false);
                pd.show();
            }
        }
        @Override
        protected String doInBackground(String... strings) {

            HttpsURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            try {
                jsonReturnFinal = new JSONObject(result);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}