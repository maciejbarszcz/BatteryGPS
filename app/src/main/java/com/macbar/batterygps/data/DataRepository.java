package com.macbar.batterygps.data;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.macbar.batterygps.Config;
import com.macbar.batterygps.SingleLiveEvent;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * By now DataRepository is implemented as a Singleton.
 * Ultimately, dependencies should be injected with Dagger
 */
public class DataRepository {

    //TODO - communication with helpers should be done with interfaces
    private BatteryHelper mBatteryHelper = null;
    private LocationHelper mLocationHelper = null;
    private NetworkHelper mNetworkHelper = null;

    private final MutableLiveData<Boolean> mIsDataProcessingRunning = new MutableLiveData<>();
    private final SingleLiveEvent<Void> mLocationProviderUnavailable = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> mInformAboutManagerException = new SingleLiveEvent<>();

    private Disposable mDataProcessingDisposable = null;
    private Disposable mProviderUnavailableDisposable = null;

    private static DataRepository INSTANCE = null;

    private DataRepository() {

    }

    public static DataRepository getRepository() {
        if(INSTANCE == null) {
            INSTANCE = new DataRepository();
        }

        return INSTANCE;
    }

    public MutableLiveData<Boolean> getIsDataProcessingRunning() {
        return mIsDataProcessingRunning;
    }

    public SingleLiveEvent<Void> getLocationProviderUnavailable() {
        return mLocationProviderUnavailable;
    }

    public SingleLiveEvent<Void> getInformAboutManagerException() {
        return mInformAboutManagerException;
    }

    public void startDataProcessing(Context context) {
        if(mBatteryHelper == null) {
            mBatteryHelper = new BatteryHelper();
        }

        if(mLocationHelper == null) {
            mLocationHelper = new LocationHelper();
        }

        if(mNetworkHelper == null) {
            mNetworkHelper = new NetworkHelper();
        }

        Observable<LocationWrapper> locationObservable = mLocationHelper.createLocationObservable(context);
        if(locationObservable == null) {
            mIsDataProcessingRunning.setValue(false);
            return;
        }

        Observable<String> batteryObservable = mBatteryHelper.createObservable(context);
        if(batteryObservable == null) {
            mIsDataProcessingRunning.setValue(false);
            return;
        }

        final Observable<Boolean> locationAvailabilityObservable = mLocationHelper.createAvailabilityObservable(context);
        if(locationAvailabilityObservable == null) {
            mIsDataProcessingRunning.setValue(false);
            return;
        }

        mDataProcessingDisposable = locationObservable.filter(new Predicate<LocationWrapper>() {
            @Override
            public boolean test(LocationWrapper locationWrapper) {
                changeAvailabilityObservableState(locationAvailabilityObservable, locationWrapper.isServiceEnabled());
                return locationWrapper.isServiceEnabled();
            }
        }).map(new Function<LocationWrapper, String>() {
            @Override
            public String apply(LocationWrapper locationWrapper) {
                return (locationWrapper.getLatitude() + "; " + locationWrapper.getLongitude());
            }
        }).mergeWith(batteryObservable).
                buffer(Config.ITEMS_COUNT_TO_SEND).map(new Function<List<String>, String>() {
            @Override
            public String apply(List<String> strings) {
                StringBuilder sb = new StringBuilder();
                for(String str: strings) {
                    sb.append(str).append(", ");
                }
                return sb.toString();
            }
        }).observeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                mNetworkHelper.sendStringOverHTTP(s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                //react to exception thrown by LocationManager
                mIsDataProcessingRunning.postValue(false);
                mInformAboutManagerException.postCall();
            }
        });

        mIsDataProcessingRunning.setValue(true);
    }

    private void changeAvailabilityObservableState(Observable<Boolean> observable, boolean isAvailable) {
        if(isAvailable) {
            if(mProviderUnavailableDisposable != null && !mProviderUnavailableDisposable.isDisposed()) {
                mProviderUnavailableDisposable.dispose();
            }
        } else {
            if(mProviderUnavailableDisposable == null ||
                    mProviderUnavailableDisposable.isDisposed()) {
                mProviderUnavailableDisposable = observable.subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean isAvailable) {
                                if(!isAvailable) {
                                    mLocationProviderUnavailable.postCall();
                                }
                            }
                });
            }
        }
    }

    public void stopDataProcessing() {
        if(mDataProcessingDisposable != null && !mDataProcessingDisposable.isDisposed()) {
            mDataProcessingDisposable.dispose();
            mIsDataProcessingRunning.setValue(false);
        }

        if(mProviderUnavailableDisposable != null && !mProviderUnavailableDisposable.isDisposed()) {
            mProviderUnavailableDisposable.dispose();
        }
    }
}
