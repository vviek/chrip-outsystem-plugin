package com.cordova.plugin;


import android.Manifest;
import android.content.pm.PackageManager;

import com.example.hello.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

import io.chirp.connect.ChirpConnect;
import io.chirp.connect.interfaces.ConnectEventListener;
import io.chirp.connect.models.ChirpError;

public class ChripPlugin extends CordovaPlugin implements ConnectEventListener {
    private static final String STOP_CHIRP = "stop";
    private static final String SEND_DATA = "sendData";
    private static final String REGISTER_AS_RECEIVER = "registerAsReceiver";

    ChirpConnect chirp;
    CallbackContext context;


    String[] permissions = {Manifest.permission.RECORD_AUDIO};
    CordovaWebView cordovaWebView;
    String dataToSend;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cordovaWebView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {
        // Verify that the user sent a 'show' action
        context = callbackContext;

        switch (action) {
            case STOP_CHIRP:
                stopChirp(callbackContext);
                break;
            case SEND_DATA:
                try {
                    dataToSend = args.getString(0);
                    checkPermission();
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
                break;

            case REGISTER_AS_RECEIVER:
                registerAsReceiver();

                break;

        }


        return true;
    }

    private void stopChirp(CallbackContext callbackContext) {
        chirp.stop();
        try {
            chirp.close();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

        callbackContext.success();
    }

    private void registerAsReceiver() {
        if (chirp == null) {
            chirp = new ChirpConnect(cordova.getActivity(), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_SECRET), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_KEY));
            chirp.setConfig(cordova.getActivity().getResources().getString(R.string.CHIRP_APP_CONFIG));
        }
        chirp.setListener(this);
        
    }

    private void sendData() {
        try {
            chirp = new ChirpConnect(cordova.getActivity(), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_SECRET), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_KEY));
            ChirpError errorConfig = chirp.setConfig(cordova.getActivity().getResources().getString(R.string.CHIRP_APP_CONFIG));
            if (errorConfig.getCode() != 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Code", errorConfig.getCode());
                jsonObject.put("Message", errorConfig.getMessage());
                context.error(jsonObject);
                return;
            }
            byte[] payload = dataToSend.getBytes(Charset.forName("UTF-8"));
            ChirpError error = chirp.send(payload);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Code", error.getCode());
            jsonObject.put("Message", error.getMessage());

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }

    }

    private void checkPermission() {

        if (hasPermisssion()) {
            sendData();
        } else {
            PermissionHelper.requestPermissions(this, 0, permissions);
        }
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if (context != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    context.sendPluginResult(result);
                    return;
                }

            }
            sendData();
        }
    }

    public boolean hasPermisssion() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onReceived(@Nullable byte[] bytes, int i) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("state", "onReceived");
            jsonObject.put("data", new String(bytes));
            jsonObject.put("stateCode", i);

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }
    }

    @Override
    public void onReceiving(int i) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("state", "onReceiving");
            jsonObject.put("data", "");
            jsonObject.put("stateCode", i);

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }
    }

    @Override
    public void onSending(@NotNull byte[] bytes, int i) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("state", "onSending");
            jsonObject.put("data", new String(bytes));
            jsonObject.put("stateCode", i);

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }
    }

    @Override
    public void onSent(@NotNull byte[] bytes, int i) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("state", "onSent");
            jsonObject.put("data", new String(bytes));
            jsonObject.put("stateCode", i);

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }
    }

    @Override
    public void onStateChanged(int i, int i1) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("state", "onStateChanged");
            jsonObject.put("beforeStateCode", i);
            jsonObject.put("afterStateCode", i1);

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }
    }

    @Override
    public void onSystemVolumeChanged(float v, float v1) {
        //cordovaWebView.sendJavascript();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("state", "onSystemVolumeChanged");
            jsonObject.put("volumeStateCode", v);
            jsonObject.put("volumeStateCode", v1);

            context.success(jsonObject);
        } catch (Exception ex) {
            context.error(ex.getMessage());
        }
    }
}