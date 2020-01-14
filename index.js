import { NativeModules, DeviceEventEmitter } from 'react-native';

const { RNPlayGamesAuth, RNPlayGamesPlayer, RNPlayGamesAchievement, RNPlayGamesLeaderboard } = NativeModules;

RNPlayGamesAuth.onAuthStateChanged = (callback) => {
    return DeviceEventEmitter.addListener(RNPlayGamesAuth.AUTH_STATE_CHANGE_EVENT, isSignedIn => {
        callback(isSignedIn)
    });
} 

export { RNPlayGamesAuth, RNPlayGamesPlayer, RNPlayGamesAchievement, RNPlayGamesLeaderboard };