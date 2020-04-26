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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import in.geekofia.igdl.R;
import in.geekofia.igdl.interfaces.ShortenApi;
import in.geekofia.igdl.models.ShortUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadActivity extends AppCompatActivity {

    private String mDataURL, mPostURL, mFileTitle;
    private VideoView mVideoView;
    private boolean isVideo;
    private Context mContext = this;
    private ImageView mImageView;
    private FrameLayout frameLayout;
    private ShortenApi shortenApi;

    private long downloadID;
    private Toolbar mToolbar;

    public static final String TAG = "DownloadActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Intent receivedIntent = getIntent();
        String receivedType = receivedIntent.getType();

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        initViews();

        if (receivedIntent.hasExtra("POST_URL")) {
            mPostURL = receivedIntent.getStringExtra("POST_URL");
        } else if (receivedType.startsWith("text/")) {
            mPostURL = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (mPostURL != null) {
            // if url is like https://www.instagram.com/p/B_NaTi2hfqD/?igshid=sfov1iuwoagd
            if (mPostURL.contains("?igshid")) {
                mPostURL = mPostURL.split("\\?")[0];
            }

            new LoadPost().execute(mPostURL);
        }
    }

    private void initViews() {
        mToolbar = findViewById(R.id.dl_toolbar);
        setSupportActionBar(mToolbar);

        frameLayout = findViewById(R.id.fl_video_box);
        mVideoView = findViewById(R.id.vv_video);
        mImageView = findViewById(R.id.iv_image);
    }

    public void setDataURL(String mDataURL) {
        this.mDataURL = mDataURL;
    }

    public void setFileTitle(String mFileTitle) {
        this.mFileTitle = mFileTitle;
    }

    public String getFileTitle() {
        return mFileTitle;
    }

    public void setVideo(boolean video) {
        isVideo = video;
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
                    String prop = element.attr("property");
                    String content = element.attr("content");

                    if (prop.equals("og:video") &&
                            content.startsWith("https://instagram.fbbi1-1.fna.fbcdn.net") &&
                            content.contains(".mp4")) {
                        setVideo(true);
                        url = element.attr("content");
                        setFileTitle("VID_" + url.split("\\?")[0].split("/")[5]);
                    } else if (prop.equals("og:image") &&
                            content.startsWith("https://instagram.fbbi1-1.fna.fbcdn.net") &&
                            content.contains(".jpg")) {
                        setVideo(false);
                        url = element.attr("content");
                        String[] splitedUrl = url.split("\\?")[0].split("/");
                        setFileTitle("IMG_" + splitedUrl[splitedUrl.length - 1]);
                    }
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }

            return url;
        }

        @Override
        protected void onPostExecute(String string) {
            Retrofit mRetrofit = initRetrofit();
            shortenApi = mRetrofit.create(ShortenApi.class);
            setTitle(getFileTitle());

            if (isVideo) {
                setDataURL(string);

                mVideoView.setVideoPath(string);
                mVideoView.setMediaController(new MediaController(mContext));
                mVideoView.start();
                frameLayout.setVisibility(View.VISIBLE);
            } else {
                setDataURL(string);

                Picasso.get().load(string).into(mImageView);
                mImageView.setVisibility(View.VISIBLE);
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

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            startDownload(mDataURL, mFileTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dl_toolbar, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tb_download:
                if (isStoragePermissionGranted()) {
                    startDownload(mDataURL, mFileTitle);
                }
                return true;
            case R.id.tb_share:
                sendDownloadURL();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Retrofit initRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://is.gd/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void sendDownloadURL() {
        if (!mDataURL.isEmpty()) {
            Call<ShortUrl> shortUrlCall = shortenApi.getShortURL("json", mDataURL);

            shortUrlCall.enqueue(new Callback<ShortUrl>() {
                @Override
                public void onResponse(Call<ShortUrl> call, Response<ShortUrl> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getShortenedURL() != null) {
                                String message = "Original instagram post: " + mPostURL + "\n\nDownload link: " + response.body().getShortenedURL() + "\n\nSent via IG Downloader by chankruze";

                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, "Share download link of this post with");
                                startActivity(shareIntent);
                            } else {
                                Toast.makeText(getBaseContext(), "Can't short this URL ;(", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ShortUrl> call, Throwable t) {
                    Toast.makeText(getBaseContext(), "Throwable: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
