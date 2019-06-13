package com.cordova.plugin;


import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

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
            String CHIRP_APP_KEY= cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier("CHIRP_APP_KEY" , "string", cordova.getActivity().getPackageName()));
            String CHIRP_APP_SECRET= cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier( "CHIRP_APP_SECRET", "string", cordova.getActivity().getPackageName()));
            String CHIRP_APP_CONFIG= cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier( "CHIRP_APP_CONFIG", "string", cordova.getActivity().getPackageName()));
            chirp.setConfig(CHIRP_APP_CONFIG);
            chirp = new ChirpConnect(cordova.getActivity(),CHIRP_APP_KEY,CHIRP_APP_SECRET );
         /*   chirp = new ChirpConnect(cordova.getActivity(),cordova.getActivity().getResources().getString(R.string.CHIRP_APP_KEY), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_SECRET) );


            chirp.setConfig(cordova.getActivity().getResources().getString(R.string.CHIRP_APP_CONFIG));
            */chirp.start(true, true);

        }
        chirp.setListener(this);

    }

    private void sendData() {
        try {
            if (chirp == null) {
                String CHIRP_APP_KEY= cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier("CHIRP_APP_KEY" , "string", cordova.getActivity().getPackageName()));
                String CHIRP_APP_SECRET= cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier( "CHIRP_APP_SECRET", "string", cordova.getActivity().getPackageName()));
                String CHIRP_APP_CONFIG= cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier( "CHIRP_APP_CONFIG", "string", cordova.getActivity().getPackageName()));
                chirp = new ChirpConnect(cordova.getActivity(), CHIRP_APP_KEY, CHIRP_APP_SECRET);
                ChirpError errorConfig = chirp.setConfig(CHIRP_APP_CONFIG);
                //Toast.makeText(cordova.getActivity(), CHIRP_APP_KEY, Toast.LENGTH_SHORT).show();
             /*   chirp = new ChirpConnect(cordova.getActivity(), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_KEY), cordova.getActivity().getResources().getString(R.string.CHIRP_APP_SECRET));
                ChirpError errorConfig = chirp.setConfig(cordova.getActivity().getResources().getString(R.string.CHIRP_APP_CONFIG));
                */
             chirp.start(true, true);
                if (errorConfig.getCode() != 0) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Code", errorConfig.getCode());
                    jsonObject.put("Message", errorConfig.getMessage());
                    context.error(jsonObject);
                    return;
                }
            }
            Toast.makeText(cordova.getActivity(),dataToSend, Toast.LENGTH_SHORT).show();
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
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data=new String(bytes);
                Toast.makeText(cordova.getActivity(),"onReceived"+data, Toast.LENGTH_SHORT).show();
                cordovaWebView.loadUrl("javascript:onReceived('"+data+"','"+i+"');");
            }
        });
    }

    @Override
    public void onReceiving(int i) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(cordova.getActivity(),"onReceiving"+i, Toast.LENGTH_SHORT).show();
                cordovaWebView.loadUrl("javascript:onReceiving('"+i+"');");
            }
        });
    }

    @Override
    public void onSending(@NotNull byte[] bytes, int i) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                
                String data=new String(bytes);
                Toast.makeText(cordova.getActivity(),"onSending"+data, Toast.LENGTH_SHORT).show();
                cordovaWebView.loadUrl("javascript:onSending('"+data+"','"+i+"');");
            }
        });

    }

    @Override
    public void onSent(@NotNull byte[] bytes, int i) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data=new String(bytes);
                Toast.makeText(cordova.getActivity(),"onSent"+data, Toast.LENGTH_SHORT).show();
                cordovaWebView.loadUrl("javascript:onSent('"+data+"','"+i+"');");
            }
        });

    }

    @Override
    public void onStateChanged(int i, int i1) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(cordova.getActivity(),"onStateChanged "+i, Toast.LENGTH_SHORT).show();
                cordovaWebView.loadUrl("javascript:onStateChanged('"+i+"','"+i1+"');");
            }
        });

    }

    @Override
    public void onSystemVolumeChanged(float v, float v1) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(cordova.getActivity(),"onSystemVolumeChanged "+v, Toast.LENGTH_SHORT).show();
                cordovaWebView.loadUrl("javascript:onSystemVolumeChanged('"+v+"','"+v1+"');");
            }
        });

        
    }
}