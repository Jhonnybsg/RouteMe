package br.com.mapchallenge.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import br.com.mapchallenge.R;
import br.com.mapchallenge.adapters.AutocompleteAdapter;
import br.com.mapchallenge.helpers.LocationHelper;
import br.com.mapchallenge.helpers.PlaceHelper;
import br.com.mapchallenge.helpers.directionhelpers.DirectionHelper;
import br.com.mapchallenge.helpers.directionhelpers.TaskLoadedCallback;
import br.com.mapchallenge.listeners.OnItemClick;
import br.com.mapchallenge.model.AutocompletePlaces;
import br.com.mapchallenge.presenter.MapScreenPresenter;
import br.com.mapchallenge.utils.Const;
import br.com.mapchallenge.view.MapScreenView;


/**
 * created by Jhonny
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback, MapScreenView, OnItemClick {

    private static final String TRACE_COLOR = "#3498db";
    private static final int ROUTE_PADDING = 350;

    private LatLng currentDestiny;
    private GoogleMap mMap;
    private LocationHelper locationHelper;
    private Polyline currentPolyline;
    private DirectionHelper directionHelper;

    private RecyclerView autocompleteList;
    private AutocompleteAdapter autocompleteAdapter;
    private EditText searchEditText;
    private ImageButton clearSearch;
    private ProgressBar progressBar;

    private MapScreenPresenter screenPresenter;
    private PlaceHelper placeHelper;
    private Marker destinyMarker;

    private boolean isGPS = false;
    private boolean mLocationPermissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationHelper = new LocationHelper(this, this);
        directionHelper = new DirectionHelper(this);

        autocompleteList = findViewById(R.id.autocomplete_list);
        autocompleteList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(autocompleteList.getContext(),
                linearLayoutManager.getOrientation());

        autocompleteList.addItemDecoration(dividerItemDecoration);
        autocompleteList.setLayoutManager(linearLayoutManager);

        searchEditText = findViewById(R.id.search_field);
        searchEditText.setOnEditorActionListener(edittextActionListener);
        clearSearch = findViewById(R.id.clear_button);
        progressBar = findViewById(R.id.loading_progress);

        Drawable progressDrawable = progressBar.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(progressDrawable);

        autocompleteAdapter = new AutocompleteAdapter(this, this);
        autocompleteList.setAdapter(autocompleteAdapter);

        searchEditText.addTextChangedListener(searchFieldWatcher);
        clearSearch.setOnClickListener(onClickClearSearch);

        placeHelper = new PlaceHelper(this);

        screenPresenter = new MapScreenPresenter(this, placeHelper);

    }

    /**
     * Handles keyboard search action event
     */
    TextView.OnEditorActionListener edittextActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH && v.getText().length() != 0) {
                screenPresenter.searchLocation(autocompleteAdapter.getItemPlaceId(0));
                searchEditText.setText(autocompleteAdapter.getItemContent(0));
                hideSoftKeyboard(MapsActivity.this);
                searchEditText.clearFocus();
                return true;
            }
            return false;
        }
    };

    /**
     * Handles item clicks on the Autocomplete list
     */
    View.OnClickListener onClickClearSearch = new View.OnClickListener() {
        private static final String BLANKE = "";

        @Override
        public void onClick(View view) {
            searchEditText.setText(BLANKE);
            autocompleteList.setVisibility(View.GONE);
        }
    };

    /**
     * Watches changes in the search edittext
     */
    TextWatcher searchFieldWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            screenPresenter.handleTextChange(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    /**
     * Sets up the map and its settings
     */
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationHelper.getCurrentLatLng()));
        mMap.addMarker(new MarkerOptions()
                .position(locationHelper.getCurrentLatLng())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.origin_map_icon)));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(locationHelper.getCurrentLatLng()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationHelper.getCurrentLatLng(), 13));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(0, 0, 0, 100);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                autocompleteList.setVisibility(View.GONE);
                hideSoftKeyboard(MapsActivity.this);
                searchEditText.clearFocus();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                locationHelper.updateCurrentLocation();
            } else {
                getLocationPermission();
            }
        }
    }

    //    /**
//     * handles device permissions
//     *
//     * @param requestCode
//     * @param permissions
//     * @param grantResults
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case LocationHelper.REQUEST_CODE:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    locationHelper.updateCurrentLocation();
//                }
//                break;
//            default:
//                break;
//        }
//    }

    /**
     * Handles the parsing result of directions from Direction API
     *
     * @param values
     */
    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline(((PolylineOptions) values[0]).color(Color.parseColor(TRACE_COLOR)));
        progressBar.setVisibility(View.GONE);

        updateMarkerPos(currentDestiny);
        zoomRoute(mMap, currentDestiny);
    }


    /**
     * Upates the destiny marker on the map
     *
     * @param latLng
     */
    @Override
    public void updateMarkerPos(LatLng latLng) {

        if (destinyMarker == null) {
            destinyMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destiny_map_icon)));
        } else {
            destinyMarker.setPosition(latLng);
        }

    }

    /**
     * Adjust camera to zoom over the whole route
     *
     * @param googleMap
     * @param destiny
     */
    public void zoomRoute(GoogleMap googleMap, LatLng destiny) {

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(locationHelper.getCurrentLatLng());
        boundsBuilder.include(destiny);


        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, ROUTE_PADDING));
    }

    /**
     * sends a new list with the predictions from Places API
     *
     * @param places
     */
    @Override
    public void updateAutocompleteList(List<AutocompletePlaces> places) {
        this.autocompleteAdapter.updateItems(places);
    }

    /**
     * called to determine when to show the autocomplete list view
     *
     * @param visibility
     */
    @Override
    public void updateAutocompleteVisibility(int visibility) {
        autocompleteList.setVisibility(visibility);
    }

    /**
     * called to draw the route on the map based on the destin Latlng
     *
     * @param destiny
     */
    @Override
    public void drawRouteOnMap(LatLng destiny) {
        directionHelper.getRoute(locationHelper.getCurrentLatLng(), destiny);
        currentDestiny = destiny;
    }

    /**
     * Used to show or hide the progress bar when a search is in progress
     *
     * @param visibility
     */
    @Override
    public void updateProgressVisibility(int visibility) {
        this.progressBar.setVisibility(visibility);
    }

    @Override
    public void showUiMessage(String message) {
        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateSearchFieldContent(String term) {
        this.searchEditText.setText(term);
    }

    /**
     * handles information from the chosena autocomplete item and fetches the corresponding route
     *
     * @param placeId
     */
    @Override
    public void onItemClick(String placeId) {
        progressBar.setVisibility(View.VISIBLE);
        screenPresenter.searchLocation(placeId);
        updateAutocompleteVisibility(View.GONE);
        hideSoftKeyboard(this);
    }

    /**
     * handles the keboard visibilty
     *
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Checks if GPS is enabled
     *
     * @return
     */
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    /**
     * checks wheter googl play services is installed and available
     *
     * @return
     */
    public boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, Const.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Check if the google map service is enabled
     *
     * @return
     */
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sends an alert about the GPS usage need
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gp_message_dialog))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, Const.PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Handles location permissions
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            locationHelper.updateCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Const.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case Const.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Const.PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    locationHelper.updateCurrentLocation();
                } else {
                    getLocationPermission();
                }
            }
        }

    }
}
