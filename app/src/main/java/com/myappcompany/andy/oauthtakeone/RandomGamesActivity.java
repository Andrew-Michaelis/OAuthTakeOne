package com.myappcompany.andy.oauthtakeone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class RandomGamesActivity extends AppCompatActivity {

    TextView textView;
    ListView gamesListView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> gamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_games);

        Intent intent = getIntent();
        gamesList = intent.getStringArrayListExtra("gamesList");
        Collections.shuffle(gamesList);
        int k = gamesList.size();
        if(k > 5){
            gamesList.subList(10, k).clear();
        }
        textView = findViewById(R.id.textView);
        gamesListView = findViewById(R.id.gamesListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, gamesList);
        gamesListView.setAdapter(arrayAdapter);
    }
}