package com.myappcompany.andy.oauthtakeone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class GameListActivity extends AppCompatActivity {

    ListView gamesListView;
//    ArrayAdapter arrayAdapter;
    CustomListAdapter adapter;
    ArrayList<String> mainTitle = new ArrayList<>();
    ArrayList<Integer> playedTime = new ArrayList<>();
    ArrayList<String> imgId = new ArrayList<>();
    String apiKey;
    JSONObject gameInfoJson;
    JSONObject playerInfoJson;
    String username;
    int timePlayedConstraint;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        apiKey = getString(R.string.STEAM_API_KEY);
        gamesListView = findViewById(R.id.gamesListView);
        adapter = new CustomListAdapter(this, mainTitle, playedTime, imgId);
//        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, games);
        gamesListView.setAdapter(adapter);
        timePlayedConstraint = 300;

        try {
            goFetching();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void goFetching() throws JSONException {

        Intent intent = getIntent();
        String gameListUrl = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/" +
                "?key="+ apiKey + "&" +
                "steamid=" + intent.getStringExtra("steamId") + "&" +
                "include_appinfo=true";
        String playerInfoUrl = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/" +
                "?key="+ apiKey + "&" +
                "steamids=" + intent.getStringExtra("steamId");

        try {
            playerInfoJson = new JsonReturn().execute(playerInfoUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            username = playerInfoJson.getJSONObject("response").getJSONArray("players").getJSONObject(0).getString("personaname");
        }
        try {
            gameInfoJson = new JsonReturn().execute(gameListUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mainTitle.clear();
            playedTime.clear();
            imgId.clear();
            JSONArray gameList = gameInfoJson.getJSONObject("response").getJSONArray("games");
            for(int i = 0; i < gameList.length(); i++){
                JSONObject object = gameList.getJSONObject(i);
                String name = object.getString("name");
                int playtime = object.getInt("playtime_forever");
                String iconUrlKey = object.getString("img_icon_url");
                int appId = object.getInt("appid");
                Boolean playedRecent = false;
                if(object.getInt("rtime_last_played") > 0){
                    playedRecent = true;
                }
                if(playtime >= timePlayedConstraint) {
                    mainTitle.add(name);
                    playedTime.add(playtime);
                    String imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/"+appId+"/"+iconUrlKey+".jpg";
                    imgId.add(imgUrl);
                }
            }
            Log.i("to update", mainTitle.toString());
            Log.i("to update", playedTime.toString());
            Log.i("to update", imgId.toString());
            adapter.updateCustomList(mainTitle,playedTime,imgId);
        }
    }

    private class JsonReturn extends AsyncTask<String, Void, JSONObject>{
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
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }
                JSONObject jsonReturn = new JSONObject(buffer.toString());
                Log.i("test", jsonReturn.toString());
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