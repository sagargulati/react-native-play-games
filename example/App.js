import React, { Component } from 'react';
import { RNPlayGamesAuth, RNPlayGamesPlayer, RNPlayGamesLeaderboard, RNPlayGamesAchievement } from 'react-native-play-games';
import {
	StyleSheet,
	Text,
	Button,
	View
} from 'react-native';


export default class App extends Component {
	authListener = null;

	constructor(props) {
		super(props);
		this.state = {
			isSignedIn: false,
			playerInfo: ''
		}

		this.authListener = RNPlayGamesAuth.onAuthStateChanged(signedIn => {
			this.setState({
				isSignedIn: signedIn,
				playerInfo: ''
			});

			if (signedIn) {
				RNPlayGamesPlayer.getCurrentPlayerInfo().then(res => {
					this.setState({
						playerInfo: JSON.stringify(res)
					});
				}).catch(err => {
					console.log("Couldn't retrieve player's info: " + err);
				});
			}
		});
	}

	componentDidMount() {
		// Google recommends that you attempt to sign in the player silently
		RNPlayGamesAuth.signInPlayerInBackground(false).then(res => {
			alert("Welcome back!");
		}).catch(err => {
			console.log("Background sign in error: " + err);
		});
	}

	componentWillUnmount() {
		// Remove listener
		if (this.authListener != null) {
			this.authListener.remove();
		}
	}

	_generateButtons = () => {
		buttons = [
			{
				name: "All Leaderboards",
				onPress: this._onAllLeaderboardsPressed
			},
			{
				name: "Single Leaderboard",
				onPress: this._onSingleLeaderboardPressed
			},
			{
				name: "All Achievements",
				onPress: this._onAllAchievementsPressed
			}
		]

		return (
			<View>
				{
					buttons.map((val, index) => {
						return (
							<View key={index} style={styles.btnContainer}>
								<Button color="green" title={val.name} onPress={() => val.onPress()} />
							</View>
						);
					})
				}
			</View>
		);
	}
	
	_onLoginPressed = () => {
		// sign in player in the background if possible
		// if fails, will prompt user to login via sign in UI.
		RNPlayGamesAuth.signInPlayerWithUI().then(res => {
			alert("You've been signed in. Welcome!");
		}).catch(err => {
			alert("Sign In Failed");
		});
	}

	_onLogoutPressed = () => {
		RNPlayGamesAuth.signOutPlayer().then(() => {
			alert("Goodbye");
		}).catch(err => {
			console.log("Sign out error: " + err);
		});
	}

	_onAllLeaderboardsPressed = () => {
		RNPlayGamesLeaderboard.showAllLeaderboardsUI().then(() => {})
		.catch(() => {});
	}

	_onSingleLeaderboardPressed = () => {
		RNPlayGamesLeaderboard.showLeaderboardUI('CgkI1f2B8a4XEAIQAg').then(() => {})
		.catch(() => {});
	}

	_onAllAchievementsPressed = () => {
		RNPlayGamesAchievement.showAchievementsUI().then(() => {})
		.catch(() => {});
	}

	render() {
		return (
			<View style={styles.container}>
				<Text style={styles.welcome}>
					React Native Google Play Games Services
        		</Text>
				<Text style={[styles.welcome, {marginBottom: 20}]}>
				(react-native-gpgs)
				</Text>
				
				{
					this.state.isSignedIn ? (
						<Button color="grey" title="Sign Out" onPress={()=> this._onLogoutPressed()}/>
					) : (
						<Button color="green" title="Sign In" onPress={()=> this._onLoginPressed()}/>
					)
				}

				{
					this.state.isSignedIn ? (
						<View>
							<View style={{ marginVertical: 10 }}>
								<Text style={{ fontWeight: 'bold', }}>Player Information:</Text>
								<Text>{this.state.playerInfo}</Text>
							</View>

							<View style={{ marginVertical: 10 }}>
								{this._generateButtons()}
							</View>

							<View>
								<Text style={{fontWeight: 'bold', alignSelf: 'center'}}>*** Includes these and many more features... ***</Text>
							</View>
						</View>
					): (null)
				}

			</View>
		);
	}
}

const styles = StyleSheet.create({
	container: {
		paddingHorizontal: 20,
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
		backgroundColor: '#F5FCFF',
	},
	welcome: {
		fontSize: 20,
		textAlign: 'center',
		margin: 10,
		fontWeight: 'bold'
	},
	btnContainer: {
		marginVertical: 10
	}
});
