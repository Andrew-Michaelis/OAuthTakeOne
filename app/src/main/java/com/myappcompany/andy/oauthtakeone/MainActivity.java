package com.myappcompany.andy.oauthtakeone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    final String REALM_PARAM = "Indecision";
    SharedPreferences sharedPreferences;
    String fetchDate;
    String apiKey;
    String userId;
    String username;
    ArrayList<String> gamesList = new ArrayList<>();
    JSONObject gameInfoJson;
    JSONObject playerInfoJson;

    TextView welcomeUserText;
    TextView lastSaved;
    TextView sitsText;
    LinearLayout buttonsLayout;
    Button libraryButton;
    Button randomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeUserText = findViewById(R.id.welcomeUserTextView);
        lastSaved = findViewById(R.id.lastSavedTextView);
        sitsText = findViewById(R.id.sitsTextView);
        buttonsLayout = findViewById(R.id.buttonsLayout);
        libraryButton = findViewById(R.id.libraryButton);
        libraryButton.setEnabled(false);
        randomButton = findViewById(R.id.randomGameButton);
        randomButton.setEnabled(false);

        sharedPreferences = getApplicationContext().getSharedPreferences("com.myappcompany.andy.oauthtakeone", Context.MODE_PRIVATE);

        apiKey = getString(R.string.STEAM_API_KEY);

        HashSet set = (HashSet<String>) sharedPreferences.getStringSet("games", null);
        if(set != null){
            gamesList = new ArrayList<>(set);
            username = sharedPreferences.getString("username", null);
            userId = sharedPreferences.getString("steamId", null);
            welcomeUserText.setText("WELCOME "+username+"!");
            lastSaved.setText("Library last fetched on: "+sharedPreferences.getString("fetchDate", null));
            lastSaved.setVisibility(View.VISIBLE);
            sitsText.setVisibility(View.VISIBLE);
            int color = com.google.android.material.R.color.mtrl_btn_transparent_bg_color;
            buttonsLayout.setForeground(new ColorDrawable(ContextCompat.getColor(this, color)));
            libraryButton.setEnabled(true);
            randomButton.setEnabled(true);
        }
    }

    public void onLibrary(View view) {
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        intent.putExtra("gamesList", gamesList);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void onRandom(View view) {
        Intent intent = new Intent(getApplicationContext(), RandomGamesActivity.class);
        intent.putExtra("gamesList", gamesList);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void onSITS(View view) {
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        final Activity activity = this;

        setContentView(webView);

        // Constructing openid url request
        String url = "https://steamcommunity.com/openid/login?" +
                "openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.identity=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.mode=checkid_setup&" +
                "openid.ns=http://specs.openid.net/auth/2.0&" +
                "openid.realm=https://" + REALM_PARAM + "&" +
                "openid.return_to=https://" + REALM_PARAM + "/signin/";

        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //checks the url being loaded
                setTitle(url);
                Uri Url = Uri.parse(url);

                if (Url.getAuthority().equals(REALM_PARAM.toLowerCase())) {
                    // That means that authentication is finished and the url contains user's id.
                    webView.stopLoading();
                    Log.i("post-stopLoading", "Stopped Loading");

                    // Extracts user id.
                    Uri userAccountUrl = Uri.parse(Url.getQueryParameter("openid.identity"));
                    userId = userAccountUrl.getLastPathSegment();

                    // Do whatever you want with the user's steam id
                    try {
                        goFetching();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } finally {
                        HashSet set = new HashSet(gamesList);
                        sharedPreferences.edit().putStringSet("games", set).apply();
                        sharedPreferences.edit().putString("steamId", userId).apply();
                        sharedPreferences.edit().putString("username", username).apply();
                        sharedPreferences.edit().putString("fetchDate", fetchDate).apply();
                        activity.recreate();
                    }
                }
            }
        });
    }

    public void goFetching() throws JSONException {

        String gameListUrl = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/" +
                "?key="+ apiKey + "&" +
                "steamid=" + userId + "&" +
                "include_appinfo=true";
        String playerInfoUrl = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/" +
                "?key="+ apiKey + "&" +
                "steamids=" + userId;

        try {
            playerInfoJson = new JsonReturn().execute(playerInfoUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            username = playerInfoJson.getJSONObject("response").getJSONArray("players").getJSONObject(0).getString("personaname");
            welcomeUserText.setText("Welcome "+username+"!");
        }
        try {
            gameInfoJson = new JsonReturn().execute(gameListUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            gamesList.clear();
            JSONArray gameList = gameInfoJson.getJSONObject("response").getJSONArray("games");
            for(int i = 0; i < gameList.length(); i++){
                JSONObject object = gameList.getJSONObject(i);
                String name = object.getString("name");
                int playtime = object.getInt("playtime_forever");
                String iconUrlKey = object.getString("img_icon_url");
                int appId = object.getInt("appid");
                Boolean playedRecent = false;
                if(object.getInt("rtime_last_played") < 20160){
                    playedRecent = true;
                }
                gamesList.add(name);
            }
            Date date = new Date();
            DateFormat df = android.text.format.DateFormat.getDateFormat(getApplicationContext());
            fetchDate = df.format(date);
        }
    }

    private class JsonReturn extends AsyncTask<String, Void, JSONObject> {

        ProgressDialog pd;
        protected void onPreExecute() {
            super.onPreExecute();

            if(pd == null || !pd.isShowing()) {
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Please wait");
                pd.setCancelable(false);
                pd.show();
            }
        }
        @Override
        protected JSONObject doInBackground(String... strings) {

            HttpsURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }
                JSONObject jsonReturn = new JSONObject(buffer.toString());
                return jsonReturn;

            } catch (IOException | JSONException e) {
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
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
        }
    }
}