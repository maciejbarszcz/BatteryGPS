package com.macbar.batterygps.data;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.macbar.batterygps.Config;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LocationHelper {

    public LocationHelper() {
    }

    public Observable<Boolean> createAvailabilityObservable(Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager == null) {
            return null;
        }

        return Observable.interval(Config.PROVIDER_UNAVAILABLE_INFORM_INTERVAL, TimeUnit.MILLISECONDS).map(new Function<Long, Boolean>() {
            @Override
            public Boolean apply(Long aLong) {
                return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        });
    }

    public Observable<LocationWrapper> createLocationObservable(Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager == null) {
            return null;
        }

        return Observable.create(new ObservableOnSubscribe<LocationWrapper>() {
            @Override
            public void subscribe(final ObservableEmitter<LocationWrapper> emitter) throws SecurityException {

                final LocationListener sLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if(!emitter.isDisposed()) {
                            emitter.onNext(new LocationWrapper(String.valueOf(location.getLatitude()),
                                    String.valueOf(location.getLongitude()), true));
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        //unused
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        //unused
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        if(!emitter.isDisposed()) {
                            emitter.onNext(new LocationWrapper(null, null, false));
                        }
                    }
                };

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        if(locationManager != null) {
                            locationManager.removeUpdates(sLocationListener);
                        }
                    }
                });

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        Config.LOCATION_GATHER_MILLIS, 0, sLocationListener);
            }
        }).observeOn(Schedulers.io());
    }
}
