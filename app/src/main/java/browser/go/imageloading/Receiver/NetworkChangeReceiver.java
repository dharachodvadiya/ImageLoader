package browser.go.imageloading.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import browser.go.imageloading.MainActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {
    protected List<NetworkStateReceiverListener> listeners;
    protected Boolean connected;
    private String TAG = "NetworkStateReceiver";

    public NetworkChangeReceiver() {
        listeners = new ArrayList<>();
        connected = null;
    }

    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent broadcast received");
        if(intent == null || intent.getExtras() == null)
            return;

        // Retrieve a ConnectivityManager for handling management of network connections
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Details about the currently active default data network. When connected, this network is the default route for outgoing connections
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {    //Boolean that indicates whether there is a complete lack of connectivity
            connected = false;
        }

        notifyStateToAll();
    }   //After the onReceive() of the receiver class has finished, the Android system is allowed to recycle the receiver

    private void notifyStateToAll() {
        Log.i(TAG, "Notifying state to " + listeners.size() + " listener(s)");
        for(NetworkStateReceiverListener eachNetworkStateReceiverListener : listeners)
            notifyState(eachNetworkStateReceiverListener);
    }

    private void notifyState(NetworkStateReceiverListener networkStateReceiverListener) {
        if(connected == null || networkStateReceiverListener == null)
            return;

        if(connected == true) {
            // Triggering function on the interface towards network availability
            networkStateReceiverListener.networkAvailable();
        } else {
            // Triggering function on the interface towards network being unavailable
            networkStateReceiverListener.networkUnavailable();
        }
    }

    public void addListener(NetworkStateReceiverListener networkStateReceiverListener) {
        Log.i(TAG, "addListener() - listeners.add(networkStateReceiverListener) + notifyState(networkStateReceiverListener);");
        listeners.add(networkStateReceiverListener);
        notifyState(networkStateReceiverListener);
    }

    public void removeListener(NetworkStateReceiverListener networkStateReceiverListener) {
        listeners.remove(networkStateReceiverListener);
    }

    public interface NetworkStateReceiverListener {
        void networkAvailable();
        void networkUnavailable();
    }
}
