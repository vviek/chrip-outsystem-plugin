package com.cordova.plugin;
// The native Toast API

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
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
    private static final String SET_CONFIGRATION = "setConfigration";
    private static final String CHECK_PERMISSION = "checkPermission";
    private static final String SEND_DATA = "sendData";
    private static final String REGISTER_AS_RECEIVER = "registerAsReceiver";

    ChirpConnect chirp;
    CallbackContext context;

    String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {
        // Verify that the user sent a 'show' action
        context = callbackContext;

        switch (action) {
            case SET_CONFIGRATION:
                setConfigration(callbackContext,args);
                break;
            case CHECK_PERMISSION:
                checkPermission(callbackContext);
                break;
            case SEND_DATA:
                try {
                    sendData(callbackContext, args.getString(0));
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

    private void registerAsReceiver() {
        chirp.setListener(this);
    }

    private void sendData(CallbackContext callbackContext, String dataToSend) {
        try {
            byte[] payload = dataToSend.getBytes(Charset.forName("UTF-8"));

            ChirpError error = chirp.send(payload);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Code", error.getCode());
            jsonObject.put("Message", error.getMessage());

            callbackContext.success(error.getCode());
        } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
        }

    }

    private void checkPermission(CallbackContext callbackContext) {

        if (hasPermisssion()) {
            PluginResult r = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(r);
        } else {
            PermissionHelper.requestPermissions(this, 0, permissions);
        }
    }

    private void setConfigration(CallbackContext callbackContext, JSONArray args) {

        try {

            //chirp = new ChirpConnect(cordova.getActivity(), CHIRP_APP_KEY, CHIRP_APP_SECRET);
            chirp = new ChirpConnect(cordova.getActivity(), args.getString(0),args.getString(1));

//            ChirpError error = chirp.setConfig(CHIRP_APP_CONFIG);
            ChirpError error = chirp.setConfig(args.getString(2));
            if (error.getCode() == 0) {
                callbackContext.success(error.getCode());
            } else {
                callbackContext.success(error.getCode());
            }
        } catch (Exception ex) {
            callbackContext.error(ex.getMessage());

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
            result = new PluginResult(PluginResult.Status.OK);
            context.sendPluginResult(result);
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