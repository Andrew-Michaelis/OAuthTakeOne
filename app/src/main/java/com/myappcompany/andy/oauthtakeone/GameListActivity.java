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
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class GameListActivity extends AppCompatActivity {

    TextView textView;
    ListView gamesListView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> gamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        Intent intent = getIntent();
        gamesList = intent.getStringArrayListExtra("gamesList");
        gamesList.sort(String::compareToIgnoreCase);
        textView = findViewById(R.id.textView);
        textView.setText(intent.getStringExtra("username")+"'s Library");
        gamesListView = findViewById(R.id.gamesListView);
        arrayAdapter = new ArrayAdapter(this, R.layout.listview_custom, gamesList);
        gamesListView.setAdapter(arrayAdapter);
    }
}