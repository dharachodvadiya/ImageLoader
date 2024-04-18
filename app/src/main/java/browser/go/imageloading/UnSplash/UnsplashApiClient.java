package browser.go.imageloading.UnSplash;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnsplashApiClient {

    private static final String TAG = UnsplashApiClient.class.getSimpleName();

    private static final String ACCESS_KEY = "Bsc_4PsXxQCIiBObwLgF0LGmChGK2OvL0NdnudK8EH8";
    private static final String BASE_URL = "https://api.unsplash.com/photos/";

    public interface OnDataFetchedListener {
        void onDataFetched(String[] imageUrls);
        void onError(String errorMessage, int pageNo);
    }

    public static void fetchImages(int pageNo, final OnDataFetchedListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(BASE_URL + "?page=" + pageNo + "&per_page=30&client_id=" + ACCESS_KEY);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    StringBuilder response = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String result = response.toString();
                    JSONArray jsonArray = new JSONArray(result);
                    String[] imageUrls = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String imageUrl = jsonObject.getJSONObject("urls").getString("regular");
                        imageUrls[i] = imageUrl;
                    }
                    listener.onDataFetched(imageUrls);
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching images: " + e.getMessage());
                    listener.onError("Failed to fetch images. Please check your internet connection.", pageNo);
                }
            }
        });
    }
}
