package com.macbar.batterygps.data;

import android.content.Context;
import android.os.BatteryManager;

import com.macbar.batterygps.Config;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class BatteryHelper {

    public Observable<String> createObservable(Context context) {

        final BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if(batteryManager == null) {
            return null;
        }

        return Observable.interval(Config.BATTERY_GATHER_MILLIS, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(Long aLong) {
                int value = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                return String.valueOf(value);
            }
        });
    }
}
