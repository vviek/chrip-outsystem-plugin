package com.cordova.plugin;
// The native Toast API

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
// Cordova-required packages
import com.utlis.ChripConstants;

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

            chirp = new ChirpConnect(cordova.getActivity(), ChripConstants.CHIRP_APP_KEY, ChripConstants.CHIRP_APP_SECRET);
            ChirpError error = chirp.setConfig(ChripConstants.CHIRP_APP_CONFIG);
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