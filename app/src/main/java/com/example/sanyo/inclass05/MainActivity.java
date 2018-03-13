package com.example.sanyo.inclass05;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity implements GetImageAsyncTask.Idata {

    TextView textView;
    Bitmap data;
    ImageView imageView;
    ImageButton next;
    ImageButton previous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new GetKeywordsAsyncTask().execute("http://dev.theappsdr.com/apis/photos/keywords.php");

        imageView = findViewById(R.id.image);
        next = findViewById(R.id.ibNext);
        previous = findViewById(R.id.ibPrevious);
    }

    private boolean isConnectedOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        } else {
            return true;
        }
    }

    private class GetKeywordsAsyncTask extends AsyncTask<String, Void, String> {
        String[] arrOfStr;

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.d("demo", s);
                arrOfStr = s.split(";");
                for (String a : arrOfStr)
                    System.out.println(a);

                findViewById(R.id.buttonGo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isConnectedOnline()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Choose A Keyword");
                            builder.setItems(arrOfStr, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    textView = findViewById(R.id.tvSearch);
                                    textView.setText(arrOfStr[i]);
                                    new GetUrlAsyncTask(arrOfStr[i]).execute("http://dev.theappsdr.com/apis/photos/index.php");
                                }
                            });
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }else{
                            Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT);
                        }
                    }
                });

            } else {
                Log.d("demo", "No result");
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder stringBuilder = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String result = null;
            try {
                Log.d("demo", "GetAsync");
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d("demo", "Connected");
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    result = stringBuilder.toString();


                } else {
                    Log.d("demo", "Not Connected");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return result;
        }
    }

    private class GetUrlAsyncTask extends AsyncTask<String, Void, LinkedList<String>> {

        String keyword;
        int i = 0;
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);


        public GetUrlAsyncTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected void onPostExecute(LinkedList s) {
            progressDialog.dismiss();
            final LinkedList linkedList = s;
            if (s != null && s.size() != 0) {
                new GetImageAsyncTask(MainActivity.this).execute(s.getFirst().toString());
                if(linkedList.size() == 1){
                     next.setEnabled(false);
                     previous.setEnabled(false);
                } else{
                    next.setEnabled(true);
                    previous.setEnabled(true);
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Images found", Toast.LENGTH_SHORT).show();
                next.setEnabled(false);
                previous.setEnabled(false);
                imageView.setImageResource(0);
            }

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    i++;
                    new GetImageAsyncTask(MainActivity.this).execute(linkedList.get((i + linkedList.size() *
                            Math.abs(i)) % linkedList.size()).toString());

                }
            });

            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    i--;
                    new GetImageAsyncTask(MainActivity.this).execute(linkedList.get((i + linkedList.size() *
                            Math.abs(i)) % linkedList.size()).toString());
                }
            });
        }

        @Override
        protected void onPreExecute() {

            progressDialog.setTitle("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected LinkedList<String> doInBackground(String... strings) {
            LinkedList<String> urls = new LinkedList<>();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String result = null;
            try {
                URL url = new URL(strings[0] + "?" + "keyword=" + keyword);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        Log.d("demo", line);
                        urls.add(line);
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            return urls;
        }
    }

    @Override
    public void handleListData(Bitmap bitmap) {
        this.data = bitmap;

        if (data != null && imageView != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(MainActivity.this, "Image not found",Toast.LENGTH_SHORT).show();
        }
    }
}
