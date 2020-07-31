package in.geekofia.igdl.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import in.geekofia.igdl.R;
import in.geekofia.igdl.fragments.DownloadPost;
import in.geekofia.igdl.fragments.PrivatePost;

import static in.geekofia.igdl.utils.Constants.DOWNLOAD_POST_FRAG;
import static in.geekofia.igdl.utils.Constants.PRIVATE_POST_FRAG;

public class DownloadActivity extends AppCompatActivity {

    private String mPostURL;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Intent receivedIntent = getIntent();
        String receivedType = receivedIntent.getType();

        initViews();

        if (receivedIntent.hasExtra("POST_URL")) {
            mPostURL = receivedIntent.getStringExtra("POST_URL");
        } else if (receivedType.startsWith("text/")) {
            mPostURL = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (mPostURL != null) {
            if (mPostURL.contains("https://www.instagram.com")) {

                if (mPostURL.contains("?igshid") || mPostURL.contains("?utm_source") || mPostURL.contains("?")) {
                    mPostURL = mPostURL.split("\\?")[0];
                }

                // split URL to parts
                String[] urlParts = mPostURL.split("/");
                String type = urlParts[3], postCode = urlParts[4];

                Bundle bundle = new Bundle();
                bundle.putString("POST_URL", mPostURL);
                bundle.putString("POST_TYPE", type);
                bundle.putString("POST_CODE", postCode);

                if (postCode.length() > 12) {
                    // private
                    PrivatePost privatePost =  new PrivatePost();
                    privatePost.setArguments(bundle);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.download_activity_frag_container, privatePost, PRIVATE_POST_FRAG)
                            .commit();
                } else {
                    // public
                    DownloadPost downloadPost =  new DownloadPost();
                    bundle.putBoolean("isPrivate", false);
                    downloadPost.setArguments(bundle);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.download_activity_frag_container, downloadPost, DOWNLOAD_POST_FRAG)
                            .commit();
                }
            }
        }
    }

    private void initViews() {
        Toolbar mToolbar = findViewById(R.id.dl_toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dl_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
