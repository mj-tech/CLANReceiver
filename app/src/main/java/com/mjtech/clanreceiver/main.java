package com.mjtech.clanreceiver;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new attendance().execute("TEST");
    }

    private class attendance extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {
            TextView tv = (TextView)findViewById(R.id.icon);
            tv.getBackground().setColorFilter(0xFF6666FF, PorterDuff.Mode.ADD);
            tv.setText("⬆");
            ((TextView)findViewById(R.id.message)).setText("Connecting...");
        }

        protected Boolean doInBackground(String... params) {

            return false;
        }

        protected void onPostExecute(Boolean stat) {
            TextView tv = (TextView) findViewById(R.id.icon);
            if(stat) {
                tv.getBackground().setColorFilter(0xFF33CC33, PorterDuff.Mode.ADD);
                tv.setText("✔");
                ((TextView) findViewById(R.id.message)).setText("Attended.");
            } else {
                tv.getBackground().setColorFilter(0xFFFF6666, PorterDuff.Mode.ADD);
                tv.setText("✖");
                ((TextView) findViewById(R.id.message)).setText("Failed.");
            }
        }
    }
}

