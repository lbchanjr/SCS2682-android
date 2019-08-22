package com.ut.cinemafinder;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Theater implements Serializable {

    String name;
    String address;
    String url;
    LatLng coords;

    Theater(String name, String address, String url, LatLng coords) {
        this.name = name;
        this.address = address;
        this.url = url;
        this.coords = coords;
    }
}
