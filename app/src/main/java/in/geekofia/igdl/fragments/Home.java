package in.geekofia.igdl.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.Explode;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import in.geekofia.igdl.R;
import in.geekofia.igdl.activities.DownloadActivity;

import static in.geekofia.igdl.activities.MainActivity.HISTORY_FRAGMENT;
import static in.geekofia.igdl.activities.MainActivity.HOME_FRAGMENT;

public class Home extends Fragment implements View.OnClickListener {

    private TextInputEditText mEditText;
    private MaterialButton mLoadButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mEditText = view.getRootView().findViewById(R.id.edit_text_post_url);
        mLoadButton = view.getRootView().findViewById(R.id.btn_load);

        mLoadButton.setOnClickListener(this);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() < 40) {
                    mLoadButton.setEnabled(false);
                    mLoadButton.setText(R.string.str_load);
                } else {
                    String url = s.toString().trim();
                    if (url.startsWith("https://www.instagram.com/p/")) {
                        mLoadButton.setEnabled(true);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load:
                Intent downloadIntent = new Intent(getContext(), DownloadActivity.class);
                downloadIntent.putExtra("POST_URL", mEditText.getText().toString());
                startActivity(downloadIntent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_toolbar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tb_history:
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new History(), HISTORY_FRAGMENT)
                        .addToBackStack(HOME_FRAGMENT)
                        .commit();
        }
        return super.onOptionsItemSelected(item);
    }
}
