package in.geekofia.igdl.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import in.geekofia.igdl.R;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private String mURL, mTitle;
    private TextView mPostURL;
    private VideoView mVideoView;
    private boolean isVideo;
    private Context mContext = this;
    private ImageView mImageView;
    private MaterialButton downloadButton;
    private DownloadManager downloadManager;
    private long downloadID;

    public static final String TAG = "DownloadActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Intent receivedIntent = getIntent();
        String receivedType = receivedIntent.getType();

        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        initViews();

        if (receivedType.startsWith("text/")) {
            mURL = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);

            if (mURL != null) {
                // if url is like https://www.instagram.com/p/B_NaTi2hfqD/?igshid=sfov1iuwoagd
                if (mURL.contains("?igshid")) {
                    mURL = mURL.split("\\?")[0];
                }

                mPostURL.setText(mURL);
                new LoadPost().execute(mURL);
            }
        }
    }

    private void initViews() {
        mPostURL = findViewById(R.id.tv_post_url);
        mVideoView = findViewById(R.id.vv_video);
        mImageView = findViewById(R.id.iv_image);
        downloadButton = findViewById(R.id.btn_download);
        downloadButton.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_download:
                if (isStoragePermissionGranted()) {
                    startDownload(mURL, mTitle);
                }
        }
    }

    public void setURL(String mURL) {
        this.mURL = mURL;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setVideo(boolean video) {
        isVideo = video;
//        System.out.println("setVideo: [boolean]: " + video);
    }


    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(mContext, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    private class LoadPost extends AsyncTask<String, Void, String> {

        String url = "";

        @Override
        protected String doInBackground(String... strings) {
            Document document;

            try {
                document = Jsoup.connect(strings[0]).get();
                Elements metas = document.getElementsByTag("meta");

                for (Element element : metas) {
                    if (element.hasAttr("content")) {
                        String content = element.attr("content");

                        if (content.startsWith("https://instagram.fbbi1-1.fna.fbcdn.net")) {
                            if (content.contains(".mp4")) {
                                setVideo(true);
                                url = element.attr("content");
                                setTitle(url.split("\\?")[0].split("/")[5]);
                            } else if (content.contains(".jpg")) {
                                setVideo(false);
                                url = element.attr("content");
                                String[] splitedUrl = url.split("\\?")[0].split("/");
                                setTitle(splitedUrl[splitedUrl.length - 1]);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return url;
        }

        @Override
        protected void onPostExecute(String string) {
            if (isVideo) {
                setURL(string);

                mVideoView.setVideoPath(string);
                mVideoView.setMediaController(new MediaController(mContext));
                mVideoView.start();
                mVideoView.setVisibility(View.VISIBLE);

                downloadButton.setEnabled(true);
                //System.out.println("onPostExecute: [video]: " + string);
            } else {
                setURL(string);
                //System.out.println("onPostExecute: [image]: " + string);

                Picasso.get().load(string).into(mImageView);
                mImageView.setVisibility(View.VISIBLE);

                downloadButton.setEnabled(true);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startDownload(String url, String title) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            downloadID = downloadManager.enqueue(request);
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            startDownload(mURL, mTitle);
        }
    }
}
