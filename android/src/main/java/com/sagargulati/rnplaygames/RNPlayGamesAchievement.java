package com.sagargulati.rnplaygames;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.sagargulati.rnplaygames.util.Helpers;


/**
 * Achievements module.
 *
 * @author Sagar Gulati
 * @version 0.0.1-beta (0.0.1-beta/6:54 PM Tuesday, January 14, 2020)
 */
public class RNPlayGamesAchievement extends ReactContextBaseJavaModule {
    private Promise mAchievementsUIPromise;
    private final static int RQC_ACHIEVEMENTS_UI = 1016;


    public RNPlayGamesAchievement(ReactApplicationContext reactContext) {
        super(reactContext);
        ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                super.onActivityResult(activity, requestCode, resultCode, data);

                // check if the user signed out from the UI.
                if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
                    Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), false);
                }

                switch (requestCode) {
                    case RQC_ACHIEVEMENTS_UI:
                        handleAchievementsActivityResults(requestCode, resultCode, data);
                        break;
                }
            }
        };
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    /**
     * Displays the achievements ui overlay activity.
     * @param promise
     */
    @ReactMethod
    public void showAchievementsUI(final Promise promise) {
        final Task<Intent> achievementsIntent = this.getAchievementsIntent();
        mAchievementsUIPromise = promise;

        if (achievementsIntent == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(mAchievementsUIPromise);
            return;
        }

        achievementsIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                getCurrentActivity().startActivityForResult(intent, RQC_ACHIEVEMENTS_UI);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(mAchievementsUIPromise, e);
            }
        });
    }

    /**
     * Increments the achievement specified by the id.
     * @param id
     * @param numSteps
     * @param promise
     */
    @ReactMethod
    public void incrementAchievement(final String id, final int numSteps, final Promise promise) {
        AchievementsClient achievementsClient = getAchievementsClient();
        if (achievementsClient == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(promise);
            return;
        }

        achievementsClient.incrementImmediate(id, numSteps).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

                // resolves promise with whether or not the achievement was unlocked.
                WritableMap returnObject = Helpers.getReturnObject();
                returnObject.putBoolean("isUnlocked", aBoolean);
                Helpers.resolvePromise(promise, returnObject);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(promise, e);
            }
        });
    }

    /**
     * Unlock the achievement specified by the id.
     * @param id
     * @param promise
     */
    @ReactMethod
    public void unlockAchievement(final String id, final Promise promise) {
        AchievementsClient achievementsClient = getAchievementsClient();
        if (achievementsClient == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(promise);
            return;
        }

        achievementsClient.unlockImmediate(id).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Helpers.resolvePromise(promise);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(promise, e);
            }
        });
    }

    /**
     * Reviews a hidden achievement.
     * @param id
     * @param promise
     */
    @ReactMethod
    public void revealHiddenAchievement(final String id, final Promise promise) {
        AchievementsClient achievementsClient = getAchievementsClient();
        if (achievementsClient == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(promise);
            return;
        }

        achievementsClient.revealImmediate(id).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Helpers.resolvePromise(promise);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(promise, e);
            }
        });
    }

    private Task<Intent> getAchievementsIntent() {
        AchievementsClient achievementsClient = getAchievementsClient();
        if (achievementsClient != null) {
            return achievementsClient.getAchievementsIntent();
        }
        return null;
    }

    /**
     * Attempts to retrieve an instance of AchievementsClient.
     * @return AchievementsClient or null if the user is not signed in.
     */
    private AchievementsClient getAchievementsClient() {
        if (GoogleSignIn.getLastSignedInAccount(getReactApplicationContext()) != null) {

            final GoogleSignInAccount lastSignedInAccount =
                    GoogleSignIn.getLastSignedInAccount(getReactApplicationContext());
            return Games.getAchievementsClient(getReactApplicationContext(), lastSignedInAccount);
        }
        return null;
    }

    private void handleAchievementsActivityResults(final int requestCode, final int resultCode, final Intent data) {
        Helpers.resolvePromise(mAchievementsUIPromise);
    }

    @Override
    public String getName() {
        return "RNPlayGamesAchievement";
    }
}
