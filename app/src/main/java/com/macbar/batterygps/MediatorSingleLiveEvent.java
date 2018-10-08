package com.macbar.batterygps;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MediatorLiveEvent based on solution from SingleLiveEvent
 * @see SingleLiveEvent
 */
public class MediatorSingleLiveEvent<T> extends MediatorLiveData<T> {

    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    public void observe(LifecycleOwner owner, final Observer<T> observer) {

        // Observe the internal MutableLiveData
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        });
    }

    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    public synchronized void postValue(final T t) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                setValue(t);
            }
        });
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void call() {
        setValue(null);
    }

    public synchronized void postCall() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                setValue(null);
            }
        });
    }
}
