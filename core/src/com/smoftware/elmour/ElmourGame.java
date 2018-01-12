package com.smoftware.elmour;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.smoftware.elmour.screens.CreditScreen;
import com.smoftware.elmour.screens.CutSceneScreen;
import com.smoftware.elmour.screens.GameOverScreen;
import com.smoftware.elmour.screens.LoadGameScreen;
import com.smoftware.elmour.screens.MainGameScreen;
import com.smoftware.elmour.screens.MainMenuScreen;
import com.smoftware.elmour.screens.NewGameScreen;
import com.smoftware.elmour.screens.SplashScreen;
import com.smoftware.elmour.screens.StartScreen;


/* NOTES
	Add LEVEL_GATES object layer. On this layer put objects to be used as gates
	that the player will pass through to determine which layer to render the
	player immediately after. The objects need to be named the same name as the tile layer
	they represent. The new rendering order will take effect as soon as the player collides
	with the gate.

	Add NPC_BOUNDS layer. On this layer put an object around the NPC to limit
	where the NPC can walk. The object needs to be named the same name as the
	NPC start point in the SPAWN_LAYER with "_BOUNDS" appended to it.
*/

public class ElmourGame extends Game {

	public static int V_WIDTH;
	public static int V_HEIGHT;
	public static float ASPECT_RATIO;

	public static boolean isAndroid() { return Gdx.app.getType() == Application.ApplicationType.Android; }

	private static SplashScreen splashScreen;
	private static StartScreen startScreen;
	private static MainGameScreen _mainGameScreen;
	private static MainMenuScreen _mainMenuScreen;
	private static LoadGameScreen _loadGameScreen;
	private static NewGameScreen _newGameScreen;
	private static GameOverScreen _gameOverScreen;
	private static CutSceneScreen _cutSceneScreen;
	private static CreditScreen _creditScreen;

	public static enum ScreenType{
		SplashScreen,
		StartScreen,
		MainMenu,
		MainGame,
		LoadGame,
		NewGame,
		GameOver,
		WatchIntro,
		Credits
	}

	public Screen getScreenType(ScreenType screenType){
		switch(screenType){
			case SplashScreen:
				return splashScreen;
			case StartScreen:
				return startScreen;
			case MainMenu:
				return _mainMenuScreen;
			case MainGame:
				return _mainGameScreen;
			case LoadGame:
				return _loadGameScreen;
			case NewGame:
				return _newGameScreen;
			case GameOver:
				return _gameOverScreen;
			case WatchIntro:
				return _cutSceneScreen;
			case Credits:
				return _creditScreen;
			default:
				return _mainMenuScreen;
		}

	}

	@Override
	public void create(){
		Utility.initializeElmourUISkin();

		// set width and height to fill screen
		if (isAndroid()) {
			V_WIDTH = 512;
			V_HEIGHT = 320;
		}
		else {
			V_WIDTH = Gdx.graphics.getWidth();
			V_HEIGHT = Gdx.graphics.getHeight();
		}

		ASPECT_RATIO = (float) Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		V_HEIGHT = (int)((float)V_WIDTH / ASPECT_RATIO);

		Gdx.app.log("tag", String.format("screen width = %d, height = %d", V_WIDTH, V_HEIGHT));

		splashScreen = new SplashScreen(this);
		startScreen = new StartScreen(this);
		_mainGameScreen = new MainGameScreen(this);
		_mainMenuScreen = new MainMenuScreen(this);
		_loadGameScreen = new LoadGameScreen(this);
		_newGameScreen = new NewGameScreen(this);
		_gameOverScreen = new GameOverScreen(this);
		_cutSceneScreen = new CutSceneScreen(this);
		_creditScreen = new CreditScreen(this);

		//setScreen(splashScreen);
		setScreen(splashScreen);

		/*//////////////////////////////////////
		//For testing to go right to game screen //srm
		FileHandle file = ProfileManager.getInstance().getProfileFile("steve");
		if (file != null) {
			ProfileManager.getInstance().setCurrentProfile("steve");
			//LoadGameScreen.this.notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE);
			setScreen(getScreenType(ElmourGame.ScreenType.MainGame));
		}
		////////////////*/
	}

	@Override
	public void dispose(){
		splashScreen.dispose();;
		startScreen.dispose();
		_mainGameScreen.dispose();
		_mainMenuScreen.dispose();
		_loadGameScreen.dispose();
		_newGameScreen.dispose();
		_gameOverScreen.dispose();
		_creditScreen.dispose();
	}

}
