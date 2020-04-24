package in.geekofia.igdl.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hotchemi.android.rate.AppRate;
import in.geekofia.igdl.R;
import in.geekofia.igdl.fragments.History;
import in.geekofia.igdl.fragments.Home;

public class MainActivity extends AppCompatActivity {

    public static final String HOME_FRAGMENT = "HOME_FRAGMENT", HISTORY_FRAGMENT = "HISTORY_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setContentView(R.layout.activity_main);

        Home home = new Home();
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home, HOME_FRAGMENT).commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        initViews();
        showRateApp();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Confirmation")
                .setMessage("Do you really want to close the app ?")
                .setPositiveButton("Yeh", (dialog, which) -> finish())
                .setNegativeButton("Nope", null)
                .show();
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
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = menuItem -> {

        Fragment selectedFragment = null;
        String FRAG_TAG = "";

        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                selectedFragment = new Home();
                FRAG_TAG = HOME_FRAGMENT;
                break;
            case R.id.navigation_history:
                selectedFragment = new History();
                FRAG_TAG = HISTORY_FRAGMENT;
                break;
        }

        if (selectedFragment != null) {
            MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment, FRAG_TAG).commit();
        }

        return true;
    };
}
