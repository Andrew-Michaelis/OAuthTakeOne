package com.myappcompany.andy.oauthtakeone;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> imgId;
    private final ArrayList<String> mainTitle;
    private final ArrayList<Integer> playedTime;

    public CustomListAdapter(Activity context, ArrayList<String> mainTitle, ArrayList<Integer> playedTime, ArrayList<String> imgId){
        super(context, R.layout.mylist, mainTitle);

        this.context = context;
        this.imgId = imgId;
        this.mainTitle = mainTitle;
        this.playedTime = playedTime;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.mylist, null, true);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        TextView playedView = (TextView) rowView.findViewById(R.id.subtitle);

        URL url = new URL("https://static.vecteezy.com/system/resources/previews/000/440/213/original/question-mark-vector-icon.jpg");
        Bitmap tempBitty = getBitmapFromURL.execute(String.valueOf(url)).get();

        imageView.setImageBitmap(tempBitty);
        titleText.setText(mainTitle.get(position));
        playedView.setText(String.valueOf(playedTime.get(position)));

        return rowView;
    }

    public void updateCustomList(ArrayList<String> newTitle, ArrayList<Integer> newTime, ArrayList<String> newImg) {

        imgId.addAll(newImg);
        mainTitle.addAll(newTitle);
        playedTime.addAll(newTime);

        Log.i("in update", imgId.toString());
        Log.i("in update", mainTitle.toString());
        Log.i("in update", playedTime.toString());

        this.notifyDataSetChanged();
    }

    public static class getBitmapFromURL extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... src) {
            try {
                URL url = new URL(src[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                connection.disconnect();
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
