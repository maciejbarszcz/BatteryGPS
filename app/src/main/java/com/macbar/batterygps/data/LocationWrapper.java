package com.macbar.batterygps.data;

public class LocationWrapper {

    private String mLatitude = null;
    private String mLongitude = null;

    private boolean mIsServiceEnabled = false;

    public LocationWrapper() {

    }

    public LocationWrapper(String mLatitude, String mLongitude, boolean mIsServiceEnabled) {
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mIsServiceEnabled = mIsServiceEnabled;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public boolean isServiceEnabled() {
        return mIsServiceEnabled;
    }
}
