package in.geekofia.igdl.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import in.geekofia.igdl.R;
import in.geekofia.igdl.adapters.ViewPagerAdapter;
import in.geekofia.igdl.interfaces.ShortenApi;
import in.geekofia.igdl.models.InstaPost;
import in.geekofia.igdl.models.ShortUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;
import static in.geekofia.igdl.utils.CustomFunctions.clipViewSourceURL;
import static in.geekofia.igdl.utils.CustomFunctions.initRetrofit;
import static in.geekofia.igdl.utils.CustomFunctions.isPrivatePost;
import static in.geekofia.igdl.utils.CustomFunctions.parseSource;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener{

    private String mDataURL, mPostURL, mFileTitle;
    private ViewPager2 viewPager2;
    private Context mContext = this;
    private ShortenApi shortenApi;
    private ScrollView scrollView;
    private TextView loadingPost;
    private Button copyURL, loadMedia;
    private TextInputEditText inputEditTextSource;

    private long downloadID;

    public static final String TAG = "DownloadActivity";

    @SuppressLint("JavascriptInterface")
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
            Log.d(TAG, "onCreate: " + mPostURL);
        }

        if (mPostURL != null) {
            // if url is like https://www.instagram.com/p/B_NaTi2hfqD/?igshid=sfov1iuwoagd

            // if it is a post
            String postFormat = "https://www.instagram.com/p/";

            // if it's a igtv video
            // https://www.instagram.com/tv/CA-kGBoAjP7/?utm_source=ig_web_copy_link
            String igtvFormat = "https://www.instagram.com/tv/";

            if (mPostURL.contains("https://www.instagram.com")) {

                if (mPostURL.contains("?igshid") || mPostURL.contains("?utm_source") || mPostURL.contains("?")) {
                    mPostURL = mPostURL.split("\\?")[0];
                }

                if (mPostURL.contains(postFormat)) {
                    // check for private/public
                    if (isPrivatePost(mPostURL, postFormat)) {
                        loadingPost.setVisibility(View.INVISIBLE);
                        scrollView.setVisibility(View.VISIBLE);
                    } else {
                        mPostURL = mPostURL.substring(mPostURL.indexOf(postFormat), postFormat.length() + 11);
                        new LoadPost().execute(mPostURL);
                    }
                } else if (mPostURL.contains(igtvFormat)) {
                    // check for private/public
                    mPostURL = mPostURL.substring(mPostURL.indexOf(igtvFormat), igtvFormat.length() + 11);
                    new LoadPost().execute(mPostURL);
                }
            }
        }
    }

    private void initViews() {
        Toolbar mToolbar = findViewById(R.id.dl_toolbar);
        setSupportActionBar(mToolbar);

        viewPager2 = findViewById(R.id.view_pager_2);
        loadingPost = findViewById(R.id.loading_hint);
        scrollView = findViewById(R.id.sv_private_post_dl);
        copyURL  = findViewById(R.id.btn_cp_src_url);
        copyURL.setOnClickListener(this);
        loadMedia = findViewById(R.id.btn_load_media);
        loadMedia.setOnClickListener(this);
        inputEditTextSource = findViewById(R.id.edit_text_source);
    }

    public void setDataURL(String mDataURL) {
        this.mDataURL = mDataURL;
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cp_src_url:
                clipViewSourceURL(mPostURL, this, this);
                break;
            case R.id.btn_load_media:
                scrollView.setVisibility(GONE);
                try {
                    Document document = Jsoup.parse(inputEditTextSource.getText().toString());
                    renderPosts(parseSource(document));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private class LoadPost extends AsyncTask<String, Void, ArrayList<InstaPost>> {

        @Override
        protected ArrayList<InstaPost> doInBackground(String... strings) {
            Document document = null;

            try {
                document = Jsoup.connect(strings[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return parseSource(document);
        }

        @Override
        protected void onPostExecute(ArrayList<InstaPost> instaPosts) {
            renderPosts(instaPosts);
        }

    }

    private void renderPosts(ArrayList<InstaPost> instaPosts) {
        TextView pageNo = viewPager2.getRootView().findViewById(R.id.page_count);
        int crrPageNo = 1;
        int totalPageNo = instaPosts.size();
        // collect all urls and load them in viewpager
        if (instaPosts.size() > 1) {
            pageNo.setVisibility(View.VISIBLE);
            pageNo.setText(getString(R.string.crr_page_number, crrPageNo, totalPageNo));
        } else {
            pageNo.setVisibility(GONE);
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(DownloadActivity.this, instaPosts);
        viewPager2.setAdapter(viewPagerAdapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                InstaPost currPost = instaPosts.get(position);
                setTitle(currPost.getId());
                pageNo.setText(getString(R.string.crr_page_number, position + crrPageNo, totalPageNo));

                if (currPost.isVideo()) {
                    setDataURL(currPost.getVideoUrl());
                    mFileTitle = "IMG_" + currPost.getVideoUrl().split("\\?")[0];
                } else {
                    setDataURL(currPost.getImageUrl());
                    mFileTitle = "VID_" + currPost.getImageUrl().split("\\?")[0];
                }
            }
        });

        InstaPost firstPost = instaPosts.get(0);
        loadingPost.setVisibility(GONE);

        setTitle(firstPost.getId());

        if (firstPost.isVideo()) {
            setDataURL(firstPost.getVideoUrl());
            mFileTitle = "IMG_" + firstPost.getVideoUrl().split("\\?")[0];
        } else {
            setDataURL(firstPost.getImageUrl());
            mFileTitle = "VID_" + firstPost.getImageUrl().split("\\?")[0];
        }

        Retrofit mRetrofit = initRetrofit();
        shortenApi = mRetrofit.create(ShortenApi.class);
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
