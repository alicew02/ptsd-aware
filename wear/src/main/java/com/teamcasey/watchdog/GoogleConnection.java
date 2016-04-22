package com.teamcasey.watchdog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.Observable;

/**
 * A helper class to manage all of our api calls to google's services aka what we need
 * to use to send messages from watch to phone
 *
 * Uses a singleton pattern, which means that you should always use it like:
 * GoogleConnection connection = GoogleConnection.getInstance()
 * NOT LIKE new GoogleConnection()
 */
public class GoogleConnection extends Observable implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private boolean connected = false;
    private static GoogleConnection sGoogleConnection;
    private GoogleApiClient.Builder googleApiClientBuilder;
    private GoogleApiClient googleApiClient;

    /**
     * Private constructor used by getInstance(), creates an apiClient
     *
     * @param context
     */
    private GoogleConnection(Context context) {
        this.googleApiClientBuilder = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);

        this.googleApiClient = this.googleApiClientBuilder.build();
    }

    /**
     * Singleton patten getInstance method, how to access this class from the outside world
     *
     * @param context
     * @return instance of GoogleConnection
     */
    public static GoogleConnection getInstance(Context context) {
        if (null == sGoogleConnection) {
            sGoogleConnection = new GoogleConnection(context);
        }

        return sGoogleConnection;
    }

    /**
     * Quick way to connect to the googleAPI
     */
    public void connect() {
        this.googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        this.connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        this.connected = false;
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        this.connected = false;
        connect();
    }

    /**
     * Getter for the googleAPIClient of the current instance
     *
     * @return GoogleApiClient object
     */
    public GoogleApiClient getClient() {
        return this.googleApiClient;
    }
}
