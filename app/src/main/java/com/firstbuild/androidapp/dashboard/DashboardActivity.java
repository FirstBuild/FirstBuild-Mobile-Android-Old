package com.firstbuild.androidapp.dashboard;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firstbuild.androidapp.Paragon.ParagonMainActivity;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productManager.ProductInfo;
import com.firstbuild.androidapp.productManager.ProductManager;

import java.util.ArrayList;

public class DashboardActivity extends ActionBarActivity implements DashboardAdapter.ClickListener {

    private Toolbar toolbar;
    private String TAG = DashboardActivity.class.getSimpleName();
    private RecyclerView listViewProduct;
    private DashboardAdapter adapterDashboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        listViewProduct = (RecyclerView) findViewById(R.id.listProduct);

        adapterDashboard = new DashboardAdapter(this);
        adapterDashboard.setClickListener(this);
        listViewProduct.setItemAnimator(null);
        listViewProduct.setAdapter(adapterDashboard);
        listViewProduct.setLayoutManager(new LinearLayoutManager(this));

        updateListView();

    }

    private void updateListView() {

        ArrayList<ProductInfo> productsOrg = ProductManager.getInstance().getProducts();
        ArrayList<ProductInfo> productsNew = new ArrayList<ProductInfo>(ProductManager.getInstance().getSize());

        for (ProductInfo product : productsOrg) {
            productsNew.add(new ProductInfo(product));
        }

        adapterDashboard.updateData(productsNew);
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void addDeviceList(BluetoothDevice device) {

    }

    @Override
    public void itemClicked(View view, int position) {
        ProductInfo productInfo = adapterDashboard.getItem(position);

        if (productInfo.type == ProductInfo.PRODUCT_TYPE_PARAGON) {
            Intent intent = new Intent(DashboardActivity.this, ParagonMainActivity.class);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "itemClicked but error :" + productInfo.type);
        }

    }
}
