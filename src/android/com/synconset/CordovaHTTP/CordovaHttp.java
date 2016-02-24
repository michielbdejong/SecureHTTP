/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import org.apache.cordova.CallbackContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;

import java.util.Iterator;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

public abstract class CordovaHttp {
    protected static final String TAG = "CordovaHTTP";
    protected static final String CHARSET = "UTF-8";

    private enum Mode { NORMAL, PINNING, ALL, TOFU };
    private static Mode mode = Mode.NORMAL;

    private String urlString;
    private Map<?, ?> params;
    private Map<String, String> headers;
    private CallbackContext callbackContext;

    public CordovaHttp(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
        this.urlString = urlString;
        this.params = params;
        this.headers = headers;
        this.callbackContext = callbackContext;
    }

    public static void enableSSLPinning(boolean enable) {
        HttpRequest.debug("setting enableSSLPinning " + (enable ? "Mode.PINNING" : "Mode.NORMAL"));
        mode = (enable ? Mode.PINNING : Mode.NORMAL);
    }

    public static void acceptAllCerts(boolean accept) {
        HttpRequest.debug("setting acceptAllCerts " + (accept ? "Mode.ALL" : "Mode.NORMAL"));
        mode = (accept ? Mode.ALL : Mode.NORMAL);
    }

    public static void acceptOnFirstUse(boolean accept) {
        HttpRequest.debug("setting acceptOnFirstUse " + (accept ? "Mode.TOFU" : "Mode.NORMAL"));
        mode = (accept ? Mode.TOFU : Mode.NORMAL);
    }

    protected String getUrlString() {
        return this.urlString;
    }

    protected Map<?, ?> getParams() {
        return this.params;
    }

    protected Map<String, String> getHeaders() {
        return this.headers;
    }

    protected CallbackContext getCallbackContext() {
        return this.callbackContext;
    }

    protected HttpRequest setupSecurity(HttpRequest request) {
        HttpRequest.debug("setting up security");
        switch(mode) {
            case TOFU:
                request.trustAllCerts();
                request.trustHostOnFirstUse();
                HttpRequest.debug("mode is TOFU");
                break;
            case PINNING:
                request.pinToCerts();
                HttpRequest.debug("mode is PINNING");
                break;
            case ALL:
                request.trustAllCerts();
                request.trustAllHosts();
                HttpRequest.debug("mode is ALL");
                break;
            case NORMAL:
            default:
                HttpRequest.debug("mode is NORMAL");
                // ...
        };
        return request;
    }

    protected void respondWithError(int status, String msg) {
        try {
            JSONObject response = new JSONObject();
            response.put("status", status);
            response.put("error", msg);
            this.callbackContext.error(response);
        } catch (JSONException e) {
            this.callbackContext.error(msg);
        }
    }

    protected void respondWithError(String msg) {
        this.respondWithError(500, msg);
    }
}
