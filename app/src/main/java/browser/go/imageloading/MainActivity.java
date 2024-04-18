package browser.go.imageloading;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import browser.go.imageloading.Adapter.LazyAdapter;
import browser.go.imageloading.Listener.EndlessRecyclerViewScrollListener;
import browser.go.imageloading.Receiver.NetworkChangeReceiver;
import browser.go.imageloading.UnSplash.UnsplashApiClient;

public class MainActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkStateReceiverListener {

    RecyclerView list;
    LazyAdapter adapter;
    ProgressBar progressBar;
    List<String> images = new ArrayList<>();
    RelativeLayout layoutError;
    TextView txtRetry;

    boolean isNetworkAvailable = true;

    private EndlessRecyclerViewScrollListener scrollListener;
    int errorToLoadPage = 0;

    private static final int STORAGE_PERMISSION_CODE = 100;

    private NetworkChangeReceiver networkStateReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list=(RecyclerView) findViewById(R.id.listView1);
        progressBar = findViewById(R.id.progressBar);
        layoutError = findViewById(R.id.layoutError);
        txtRetry = findViewById(R.id.txtRetry);

        startNetworkBroadcastReceiver(this);


        if(!checkStoragePermissions())
        {
            requestForStoragePermissions();
        }else {
            loadNextDataFromApi(1);
        }
        GridLayoutManager layoutManager=new GridLayoutManager(MainActivity.this,2);

        // at last set adapter to recycler view.
        list.setLayoutManager(layoutManager);
        //GridLayoutManager layoutManager=new GridLayoutManager(MainActivity.this,2);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page +1);
                //Toast.makeText(MainActivity.this, page+".." , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onScrollEnd(int pos) {

                if(isNetworkAvailable)
                    showProgressView();
            }
        };
        adapter=new LazyAdapter(MainActivity.this, images);
        list.setAdapter(adapter);
        list.addOnScrollListener(scrollListener);

        txtRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              loadNextDataFromApi(errorToLoadPage);
            }
        });
    }

    void  loadNextDataFromApi(int pageNo)
    {
        //Log.i("aaa", "Load page.." + pageNo);
       // showProgressView();
        UnsplashApiClient.fetchImages(pageNo,new UnsplashApiClient.OnDataFetchedListener() {
            @Override
            public void onDataFetched(String[] imageUrls) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        images.addAll(Arrays.asList(imageUrls));
                        hideErrorLayout();
                        hideProgressView();
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(String errorMessage, int pageNo) {
                errorToLoadPage = pageNo;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorLayout();
                        Toast.makeText(MainActivity.this, "Couldn't load..", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    void showProgressView() {
        hideErrorLayout();
        progressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressView() {
        progressBar.setVisibility(View.GONE);
    }

    void showErrorLayout() {
        hideProgressView();
        layoutError.setVisibility(View.VISIBLE);
        isNetworkAvailable = false;
    }

    void hideErrorLayout() {
        layoutError.setVisibility(View.GONE);
        isNetworkAvailable = true;
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d("TAG", "onActivityResult: Manage External Storage Permissions Granted");
                                    loadNextDataFromApi(1);
                                }else{
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11
                                loadNextDataFromApi(1);

                            }
                        }
                    });

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }

    }

    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    protected void onPause() {
        unregisterNetworkBroadcastReceiver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerNetworkBroadcastReceiver(this);
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        list.setAdapter(null);
        unregisterNetworkBroadcastReceiver(this);
        super.onDestroy();
    }

    @Override
    public void networkAvailable() {
        //Log.i(TAG, "networkAvailable()");
        if(!isNetworkAvailable)
            loadNextDataFromApi(errorToLoadPage);
    }

    @Override
    public void networkUnavailable() {
        //Log.i(TAG, "networkUnavailable()");

    }

    public void startNetworkBroadcastReceiver(Context currentContext) {
        networkStateReceiver = new NetworkChangeReceiver();
        networkStateReceiver.addListener((NetworkChangeReceiver.NetworkStateReceiverListener) currentContext);
        registerNetworkBroadcastReceiver(currentContext);
    }

    public void registerNetworkBroadcastReceiver(Context currentContext) {
        currentContext.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void unregisterNetworkBroadcastReceiver(Context currentContext) {
        currentContext.unregisterReceiver(networkStateReceiver);
    }
}