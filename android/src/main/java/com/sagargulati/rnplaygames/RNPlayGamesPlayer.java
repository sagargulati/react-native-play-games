package com.sagargulati.rnplaygames;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerLevelInfo;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.sagargulati.rnplaygames.util.Helpers;

/**
 * Players module.
 *
 * @author Sagar Gulati
 * @version 0.0.1-beta (0.0.1-beta/6:54 PM Tuesday, January 14, 2020)
 */
public class RNPlayGamesPlayer extends ReactContextBaseJavaModule {
    private static final String TAG = "RNPlayGames";

    public RNPlayGamesPlayer(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void getCurrentPlayerInfo(final Promise promise) {
        final WritableMap playerInfoMap = Helpers.getReturnObject();
        final PlayersClient playersClient = this.getPlayersClient();

        if (playersClient == null) {
            Helpers.rejectPromiseWithAuthenticationRequired(promise);
            return;
        }
        playersClient.getCurrentPlayer().addOnSuccessListener(new OnSuccessListener<Player, PlayerLevelInfo>() {
            @Override
            public void onSuccess(Player player, PlayerLevelInfo playerlevelinfo) {
                playerInfoMap.putString("displayName", player.getDisplayName());
                playerInfoMap.putString("playerId", player.getPlayerId());
                playerInfoMap.putString("levelInfo", playerlevelinfo.getLevelInfo());
                playerInfoMap.putDouble("lastTimePlayed", player.getLastPlayedWithTimestamp());
                playerInfoMap.putString("title", player.getTitle());
                promise.resolve(playerInfoMap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.rejectPromise(promise, e);
            }
        });
    }


    /**
     * Attempts to retrieve an instance of PlayersClient.
     * @return PlayersClient or null if the user is not signed in.
     */
    private PlayersClient getPlayersClient() {
        if (GoogleSignIn.getLastSignedInAccount(getReactApplicationContext()) != null) {
            return Games.getPlayersClient(getReactApplicationContext(),
                    GoogleSignIn.getLastSignedInAccount(getReactApplicationContext()));
        }
        return null;
    }

    @Override
    public String getName() {
        return "RNPlayGamesPlayer";
    }
}
