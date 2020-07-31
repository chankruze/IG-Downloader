package in.geekofia.igdl.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.WindowManager;

import hotchemi.android.rate.AppRate;
import in.geekofia.igdl.R;
import in.geekofia.igdl.fragments.Home;

import static in.geekofia.igdl.utils.Constants.HOME_FRAGMENT;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new Home(), HOME_FRAGMENT)
                .commit();

        initViews();
        showRateApp();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager()
                    .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Confirmation")
                    .setMessage("Do you really want to close the app ?")
                    .setPositiveButton("Yeh", (dialog, which) -> finish())
                    .setNegativeButton("Nope", null)
                    .show();
        }
    }


    private void showRateApp() {
        AppRate.with(this)
                .setInstallDays(1)
                .setLaunchTimes(3)
                .setRemindInterval(2)
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
    }
}
