package br.com.mapchallenge.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import br.com.mapchallenge.R;
import br.com.mapchallenge.presenter.MapScreenCallback;

public class PlaceHelper {

    private static final String COUNTRY_CODE = "BR";

    private Context context;
    private AutocompleteSessionToken token;
    private PlacesClient placesClient;
    private FindAutocompletePredictionsRequest request;
    private List<Place.Field> placeFields;

    public PlaceHelper(Context context) {

        this.context = context;

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_maps_key));
        }

        placesClient = Places.createClient(context);

        token = AutocompleteSessionToken.newInstance();

        placeFields = Arrays.asList(Place.Field.ADDRESS,
                Place.Field.ID,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG);

    }

    /**
     * fetch predictions from google Places API
     */
    public void getPredictions(final MapScreenCallback callback, String term) {

        request = FindAutocompletePredictionsRequest.builder()
                .setCountry(COUNTRY_CODE)
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(term)
                .build();


        placesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse response) {

                callback.sendPredictionsResult(response.getAutocompletePredictions());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("PLACES", "Place not found: " + apiException.getStatusCode());
                }
            }
        });
    }

    /**
     * Find location based on its PlaceID
     *
     * @param placeId
     */
    public void loadLocation(final MapScreenCallback callback, String placeId) {

        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {

                callback.sendLatLngResult(response.getPlace().getLatLng());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                }
            }
        });
    }
}
