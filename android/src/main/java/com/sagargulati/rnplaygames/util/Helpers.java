package com.sagargulati.rnplaygames.util;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.sagargulati.rnplaygames.RNPlayGamesAuth;

public class Helpers {
    private final static String MESSAGE_AUTHENTICATION_REQUIRED = "Authentication Required.";

    public static WritableMap getReturnObject() {
        return Arguments.createMap();
    }

    public static void resolvePromise(final Promise promise) {
        if (promise == null) return;
        promise.resolve(null);
    }

    public static void resolvePromise(final Promise promise, final WritableMap message) {
        if (promise == null) return;
        promise.resolve(message);
    }

    public static void rejectPromise(final Promise promise, final Exception throwable) {
        if (promise == null) return;
        promise.reject(throwable);
    }

    public static void rejectPromiseWithAuthenticationRequired(final Promise promise) {
        if (promise == null) return;
        promise.reject(new Exception(MESSAGE_AUTHENTICATION_REQUIRED));
    }

    public static void sendAuthStateChangedEvent(final ReactApplicationContext context, final boolean isSignedIn) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(RNPlayGamesAuth.AUTH_STATE_CHANGED_EVENT_NAME, isSignedIn);
    }
}