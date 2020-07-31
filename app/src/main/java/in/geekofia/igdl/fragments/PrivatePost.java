package in.geekofia.igdl.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import in.geekofia.igdl.R;
import in.geekofia.igdl.models.InstaPost;

import static in.geekofia.igdl.utils.Constants.DOWNLOAD_POST_FRAG;
import static in.geekofia.igdl.utils.CustomFunctions.clipViewSourceURL;

public class PrivatePost extends Fragment implements View.OnClickListener {

    private String mPostURL;
    private Button copyURL, loadMedia;
    private TextInputEditText inputEditTextSource;
    private InstaPost instaPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_post, container, false);

        setHasOptionsMenu(true);

        initViews(view);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            instaPost = (InstaPost) bundle.getSerializable("IG_POST");
            if (instaPost != null) {
                mPostURL = instaPost.getPostUrl();
            }
        }

        return view;
    }

    private void initViews(View view) {
        copyURL = view.getRootView().findViewById(R.id.btn_cp_src_url);
        copyURL.setOnClickListener(this);
        loadMedia = view.getRootView().findViewById(R.id.btn_load_media);
        loadMedia.setOnClickListener(this);
        inputEditTextSource = view.getRootView().findViewById(R.id.edit_text_source);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cp_src_url:
                clipViewSourceURL(mPostURL, getActivity(), getContext());
                break;
            case R.id.btn_load_media:
                Bundle bundle = new Bundle();
                instaPost.setPostSourceCode(inputEditTextSource.getText().toString());
                bundle.putSerializable("IG_POST", instaPost);
                DownloadPost downloadPost = new DownloadPost();
                downloadPost.setArguments(bundle);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.download_activity_frag_container, downloadPost, DOWNLOAD_POST_FRAG)
                        .commit();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem btn_download = menu.findItem(R.id.tb_download);
        MenuItem btn_share = menu.findItem(R.id.tb_share);

        if (btn_download != null)
            btn_download.setVisible(false);

        if (btn_share != null)
            btn_share.setVisible(false);
    }
}
