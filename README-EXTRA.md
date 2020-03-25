# React Native Google Play Games Services Docs
React Native Google Play Games Services (react-native-play-games) is a wrapper around the  native Google Play Games Services  API for Android.
### NOTE: This library in for react native and only supports the android platform.

## Pre-installation requirements
This documentation assumes that you've already completed all the requirements of the setup guide found [here](https://developers.google.com/games/services/console/enabling).

It is important that you follow and complete all the listed requirements or the library will not work. 

### After you've completed the guide:

 1. Create a value resource file named ***id.xml*** in the ***res/values*** folder
 2. Paste your application id in the created file. See [example](https://github.com/playgameservices/android-basic-samples/blob/master/ButtonClicker/src/main/res/values/ids.xml).
```
<string name="app_id">YOUR_APP_ID</string>
```

3. Paste the following in your within the ``<application>`` tag in the AndroidManifest.xml file
```diff
...
 <application>
 ...
+ <meta-data android:name="com.google.android.gms.games.APP_ID" android:value="@string/app_id"/>

+ <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version"/>
 </application>
 ...
```
 **Note:**  if you open android studio at this point, you may get an error saying that it cannot resolve @integer/google_play_services_version. This error should be resolved after you have completed the installation and linking steps below.
 
## Installation
1. Install the package from npm
``` [sudo] npm install react-native-play-games --save``` OR yarn ``` [sudo] yarn install react-native-play-games```

2. Link the library
``` react-native link react-native-play-games```

3. Edit top-level build.gradle (Project module)
```diff 
allprojects {
	...
	repositories {
		... 
		// Add these lines of code to your android/build.gradle file
+		maven {  
+			url "https://maven.google.com"  
+		}
		...
	}
	...
}
``` 


## API Overview

|       Module      |                                          Method                                         |          Return         | Since |
|:-----------------:|:---------------------------------------------------------------------------------------:|:-----------------------:|:-----:|
| RNPlayGamesAuth        | [onAuthStateChanged](#onauthstatechangedcallback)                                       | \*event handler\*       | 1.0.0 |
| RNPlayGamesAuth        | [signInPlayerInBackground](#signinplayerinbackgroundtriggerui)                          | Promise<null>           | 1.0.0 |
| RNPlayGamesAuth        | [signInPlayerWithUI](#signinplayerwithui)                                               | Promise<null>           | 1.0.0 |
| RNPlayGamesAuth        | [signOutPlayer](#signoutplayer)                                                         | Promise<null>           | 1.0.0 |
| RNPlayGamesPlayer      | [getCurrentPlayerInfo](#getcurrentplayerinfo)                                           | Promise(playerInfoObj)  | 1.0.0 |
| RNPlayGamesLeaderboard | [showAllLeaderboardsUI](#showallleaderboardsui)                                         | Promise<null>           | 1.0.0 |
| RNPlayGamesLeaderboard | [showLeaderboardUI](#showleaderboarduiboardid)                                          | Promise<null>           | 1.0.0 |
| RNPlayGamesLeaderboard | [showLeaderboardUIFilteredTimeSpan](#showleaderboarduifilteredtimespanboardid-timespan) | Promise<null>           | 1.0.0 |
| RNPlayGamesLeaderboard | [submitScore](#submitscoreboardid-score-scoretag)                                       | Promise({ isNewBest })  | 1.0.0 |
| RNPlayGamesAchievement | [showAchievementsUI](#showachievementsui)                                               | Promise<null>           | 1.0.0 |
| RNPlayGamesAchievement | [incrementAchievement](#incrementachievementid-numsteps)                                | Promise({ isUnlocked }) | 1.0.0 |
| RNPlayGamesAchievement | [unlockAchievement](#unlockachievementid)                                               | Promise<null>           | 1.0.0 |
| RNPlayGamesAchievement | [revealHiddenAchievement](#revealhiddenachievementid)                                   | Promise<null>           | 1.0.0 |

## Auth Module (RNPlayGamesAuth)
To use methods in the auth module, import RNPlayGamesAuth:

```js 
import { RNPlayGamesAuth } from 'react-native-play-games' 
```

### onAuthStateChanged(callback)
This method triggers the callback function whenever the user's authentication state has changed (signed in or signed out).

**Important:** It is important to implement this method because the sign in and sign out methods do not return the user's authentication state.

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| callback | function(isSignedIn) | - | Yes | **Param: isSignedIn (boolean)** - whether or not the user is signed in. |  

**Return**
\*event handler\*

**Example**
```js
...

componentWillMount() {
	this.authListener = RNPlayGamesAuth.onAuthStateChanged(isSignedIn => {
		this.setState({
			isSignedIn:  isSignedIn
		});
	})
}

componentWillUnmount() {
	// Remove listener
	if (this.authListener != null) {
		this.authListener.remove();
	}
}
...
```

### signInPlayerInBackground(triggerUI)
Attempts to silently sign in the player/user in the background. If triggerUI is true, then if the background sign in failed, then the signin UI will be presented to the user.

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| triggerUI | boolean | - | Yes | Whether or not to display the sign-in UI in the event that the silent sign in failed. |  

**Return**

    Promise<null>
Returns a promise that is fulfilled after the sign-in flow is is complete or dismissed by the user.
*Triggers callback provided to the [onAuthStateChanged](#onauthstatechangedcallback) method.*

**Example**
```js
RNPlayGamesAuth.signInPlayerInBackground(false).catch(err => {
	console.log(err);
})
```


### signInPlayerWithUI()
Displays the interactive sign in UI to the user.

**Return**

    Promise<null>
Returns a promise that is fullfilled after the sign-in flow is is complete or dismissed by the user.
*Triggers callback provided to the [onAuthStateChanged(callback)](#onauthstatechangedcallback) method.*

**Example**
```js
RNPlayGamesAuth.signInPlayerWithUI().catch(err => {
	console.log(err);
})
```

### signOutPlayer()
Attemps to sign out the current player/user.

**Return**

```js 
Promise<null>
```
Returns a promise that is fullfilled after the sign-out process is complete.
*Triggers callback provided to the [onAuthStateChanged(callback)](#onAuthStateChonauthstatechangedcallbackanged) method.*

**Example**
```js
RNPlayGamesAuth.signOutPlayer().catch(err => {
    console.log(err);
})
```

## Player Module (RNPlayGamesPlayer)
To use methods in the auth module, import RNPlayGamesPlayer:

```js 
import { RNPlayGamesPlayer } from 'react-native-play-games' 
```

### getCurrentPlayerInfo()
This method returns information about the user that is currently login in.

**Return**
```js
Promise(playerInfoObj)
```

| Name | Type | Description |
|--|--|--|
| playerInfoObj | object | ```{ title, lastTimePlayed, playerId, displayName, levelInfo }``` |





## Leaderboard Module (RNPlayGamesLeaderboard)
To use methods in the auth module, import RNPlayGamesLeaderboard:
```js
import { RNPlayGamesLeaderboard } from 'react-native-play-games'
```

### showAllLeaderboardsUI()
Displays a leaderboard UI overlay containing all the leaderboards associated with the application/game.

**Return**
```js 
Promise<null>
```
Returns a promise that is fullfilled when the user exits the UI.
*Triggers callback provided to the [onAuthStateChanged(callback)](#onauthstatechangedcallback) method if the user signs out in the UI.*

**Example**
```js
RNPlayGamesLeaderboard.showAllLeaderboardsUI().catch(err => {
	alert("Failed to load UI.");
	console.log(err);
})
```

### showLeaderboardUI(boardId)
Displays the leaderboards UI overlay to the user.

**Parameters**

| Name | Type | Default | Required | Description |
|---|--|---|---|---|
| boardId | string | - | Yes | The id of the specific leaderboard |  

**Return**
```js 
Promise<null>
```
Returns a promise that is fullfilled when the user exits the UI.
*Triggers callback provided to the [onAuthStateChanged(callback)](#onauthstatechangedcallback) method if the user signs out in the UI.*

**Example**
```js
RNPlayGamesLeaderboard.showLeaderboardUI('board-id-here').catch(err => {
	alert("Failed to load UI.");
	console.log(err);
})
```


### showLeaderboardUIFilteredTimeSpan(boardId, timeSpan)
Displays the leaderboards UI overlay to the user.

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| boardId | string | - | Yes | The id of the specific leaderboard |
| timeSpan | RNPlayGamesConstants | - | Yes | Use one of the provided constants: TIME_SPAN_DAILY, TIME_SPAN_WEEKLY, TIME_SPAN_ALL_TIME |

**Return**
```js 
Promise<null>
```
Returns a promise that is fullfilled when the user exits the UI.
*Triggers callback provided to the [onAuthStateChanged(callback)](#onauthstatechangedcallback) method if the user signs out in the UI.*

**Example**
```js
RNPlayGamesLeaderboard.showLeaderboardUIFilteredTimeSpan('board-id-here', RNPlayGamesConstants.TIME_SPAN_WEEKLY).catch(err => {
	console.log(err);
})
```


### submitScore(boardId, score, scoreTag)
Submits the specified score for the current user to the specified leaderboard (boardId).

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| boardId | string | - | Yes | The id of the specific leaderboard |
| score | integer | - | Yes | The score
| scoreTag | string | - | no (nullable) | Pass 'null' to ignore. See google's documentation for more details on scoreTags. |

**Return**
```js
Promise({ isNewBest })
```
| Name | Type | Description |
|--|--|--|
| isNewBest | boolean | whether or not the score is a new best |

**Example**
```js
RNPlayGamesLeaderboard.submitScore('board-id-here', 200, null).then(mess => {
	if(mess.isNewBest) {
		alert("You set a new best score.");
	}
}).catch(err => {
	console.log(err);
})
```


## Achievements Module (RNPlayGamesAchievement)
To use methods in the achievements module, import RNPlayGamesAchievement:
```js
import { RNPlayGamesAchievement } from 'react-native-play-games'
```
### showAchievementsUI()
Displays all the achievements associated with the application/game.

**Return**
```js 
Promise<null>
```

**Example**
```js
RNPlayGamesAchievement.showAchievementsUI().catch(err => {
	console.log(err);
})
```


### incrementAchievement(id, numSteps)
Increments the specific achievement by the specified number of steps for the current player/user.

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| id | string | - | Yes | The ID of the achievement to increment. |
| numSteps | integer | - | Yes | The number of steps to increment by. Must be greater than 0. |
**Return**
```js
Promise({ isUnlocked })
```
| Name | Type | Description |
|---|---|---|
| isUnlocked | boolean | indicates whether the achievement is now unlocked  |

**Example**
```js
RNPlayGamesAchievement.incrementAchievement('achievement-id-here', 2).then(mess => {
	if(mess.isUnlocked) {
	    alert("New Achievement unlocked!");
	}
}).catch(err => {
	console.log(err);
})
```

### unlockAchievement(id)
Unlocks an achievement for the currently signed in player. If the achievement is hidden this will reveal it to the player.

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| id | string | - | Yes | The ID of the achievement to unlock. |

**Return**
```js 
Promise<null>
```

**Example**
```js
RNPlayGamesAchievement.unlockAchievement('achievement-id-here').catch(err => {
	console.log(err);
})
```


### revealHiddenAchievement(id)
Reveals a hidden achievement to the currently signed-in player. If the achievement has already been unlocked, this will have no effect.

**Parameters**

| Name | Type | Default | Required | Description |
|---|---|---|---|---|
| id | string | - | Yes | The ID of the hidden achievement to reveal. |

**Return**
```js 
Promise<null>
```

**Example**
```js
RNPlayGamesAchievement.revealHiddenAchievement('achievement-id-here').catch(err => {
	console.log(err);
})
```