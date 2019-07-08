package br.com.mapchallenge.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * create by Jhonny
 */
public class AutocompletePlaces implements Parcelable {

    private String address;
    private String placeId;

    public AutocompletePlaces() {

    }

    protected AutocompletePlaces(Parcel in) {
        address = in.readString();
        placeId = in.readString();
    }

    public static final Creator<AutocompletePlaces> CREATOR = new Creator<AutocompletePlaces>() {
        @Override
        public AutocompletePlaces createFromParcel(Parcel in) {
            return new AutocompletePlaces(in);
        }

        @Override
        public AutocompletePlaces[] newArray(int size) {
            return new AutocompletePlaces[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(address);
        parcel.writeString(placeId);
    }
}
