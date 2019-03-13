package com.smoftware.elmour;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.smoftware.elmour.UI.StatusUI;
import com.smoftware.elmour.battle.BattleState;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.screens.CreditScreen;
import com.smoftware.elmour.screens.CutSceneChapter2;
import com.smoftware.elmour.screens.CutSceneChapter1;
import com.smoftware.elmour.screens.GameOverScreen;
import com.smoftware.elmour.screens.MainGameScreen;
import com.smoftware.elmour.screens.SplashScreen;
import com.smoftware.elmour.screens.StartScreen;
import com.smoftware.elmour.tests.QuestGraphTest;


/* NOTES
	srm - issue with Desktop project not running with Android Studio 3.0 using Gradle-4.1 and android-gradle-plugin:3.0.1
	https://stackoverflow.com/questions/46975883/error2-0-plugin-with-id-jetty-not-found

	Add NPC_BOUNDS layer. On this layer put an object around the NPC to limit
	where the NPC can walk. The object needs to be named the same name as the
	NPC start point in the SPAWN_LAYER with "_BOUNDS" appended to it.
*/

public class ElmourGame extends Game {

	public static int V_WIDTH;
	public static int V_HEIGHT;
	public static float ASPECT_RATIO;

	public static boolean isAndroid() { return Gdx.app.getType() == Application.ApplicationType.Android; }
	public static boolean DEV_MODE = true;

	public BattleState battleState = null;
	public StatusUI statusUI;

	private static SplashScreen splashScreen;
	private static StartScreen startScreen;
	private static MainGameScreen _mainGameScreen;
	private static GameOverScreen _gameOverScreen;
	private static CreditScreen _creditScreen;
	private static BattleScreen battleScreen;
	private static CutSceneChapter1 cutSceneChapter1;
	private static CutSceneChapter2 cutSceneChapter2;

	public static enum ScreenType{
		BattleScreen,
		SplashScreen,
		StartScreen,
		MainGame,
		GameOver,
		WatchIntro,
		Credits
	}

	public Screen getScreenType(ScreenType screenType){
		switch(screenType){
			case BattleScreen:
				return battleScreen;
			case SplashScreen:
				return splashScreen;
			case StartScreen:
				return startScreen;
			case MainGame:
				return _mainGameScreen;
			case GameOver:
				return _gameOverScreen;
			case Credits:
				return _creditScreen;
			default:
				return startScreen;
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

		statusUI = new StatusUI();
		battleState = new BattleState(this);

		splashScreen = new SplashScreen(this);
		startScreen = new StartScreen(this);
		_mainGameScreen = new MainGameScreen(this);
		_gameOverScreen = new GameOverScreen(this);
		_creditScreen = new CreditScreen(this);
		battleScreen = new BattleScreen(this);
		cutSceneChapter1 = new CutSceneChapter1(this);
		cutSceneChapter2 = new CutSceneChapter2(this);

		//Utility.parseConversationXMLFiles("n0");
		//setScreen(cutSceneChapter1);
		setScreen(cutSceneChapter2);

		//setScreen(splashScreen);

		/*//////////////////////////////////////
		//For testing to go right to game screen //srm
		FileHandle file = ProfileManager.getInstance().getProfileFile("steve");
		if (file != null) {
			ProfileManager.getInstance().setCurrentProfile("steve");
			//LoadGameScreen.this.notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE);
			setScreen(getScreenType(ElmourGame.ScreenType.MainGame));
		}
		////////////////*/

		QuestGraphTest questGraphTest = new QuestGraphTest();
		questGraphTest.main(null);
	}

	@Override
	public void dispose(){
		splashScreen.dispose();;
		startScreen.dispose();
		_mainGameScreen.dispose();
		_gameOverScreen.dispose();
		_creditScreen.dispose();
		battleScreen.dispose();
		cutSceneChapter1.dispose();
		cutSceneChapter2.dispose();
	}

}
