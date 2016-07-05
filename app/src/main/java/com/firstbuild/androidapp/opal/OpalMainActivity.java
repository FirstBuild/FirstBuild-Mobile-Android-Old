package com.firstbuild.androidapp.opal;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.OpalInfo;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.tools.IntentTools;

/**
 * Created by hans on 16. 6. 16..
 */
public class OpalMainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1234;

    private static final String TAG = OpalMainActivity.class.getSimpleName();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private String appVersionName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opal_main);

        setupToolBar();

        setupDrawerLayout();

        setupNavagationView();

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.frame_content, new OpalMainFragment()).
                commit();

        try {
            appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkBleAvailability();

    }

    private void setupNavagationView() {
        // navigation_view
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //Checking if the item is in checked state or not, if not make it in checked state
                item.setChecked(true);

                switch(item.getItemId()) {
                    case R.id.nav_item_my_product:

                        IntentTools.goToDashboard(OpalMainActivity.this, OpalMainActivity.class.getSimpleName());
                        drawerLayout.closeDrawers();

                        break;

                    case R.id.nav_item_help:

                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.menu_navigation_help_sub);
                        navigationView.getMenu().getItem(0)
                                .getActionView().findViewById(R.id.back_btn_container)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        navigationView.getMenu().clear();
                                        navigationView.inflateMenu(R.menu.menu_navigation_drawer);
                                    }
                                });

                        // Dirty fix for moving back button arrow to the left a bit.
                        // I have no designer to ask to modify image at the moment.
                        navigationView.getMenu().
                                getItem(0).getActionView().
                                setTranslationX(getResources().
                                        getDimensionPixelSize(R.dimen.navigation_back_button_left_translation) * -1);

                        break;

                    case R.id.nav_item_faq:
                        IntentTools.openBrowser(getBaseContext(), IntentTools.OPAL_INTRODUCTION_HOME_URL);
                        break;

                    case R.id.nav_item_contact_expert:
                        IntentTools.composeEmail(getBaseContext(),
                                new String[] {IntentTools.OPAL_CONTACT_AN_EXPERT_EMAIL_ADDRESS},
                                IntentTools.OPAL_CONTACT_AN_EXPERT_EAMIL_SUBJECT);
                        break;

                    case R.id.nav_item_contact_warranty:
                        IntentTools.composeEmail(getBaseContext(),
                                new String[] {IntentTools.OPAL_CONTACT_WARRANTY_EMAIL_ADDRESS},
                                IntentTools.OPAL_CONTACT_WARRANTY_EMAIL_SUBJECT);
                        break;

                    case R.id.nav_item_feedback:
                        IntentTools.composeEmail(getBaseContext(),
                                new String[] {IntentTools.OPAL_FEEDBACK_EMAIL_ADDRESS},
                                IntentTools.OPAL_FEEDBACK_EMAIL_SUBJECT);

                        break;
                    case R.id.nav_item_about:

                        navigationView.getMenu().clear();

                        // since Navigation Menu item has limited height so utilize header view
                        navigationView.inflateHeaderView(R.layout.navigation_menu_item_back_button_header_about);
                        navigationView.inflateHeaderView(R.layout.navigation_menu_item_about);

                        navigationView.getHeaderView(0).findViewById(R.id.back_btn_container)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                        public void onClick(View v) {
                                        View back_btn_header = navigationView.getHeaderView(0);
                                        View headerContents = navigationView.getHeaderView(1);
                                        navigationView.removeHeaderView(back_btn_header);
                                        navigationView.removeHeaderView(headerContents);

                                        navigationView.inflateMenu(R.menu.menu_navigation_drawer);
                                    }
                                });

                        OpalInfo opalInfo = (OpalInfo)ProductManager.getInstance().getCurrent();

                        ((TextView)navigationView.getHeaderView(1).findViewById(R.id.opal_about_app_version)).setText(appVersionName);
                        ((TextView)navigationView.getHeaderView(1).findViewById(R.id.opal_about_firmware_version)).setText(opalInfo.getFirmWareversion());
                        ((TextView)navigationView.getHeaderView(1).findViewById(R.id.opal_about_bt_version)).setText(opalInfo.getBTVersion());

                        break;
                    case R.id.nav_item_update:

                        break;

                    default:
                        Log.d(TAG, "onNavigationItemSelected : unknown menu id : " + item.getItemId());
                        break;
                }

                return true;
            }
        });
    }

    private void setupDrawerLayout() {
        // drawer view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openDrawer,
                R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // Make sure to call below after setting drawer layout otherwise it will throw exception that right drawer is not available
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });


    }

    private void setupToolBar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        // Make sure to set this first before calling toolbar API
        setSupportActionBar(toolbar);

        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).
                setText(getString(R.string.product_name_opal).toUpperCase());
    }

    private void checkBleAvailability() {
        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start connect");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}

