package com.mjtech.clanreceiver;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class main extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    String rm = "AC2 5506";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.setNdefPushMessage(null, this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView)findViewById(R.id.room)).setText(rm);
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
            new attendance().execute(new String(record.getPayload()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private class attendance extends AsyncTask<String, Void, Void> {
        JSONObject obj;
        protected void onPreExecute() {
            TextView tv = (TextView)findViewById(R.id.icon);
            tv.getBackground().setColorFilter(0xFF6666FF, PorterDuff.Mode.ADD);
            tv.setText("⬆");
            ((TextView)findViewById(R.id.message)).setText("Connecting...");
        }

        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("https://mjtech.cf/api/attendance/attend.php");
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("session", params[0])
                                                       .appendQueryParameter("room", rm);

                String query = builder.build().getEncodedQuery();

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                obj = new JSONObject(readStream(con.getInputStream()));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void voided) {
            TextView tv = (TextView) findViewById(R.id.icon);
            try {
                if (obj.getInt("err") == 0) {
                    tv.getBackground().setColorFilter(0xFF33CC33, PorterDuff.Mode.ADD);
                    tv.setText("✔");
                    ((TextView) findViewById(R.id.message)).setText("Attendance Recorded.");
                } else {
                    tv.getBackground().setColorFilter(0xFFFF6666, PorterDuff.Mode.ADD);
                    tv.setText("✖");
                    ((TextView) findViewById(R.id.message)).setText(obj.getString("errmsg"));
                }
            } catch (Exception e) {}
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

