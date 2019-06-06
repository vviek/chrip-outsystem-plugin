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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

import io.chirp.connect.ChirpConnect;
import io.chirp.connect.models.ChirpError;

public class ChripPlugin extends CordovaPlugin {
    private static final String SET_CONFIGRATION = "setConfigration";
    private static final String CHECK_PERMISSION = "checkPermission";
    private static final String SEND_DATA = "sendData";
    ChirpConnect chirp;
    CallbackContext context;
    String CHIRP_APP_KEY = "9ac1BE567bBa4b1E80bBaEE1d";
    String CHIRP_APP_SECRET = "ED1703F312A9Cc7B0C5fFccd841BCaDFb880CDb518c9C6b23F";
    String CHIRP_APP_CONFIG = "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";

    String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {
        // Verify that the user sent a 'show' action
        context = callbackContext;

        switch (action) {
            case SET_CONFIGRATION:
                setConfigration(callbackContext);
                break;
            case CHECK_PERMISSION:
                startSDK(callbackContext);
                break;
            case SEND_DATA:
                try {
                    sendData(callbackContext, args.getString(0));
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
                break;


        }


        return true;
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

    private void startSDK(CallbackContext callbackContext) {

        if (hasPermisssion()) {
            PluginResult r = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(r);
        } else {
            PermissionHelper.requestPermissions(this, 0, permissions);
        }
    }

    private void setConfigration(CallbackContext callbackContext) {

        try {

            chirp = new ChirpConnect(cordova.getActivity(),CHIRP_APP_KEY, CHIRP_APP_SECRET);
            ChirpError error = chirp.setConfig(CHIRP_APP_CONFIG);
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
}