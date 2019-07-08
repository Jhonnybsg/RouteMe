package br.com.mapchallenge.presenter;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.ArrayList;
import java.util.List;

import br.com.mapchallenge.helpers.PlaceHelper;
import br.com.mapchallenge.model.AutocompletePlaces;
import br.com.mapchallenge.view.MapScreenView;

/**
 * Created by Jhonny
 */
public class MapScreenPresenter implements MapScreenCallback {

    private MapScreenView mapScreenView;
    private PlaceHelper placeHelper;

    public MapScreenPresenter(MapScreenView mapScreenView, PlaceHelper placeHelper) {
        this.mapScreenView = mapScreenView;
        this.placeHelper = placeHelper;
    }

    /**
     * Executes a search with Places API to get the place LatLng
     *
     * @param placeId
     */
    public void searchLocation(String placeId) {
        if (placeId != null) {
            if (placeId.length() > 0 && !placeId.equals("unknown")) {
                placeHelper.loadLocation(this, placeId);
                mapScreenView.updateProgressVisibility(View.VISIBLE);
                mapScreenView.updateAutocompleteVisibility(View.GONE);
            } else if (placeId.equals("unknown")){
                mapScreenView.showUiMessage("Destino não encontrado");
            }
        } else {
            mapScreenView.showUiMessage("Digite o seu destino");
        }
    }

    /**
     * Checks if the term is valid an not null to proceed to the predictions fetch
     *
     * @param term
     */
    public void handleTextChange(String term) {
        if (term != null && term.length() > 0) {
            placeHelper.getPredictions(this, term);
        } else {
            mapScreenView.updateAutocompleteVisibility(View.GONE);
        }
    }

    /**
     * Sends the list of predictins to the view
     *
     * @param predictions
     */
    @Override
    public void sendPredictionsResult(List<AutocompletePrediction> predictions) {

        List<AutocompletePlaces> places = new ArrayList<>();

        for (AutocompletePrediction prediction : predictions) {
            AutocompletePlaces place = new AutocompletePlaces();
            place.setAddress(prediction.getFullText(null).toString());
            place.setPlaceId(prediction.getPlaceId());
            places.add(place);
        }

        mapScreenView.updateAutocompleteList(places);

        if (places.size() > 0) {
            mapScreenView.updateAutocompleteVisibility(View.VISIBLE);
        } else {
            mapScreenView.updateAutocompleteVisibility(View.GONE);
        }
    }

    /**
     * Executes the call to get the Polylines and draw on the map based on the latlng
     *
     * @param latLng
     */
    @Override
    public void sendLatLngResult(LatLng latLng) {
        mapScreenView.drawRouteOnMap(latLng);
    }
}
