package com.example.sanyo.inclass05;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

/**
 * Created by sanyo on 2/12/2018.
 */

public class GetImageAsyncTask extends AsyncTask<String, Integer, Bitmap> {
    Idata idata;
    Bitmap bitmap = null;
    public GetImageAsyncTask(Idata idata) {
        this.idata = idata;
    }
    ProgressDialog progressDialog;
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog((Context) idata);
        progressDialog.setTitle("Loading Image");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        idata.handleListData(bitmap);
        progressDialog.dismiss();
    }



    @Override
    protected Bitmap doInBackground(String... strings) {

        HttpURLConnection connection = null;
        bitmap = null;
        try {
            URL url = new URL(strings[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                publishProgress(100);
                Log.d("demo","Bitmap found");

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if(connection != null){
                connection.disconnect();
            }

        }
        return bitmap;
    }

    public static interface Idata{
        public void handleListData(Bitmap bitmap);
    }

}






    