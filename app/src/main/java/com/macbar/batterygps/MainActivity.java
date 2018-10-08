package com.macbar.batterygps;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.macbar.batterygps.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int FINE_LOCATION_REQUEST_CODE = 102;

    private MyViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(MainActivity.this);

        //obtain ViewModel
        mViewModel = ViewModelProviders.of(MainActivity.this).get(MyViewModel.class);

        //connect viewModel with data binding
        binding.setViewmodel(mViewModel);

        //subscribe to viewModel calls
        mViewModel.getAskForLocationPermission().observe(MainActivity.this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void aVoid) {
                requestLocationPermission();
            }
        });

        mViewModel.getAskForToastShow().observe(MainActivity.this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String label) {
                showToast(label);
            }
        });

        mViewModel.getManagerExceptionInfo().observe(MainActivity.this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String label) {
                showToast(label);
            }
        });

        mViewModel.getProviderUnavailableInfo().observe(MainActivity.this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String label) {
                showToast(label);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE:
                boolean isPermissionGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                mViewModel.updateLocationPermissionState(isPermissionGranted ?
                        PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED);
                break;
        }
    }

    private void requestLocationPermission() {
        boolean permissionGranted = true;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionGranted = (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED);
        }

        if(permissionGranted) {
            mViewModel.updateLocationPermissionState(PackageManager.PERMISSION_GRANTED);
        } else {
            requestSpecificPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_REQUEST_CODE);
        }
    }

    private void showToast(String label) {
        if(TextUtils.isEmpty(label)) {
            return;
        }

        Toast.makeText(MainActivity.this, label, Toast.LENGTH_LONG).show();
    }

    private void requestSpecificPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{permission},
                requestCode);
    }
}
