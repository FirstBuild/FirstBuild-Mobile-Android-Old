
package com.firstbuild.androidapp.paragon.navigation;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment {

    // Name for SharedPreferences
    public static final String PREF_FILE_NAME = "geKitchenDrawer";
    private static final String TAG = NavigationDrawerFragment.class.getSimpleName();
    private static int DELAYTIME_START_NEXT_APPLIANCE = 700;

    private RecyclerView recyclerView;              //Recycle view for menu list.
    private ActionBarDrawerToggle drawerToggle;     // Drawer menu handler.
    private DrawerLayout drawerLayout;              // Drawer UI layout.
    private boolean isSelectingAppliance = false;   // State for now Selecting Appliance or Sub menu.
    private int selectedApplianceIndex = 0;         // Index appliance in Menu. Not index for ApplianceManager.
    //    private NavigationManager.NavigationAppliance nextNavigationAppliance;
    private Handler handler = new Handler();        // for Delay start for next appliance main menu.

    private int lastSelectedSubmenu = 1;        // index 0 is for Appliance, sub menus is start from 1.

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_navigation_drawer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Setting up drawer menu's behavior such as open / close, dimming header and menus.
     *
     * @param fragmentId   Resource ID of layout which set in .xml file.
     * @param drawerLayout Handle of DrawerLayout.
     * @param toolbar      Toolbar for handling hamburger button.
     */
    public void setUp(int fragmentId, final DrawerLayout drawerLayout, final Toolbar toolbar) {

        this.drawerLayout = drawerLayout;


        /**
         * Perform Open/ Close drawer menu.
         */
        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                Log.d(TAG, "NavigationDrawerFragment.onOptionsItemSelected" + " item id:" + item + ", android.R.id.home:" + android.R.id.home);

//                return super.onOptionsItemSelected(item);

                if (item != null && item.getItemId() == android.R.id.home) {
                    if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                        drawerLayout.closeDrawer(Gravity.RIGHT);
                    } else {
                        drawerLayout.openDrawer(Gravity.RIGHT);
                    }
                }
                return false;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                // Dimming tool bar where the drawer menu openning.
//                if (slideOffset < 0.6) {
//                    toolbar.setAlpha(1 - slideOffset);
//                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //Redraw option menu on toolbar.
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                //Redraw option menu on toolbar.
                getActivity().invalidateOptionsMenu();
            }

        };

        this.drawerLayout.setDrawerListener(this.drawerToggle);

        //Sync Open / Close state and set hamburger icon.
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
//                toolbar.setNavigationIcon(R.drawable.ic_hamburger_logo);
            }
        });

        this.drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "NavigationDrawerFragment.onClick" + " clicked Toolbar Naivagrion!!!!!");
                getActivity().getFragmentManager().popBackStack();
            }
        });

    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return drawerToggle;
    }

}
