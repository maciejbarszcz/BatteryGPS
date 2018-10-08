package com.macbar.batterygps;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.macbar.batterygps.data.DataRepository;

public class MyViewModel extends AndroidViewModel {

    public final MediatorLiveData<String> startButtonLabel = new MediatorLiveData<>();
    public final MediatorLiveData<String> stopButtonLabel = new MediatorLiveData<>();
    public final MediatorLiveData<Boolean> startButtonEnabled = new MediatorLiveData<>();
    public final MediatorLiveData<Boolean> stopButtonEnabled = new MediatorLiveData<>();

    private final SingleLiveEvent<Void> mAskForLocationPermission = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> mAskForToastShow = new SingleLiveEvent<>();

    private final MediatorSingleLiveEvent<String> mManagerExceptionInfo = new MediatorSingleLiveEvent<>();
    private final MediatorSingleLiveEvent<String> mProviderUnavailableInfo = new MediatorSingleLiveEvent<>();

    private int mLocationPermissionState = PackageManager.PERMISSION_DENIED;//nominal state, to be verified

    public MyViewModel(@NonNull Application application) {
        super(application);

        initModelMediators();

        //nominal buttons state
        startButtonEnabled.setValue(true);
        stopButtonEnabled.setValue(false);

        setButtonLabels();
    }

    SingleLiveEvent<Void> getAskForLocationPermission() {
        return mAskForLocationPermission;
    }

    SingleLiveEvent<String> getAskForToastShow() {
        return mAskForToastShow;
    }

    public MediatorSingleLiveEvent<String> getManagerExceptionInfo() {
        return mManagerExceptionInfo;
    }

    public MediatorSingleLiveEvent<String> getProviderUnavailableInfo() {
        return mProviderUnavailableInfo;
    }

    private void initModelMediators() {
        startButtonLabel.addSource(DataRepository.getRepository().getIsDataProcessingRunning(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isServiceRunning) {
                if(isServiceRunning == null) {
                    return;
                }

                startButtonLabel.setValue(getStartButtonLabel(isServiceRunning));
            }
        });

        stopButtonLabel.addSource(DataRepository.getRepository().getIsDataProcessingRunning(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isServiceRunning) {
                if(isServiceRunning == null) {
                    return;
                }

                stopButtonLabel.setValue(getStopButtonLabel(isServiceRunning));
            }
        });

        startButtonEnabled.addSource(DataRepository.getRepository().getIsDataProcessingRunning(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isServiceRunning) {
                if(isServiceRunning == null) {
                    return;
                }

                startButtonEnabled.setValue(!isServiceRunning);
            }
        });

        stopButtonEnabled.addSource(DataRepository.getRepository().getIsDataProcessingRunning(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isServiceRunning) {
                if(isServiceRunning == null) {
                    return;
                }

                stopButtonEnabled.setValue(isServiceRunning);
            }
        });

        mManagerExceptionInfo.addSource(DataRepository.getRepository().getInformAboutManagerException(), new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void aVoid) {
                mManagerExceptionInfo.setValue(prepareLocationExceptionToastLabel());
            }
        });

        mProviderUnavailableInfo.addSource(DataRepository.getRepository().getLocationProviderUnavailable(), new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void aVoid) {
                mProviderUnavailableInfo.setValue(prepareLocationUnavailableToastLabel());
            }
        });
    }

    public void onStartButtonClicked() {
        //start with check location permission
        if(mLocationPermissionState != PackageManager.PERMISSION_GRANTED) {
            mAskForLocationPermission.call();
        } else {
            startServices();
        }
    }

    public void onStopButtonClicked() {
        terminateServices();
    }

    void updateLocationPermissionState(int permissionState) {
        mLocationPermissionState = permissionState;
        if(mLocationPermissionState == PackageManager.PERMISSION_GRANTED) {
            startServices();
        } else {
            mAskForToastShow.setValue(preparePermissionDeniedToastLabel());
        }
    }

    private void startServices() {
       DataRepository.getRepository().startDataProcessing(getApplication().getApplicationContext());
    }

    private void terminateServices() {
        DataRepository.getRepository().stopDataProcessing();
    }

    private String preparePermissionDeniedToastLabel() {
        Context context = getApplication().getApplicationContext();
        return context.getResources().getString(R.string.toast_permission_denied);
    }

    private String prepareLocationUnavailableToastLabel() {
        Context context = getApplication().getApplicationContext();
        return context.getResources().getString(R.string.toast_location_service_unavailable);
    }

    private String prepareLocationExceptionToastLabel() {
        Context context = getApplication().getApplicationContext();
        return context.getResources().getString(R.string.toast_location_service_exception);
    }

    private String getStartButtonLabel(boolean isServiceRunning) {
        Context context = getApplication().getApplicationContext();
        return context.getResources().getString(isServiceRunning ? R.string.btn_start_running : R.string.btn_start_enabled);
    }

    private String getStopButtonLabel(boolean isServiceRunning) {
        Context context = getApplication().getApplicationContext();
        return context.getResources().getString(isServiceRunning ? R.string.btn_stop_enabled : R.string.btn_stop_disabled);
    }

    private void setButtonLabels() {
        Context appContext = getApplication().getApplicationContext();
        if(stopButtonEnabled.getValue() == null || startButtonEnabled.getValue() == null) {
            return;
        }

        if(stopButtonEnabled.getValue()) {
            stopButtonLabel.setValue(appContext.getResources().getString(R.string.btn_stop_enabled));
        } else {
            stopButtonLabel.setValue(appContext.getResources().getString(R.string.btn_stop_disabled));
        }

        if(startButtonEnabled.getValue()) {
            startButtonLabel.setValue(appContext.getResources().getString(R.string.btn_start_enabled));
        } else {
            startButtonLabel.setValue(appContext.getResources().getString(R.string.btn_start_running));
        }
    }

}
