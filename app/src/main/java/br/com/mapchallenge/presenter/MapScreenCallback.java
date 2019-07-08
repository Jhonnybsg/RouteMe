package br.com.mapchallenge.presenter;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.List;

/**
 * Created by Jhonny
 */
public interface MapScreenCallback {

    /**
     * Sends back to presenter the result of predictions
     * @param predictions
     */
    void sendPredictionsResult(List<AutocompletePrediction> predictions);


    /**
     * Sends back to the presenter the latLng result
     * @param latLng
     */
    void sendLatLngResult(LatLng latLng);

}
