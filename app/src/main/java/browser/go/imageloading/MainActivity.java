package browser.go.imageloading;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import browser.go.imageloading.Adapter.LazyAdapter;
import browser.go.imageloading.UnSplash.UnsplashApiClient;

public class MainActivity extends AppCompatActivity {

    ListView list;
    LazyAdapter adapter;

    private static final int STORAGE_PERMISSION_CODE = 23;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list=(ListView)findViewById(R.id.listView1);


        if(!checkStoragePermissions())
        {
            requestForStoragePermissions();
        }else {
            loadImages();
        }
    }

    void  loadImages()
    {
        UnsplashApiClient.fetchImages(new UnsplashApiClient.OnDataFetchedListener() {
            @Override
            public void onDataFetched(String[] imageUrls) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        adapter=new LazyAdapter(MainActivity.this, imageUrls);
                        list.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "aaa " + errorMessage , Toast.LENGTH_SHORT).show();
            }
        });
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
                                    loadImages();
                                }else{
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11
                                loadImages();

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
    public void onDestroy()
    {
        list.setAdapter(null);
        super.onDestroy();
    }

    private String imageUrls[] = {
            "https://images.unsplash.com/photo-1712847331865-f11e31be73cf?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w1OTE3ODV8MXwxfGFsbHwxfHx8fHx8Mnx8MTcxMzM3Nzk5OHw&ixlib=rb-4.0.3&q=80&w=1080",
            "https://images.unsplash.com/photo-1713189005053-e38b1b88ac4a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w1OTE3ODV8MHwxfGFsbHwyfHx8fHx8Mnx8MTcxMzM3Nzk5OHw&ixlib=rb-4.0.3&q=80&w=1080"

    };
}