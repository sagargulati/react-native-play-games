package com.sagargulati.rnplaygames;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Games.GamesOptions;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import com.sagargulati.rnplaygames.util.Helpers;

/**
 * Authentication module.
 *
 * @author Sagar Gulati
 * @version 0.0.1-beta (0.0.1-beta/6:54 PM Tuesday, January 14, 2020)
 */
public class RNPlayGamesAuth extends ReactContextBaseJavaModule {
    private static final String TAG = "RNPlayGames";
    private final static int RQC_SIGNIN_UI = 1013;

    // Promises
    private Promise mSignInWithUIPromise;

    // To be exposed to JS
    public final static String AUTH_STATE_CHANGE_EVENT = "AUTH_STATE_CHANGE_EVENT";
    public static final String AUTH_STATE_CHANGED_EVENT_NAME = "rnplaygamesauthstate";


    public RNPlayGamesAuth(ReactApplicationContext reactContext) {
        super(reactContext);
        ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                super.onActivityResult(activity, requestCode, resultCode, data);

                // check if the user signed out UI.
                if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
                    Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), false);
                }

                // SignIn UI result
                switch (requestCode) {
                    case RQC_SIGNIN_UI:
                        handleSignInActivityResults(requestCode, resultCode, data);
                        break;
                }
            }
        };
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    /**
     * Attempts to sign in the user silently.
     * If it fails, then will prompt user with sign-in UI.
     * @param triggerUI whether or not to trigger the interactive UI if silent sign in fails.
     * @param promise
     */
    @ReactMethod
    public void signInPlayerInBackground(final boolean triggerUI, final Promise promise) {
        if (!this.isSignedIn()) {
            this.getSignInClient().silentSignIn().addOnCompleteListener(getCurrentActivity(),
                    new OnCompleteListener<GoogleSignInAccount>() {
                        @Override
                        public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                            if (task.isSuccessful()) {
                                Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), true);
                                Helpers.resolvePromise(promise);
                            } else if (triggerUI){
                                Log.d(TAG, "Failed to sign in silently, trying UI.");
                                // Player will need to sign-in explicitly via UI
                                signInPlayerWithUI(promise);
                            } else {
                                Helpers.rejectPromise(promise, new Exception("Sign in failed."));
                            }
                        }
                    });
        } else {
            Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), true);
            Helpers.resolvePromise(promise);
        }
    }

    /**
     * Displays the interactive sign in UI to the user.
     * @param promise
     */
    @ReactMethod
    public void signInPlayerWithUI(final Promise promise) {
        if (!this.isSignedIn()) {
            mSignInWithUIPromise = promise;
            getCurrentActivity().startActivityForResult(this.startSignInIntent(), RQC_SIGNIN_UI);
        } else {
            Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), true);
            Helpers.resolvePromise(promise);
        }
    }

    @ReactMethod
    public void signOutPlayer(final Promise promise) {
        if (this.isSignedIn()) {
            this.getSignInClient().signOut().addOnCompleteListener(getCurrentActivity(),
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), false);
                                Helpers.resolvePromise(promise);
                            } else {
                                Helpers.rejectPromise(promise, new Exception("Sign out failed."));
                            }
                        }
                    });
        } else {
            Helpers.resolvePromise(promise);
        }
    }

    private GoogleSignInAccount getSignedInUser() {
        return GoogleSignIn.getLastSignedInAccount(getReactApplicationContext());
    }

    /**
     * Verifies whether or not the user is signed in.
     * @return whether or  not a user is signed in.
     */
    public boolean isSignedIn() {
        return getSignedInUser() != null;
    }

    private Intent startSignInIntent() {
//        GoogleSignInClient signInClient = GoogleSignIn.getClient(getReactApplicationContext(),
//                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        // get sign in client with special scope for saved games (feature coming soon)
        return this.getSignInClient().getSignInIntent();
    }


    /**
     *  Sign in client with necessary drive scope for use of saved games.
     * @return sign in client
     */
    private GoogleSignInClient getSignInClient() {
        // Build Sign in options with SCOPE_APP_FOLDER google drive scope.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
//                .requestScopes(new Scope(Scopes.GAMES))
                .requestScopes(new Scope(Scopes.GAMES), new Scope(Scopes.EMAIL), new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.PROFILE))
//                .requestScopes(Games.SCOPE_GAMES_LITE)
//                .requestScopes(Drive.SCOPE_APPFOLDER)
//                .requestEmail()
                .build();
        return GoogleSignIn.getClient(getCurrentActivity(), googleSignInOptions);
    }

    private void handleSignInActivityResults(int requestCode, int resultCode, Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

        if (result.isSuccess()) {
            Helpers.sendAuthStateChangedEvent(getReactApplicationContext(), true);
            Helpers.resolvePromise(mSignInWithUIPromise);
        } else {
            Log.e(TAG, "Sign In failed via UI with result code: " + resultCode);
            Helpers.rejectPromise(mSignInWithUIPromise, new Exception("Sign In Failed."));
        }
    }

    @Override
    public String getName() {
        return "RNPlayGamesAuth";
    }


    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(AUTH_STATE_CHANGE_EVENT, AUTH_STATE_CHANGED_EVENT_NAME);
        return constants;
    }
}
