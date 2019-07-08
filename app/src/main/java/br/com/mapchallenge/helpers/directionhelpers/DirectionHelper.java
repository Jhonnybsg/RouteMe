package br.com.mapchallenge.helpers.directionhelpers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import br.com.mapchallenge.R;

/**
 * created by Jhonny
 */
public class DirectionHelper {

    private static final String PATH = "https://maps.googleapis.com/maps/api/directions/";
    private Context context;

    public DirectionHelper(Context context) {
        this.context = context;
    }

    //setup Direction request and retrieves the json
    public void getRoute(LatLng origin, LatLng destiny) {
        new FetchURL(context).execute(getUrl(origin, destiny, "driving"), "driving");
    }

    //setup the url for the request
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = PATH + output + "?" + parameters + "&key=" + context.getString(R.string.google_maps_key);
        return url;
    }
}
