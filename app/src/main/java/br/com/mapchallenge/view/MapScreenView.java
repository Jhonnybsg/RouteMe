package br.com.mapchallenge.view;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import br.com.mapchallenge.model.AutocompletePlaces;

/**
 * created by Jhonny
 */
public interface MapScreenView {

    /**
     * Add a new marker on the map based on the given LatLng
     * @param coords
     */
    void updateMarkerPos(LatLng coords);

    /**
     * Update adapter items list with given new AutocompletePlaces
     * @param items
     */
    void updateAutocompleteList(List<AutocompletePlaces> items);

    /**
     * Change autocomplete view visibility
     * @param visibility
     */
    void updateAutocompleteVisibility(int visibility);

    /**
     * Draws the Polylines to the destination
     * @param destiny
     */
    void drawRouteOnMap(LatLng destiny);

    /**
     * Changes the progress view visibiliy according to the searching status
     * @param visibility
     */
    void updateProgressVisibility(int visibility);

    /**
     * sends a message to be displayed on the screen
     * @param message
     */
    void showUiMessage(String message);
}
