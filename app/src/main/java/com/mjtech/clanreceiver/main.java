package com.mjtech.clanreceiver;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class main extends AppCompatActivity {
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.setNdefPushMessage(null, this);
        //nfcAdapter.setNdefPushMessage(new NdefMessage(NdefRecord.createMime("token", string.getBytes())), this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        waiting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        waiting();
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);;
        nfcAdapter.enableForegroundDispatch(this, pIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefRecord record = ((NdefMessage) rawMessages[0]).getRecords()[0];
            Log.e("Received",new String(record.getType()));
            new attendance().execute(new String(record.getType()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private class attendance extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {
            TextView tv = (TextView)findViewById(R.id.icon);
            tv.getBackground().setColorFilter(0xFF6666FF, PorterDuff.Mode.ADD);
            tv.setText("⬆");
            ((TextView)findViewById(R.id.message)).setText("Connecting...");
        }

        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL("http://mjtech.cf/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String stat = readStream(con.getInputStream());
                if(stat.equals("<h1>mjtech</h1>")) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean stat) {
            TextView tv = (TextView) findViewById(R.id.icon);
            if (stat) {
                tv.getBackground().setColorFilter(0xFF33CC33, PorterDuff.Mode.ADD);
                tv.setText("✔");
                ((TextView) findViewById(R.id.message)).setText("Attended.");
            } else {
                tv.getBackground().setColorFilter(0xFFFF6666, PorterDuff.Mode.ADD);
                tv.setText("✖");
                ((TextView) findViewById(R.id.message)).setText("Failed.");
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waiting();
                }
            }, 3000);
        }
    }

    private void waiting() {
        TextView tv = (TextView) findViewById(R.id.icon);
        tv.getBackground().setColorFilter(0xFF9999FF, PorterDuff.Mode.ADD);
        tv.setText("▼");
        ((TextView) findViewById(R.id.message)).setText("Please Tap Your Phone.");
    }

    public String readStream(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}

