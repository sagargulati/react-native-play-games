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
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import com.sagargulati.rnplaygames.util.Helpers;

/**
 * Leaderboards module.
 *
 * @author Sagar Gulati
 * @version 0.0.1-beta (0.0.1-beta/6:54 PM Tuesday, January 14, 2020)
 */
public class RNPlayGamesLeaderboard extends ReactContextBaseJavaModule  {
    private Promise mLeaderboardUIPromise;
    private final static int RQC_SINGLE_LEADERBOARD_UI = 1014;
    private final static int RQC_ALL_LEADERBOARDS_UI = 1015;

    // To be exposed to JS
    private static final String TIME_SPAN_DAILY = "TIME_SPAN_DAILY";
    private static final String TIME_SPAN_WEEKLY = "TIME_SPAN_WEEKLY";
    private static final String TIME_SPAN_ALL_TIME = "TIME_SPAN_ALL_TIME";
    private static final String COLLECTION_PUBLIC = "COLLECTION_PUBLIC";

    public RNPlayGamesLeaderboard(ReactApplicationContext reactContext) {
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
                    case RQC_ALL_LEADERBOARDS_UI:
                        handleAllLeaderboardsActivityResults(requestCode, resultCode, data);
                        break;
                    case RQC_SINGLE_LEADERBOARD_UI:
                        handleSingleLeaderboardActivityResults(requestCode, resultCode, data);
                        break;
                }
            }
        };
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    /**
     * Displays the specified leaderboard overlay ui activity.
     * @param boardId
     * @param promise
     */
    @ReactMethod
    public void showLeaderboardUI(final String boardId, final Promise promise) {
        mLeaderboardUIPromise = promise;
        singleLeaderboardUIHelper(boardId, LeaderboardVariant.TIME_SPAN_ALL_TIME);
    }

    /**
     * Displays the specified leaderboard UI with the specified timespan filter.
     * @param boardId
     * @param timeSpan
     * @param promise
     */
    @ReactMethod
    public void showLeaderboardUIFilteredTimeSpan(final String boardId, final int timeSpan, final Promise promise) {
        mLeaderboardUIPromise = promise;
        singleLeaderboardUIHelper(boardId, timeSpan);
    }

    /**
     * Displays all the leaderboards in the overlay ui activity.
     * @param promise
     */
    @ReactMethod
    public void showAllLeaderboardsUI(final Promise promise) {
        mLeaderboardUIPromise = promise;
        Task<Intent> allLeaderboardsIntent = this.getAllLeaderboardsIntent();

        if (allLeaderboardsIntent == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(mLeaderboardUIPromise);
            return;
        }

        allLeaderboardsIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                getCurrentActivity().startActivityForResult(intent, RQC_ALL_LEADERBOARDS_UI);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(mLeaderboardUIPromise, e);
            }
        });
    }


    /**
     * Helper method.
     * On success, promise is resolved with object containing isNewBest key
     * @param boardId
     * @param timeSpan
     */
    private void singleLeaderboardUIHelper(final String boardId, final int timeSpan) {
        Task<Intent> leaderboardIntent = this.getLeaderboardIntent(boardId, timeSpan);

        if (leaderboardIntent == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(mLeaderboardUIPromise);
            return;
        }

        leaderboardIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                getCurrentActivity().startActivityForResult(intent, RQC_SINGLE_LEADERBOARD_UI);
            }
        });

        leaderboardIntent.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(mLeaderboardUIPromise, e);
            }
        });
    }

    /**
     * Submits the new score to the specified leaderhoard.
     * @param boardId id of the leaderboard
     * @param score
     * @param scoreTag
     * @param promise
     */
    @ReactMethod
    public void submitScore(final String boardId, final int score, String scoreTag, final Promise promise) {
        try {
            Task<ScoreSubmissionData> scoreSubmissionDataTask;
            final WritableMap scoreResultsMap = Helpers.getReturnObject();

            LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
            if (leaderboardsClient == null) {
                Helpers.rejectPromiseWithAuthenticationRequired(promise);
                return;
            }

            if (scoreTag == null) {
                scoreSubmissionDataTask = leaderboardsClient.submitScoreImmediate(boardId, (long)score);
            } else {
                scoreSubmissionDataTask = leaderboardsClient.submitScoreImmediate(boardId,
                        (long)score, scoreTag);
            }
            scoreSubmissionDataTask.addOnSuccessListener(new OnSuccessListener<ScoreSubmissionData>() {
                @Override
                public void onSuccess(ScoreSubmissionData scoreSubmissionData) {
                    // resolve promise with whether or not the score is a new best.
                    scoreResultsMap.putBoolean("isNewBest",
                            scoreSubmissionData.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME)
                                    .newBest);
                    promise.resolve(scoreResultsMap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Helpers.rejectPromise(promise,e);
                }
            });

        } catch(Exception e) {
            Helpers.rejectPromise(promise, e);
        }
    }

    private Task<Intent> getLeaderboardIntent(final String boardId, final int timeSpan) {
        LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
        if (leaderboardsClient != null) {
            return leaderboardsClient.getLeaderboardIntent(boardId, timeSpan);
        }
        return null;
    }

    private Task<Intent> getAllLeaderboardsIntent() {
        LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
        if (leaderboardsClient != null) {
            return leaderboardsClient.getAllLeaderboardsIntent();
        }
        return null;
    }

    /**
     * Attempts to retrieve an instance of LeaderboardsClient.
     * @return LeaderboardsClient or null if the user is not signed in.
     */
    private LeaderboardsClient getLeaderboardsClient() {
        if (GoogleSignIn.getLastSignedInAccount(getReactApplicationContext()) != null) {
            return Games.getLeaderboardsClient(getReactApplicationContext(),
                    GoogleSignIn.getLastSignedInAccount(getReactApplicationContext()));
        }
        return null;
    }

    private void handleSingleLeaderboardActivityResults(final int requestCode, final int resultCode, final Intent data) {
        Helpers.resolvePromise(mLeaderboardUIPromise);
    }

    private void handleAllLeaderboardsActivityResults(final int requestCode, final int resultCode, final Intent data) {
        Helpers.resolvePromise(mLeaderboardUIPromise);
    }


    @Override
    public String getName() {
        return "RNPlayGamesLeaderboard";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(TIME_SPAN_DAILY, LeaderboardVariant.TIME_SPAN_DAILY);
        constants.put(TIME_SPAN_WEEKLY, LeaderboardVariant.TIME_SPAN_WEEKLY);
        constants.put(TIME_SPAN_ALL_TIME, LeaderboardVariant.TIME_SPAN_ALL_TIME);
        constants.put(COLLECTION_PUBLIC, LeaderboardVariant.COLLECTION_PUBLIC);
        return constants;
    }
}
