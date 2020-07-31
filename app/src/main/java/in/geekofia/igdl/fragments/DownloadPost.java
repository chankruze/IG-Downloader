package in.geekofia.igdl.fragments;

import android.Manifest;
import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.view.View.GONE;
import static in.geekofia.igdl.utils.CustomFunctions.initRetrofit;
import static in.geekofia.igdl.utils.CustomFunctions.parseSource;

public class DownloadPost extends Fragment {

    private ViewPager2 viewPager2;
    private TextView loadingPost;

    private String mDataURL, mPostURL, mFileTitle;
    private ShortenApi shortenApi;

    private long downloadID;
    private static final String TAG = "DownloadPostFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_post, container, false);
        setHasOptionsMenu(true);

        getContext().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        initViews(view);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            if (bundle.getBoolean("isPrivate")) {
                renderPosts(parseSource(Jsoup.parse(bundle.getString("POST_SRC"))));
            } else {
                mPostURL = bundle.getString("POST_URL");
                new LoadPost().execute(mPostURL);
            }
        }

        return view;
    }

    private void initViews(View view) {
        viewPager2 = view.getRootView().findViewById(R.id.view_pager_2);
        loadingPost = view.getRootView().findViewById(R.id.loading_hint);
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

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getActivity(), instaPosts);
        viewPager2.setAdapter(viewPagerAdapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                InstaPost currPost = instaPosts.get(position);
                getActivity().setTitle(currPost.getId());
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

        getActivity().setTitle(firstPost.getId());

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

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

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

        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            downloadID = downloadManager.enqueue(request);
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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

    private void sendDownloadURL() {
        if (!mDataURL.isEmpty()) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            final View customLayout = getLayoutInflater().inflate(R.layout.download_prepare, null);
            alertDialogBuilder.setView(customLayout);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

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
                                alertDialog.dismiss();

                                startActivity(shareIntent);
                            } else {
                                Toast.makeText(getContext(), "Can't short this URL ;(", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ShortUrl> call, Throwable t) {
                    Toast.makeText(getContext(), "Throwable: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void setDataURL(String mDataURL) {
        this.mDataURL = mDataURL;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(onDownloadComplete);
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
}
