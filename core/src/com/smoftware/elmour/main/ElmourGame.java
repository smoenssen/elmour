package com.smoftware.elmour.main;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.UI.huds.StatusUI;
import com.smoftware.elmour.battle.BattleState;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.screens.BattleScreen;
import com.smoftware.elmour.screens.CreditScreen;
import com.smoftware.elmour.screens.chapters.Chapter2;
import com.smoftware.elmour.screens.chapters.Chapter1;
import com.smoftware.elmour.screens.ChapterSplashScreen;
import com.smoftware.elmour.screens.chapters.Chapter3;
import com.smoftware.elmour.screens.quests.CloningQuest;
import com.smoftware.elmour.screens.quests.Quest1;
import com.smoftware.elmour.screens.GameOverScreen;
import com.smoftware.elmour.screens.MainGameScreen;
import com.smoftware.elmour.screens.SplashScreen;
import com.smoftware.elmour.screens.StartScreen;


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
	//public static boolean isAndroid() { return true; }
	public static boolean DEV_MODE = true;

	private Array<EntityFactory.EntityName> partyList;

	public BattleState battleState = null;
	public StatusUI statusUI;

	private static SplashScreen splashScreen;
	private static StartScreen startScreen;
	private static MainGameScreen _mainGameScreen;
	private static GameOverScreen _gameOverScreen;
	private static CreditScreen _creditScreen;
	private static BattleScreen battleScreen;
	private static Chapter1 Chapter1;
	private static Chapter2 Chapter2;
	private static Chapter3 Chapter3;

	private static Quest1 quest1;
	private static CloningQuest cloningQuest;

	public static enum ScreenType{
		BattleScreen,
		SplashScreen,
		StartScreen,
		MainGame,
		GameOver,
		WatchIntro,
		ChapterSplashScreen,
		Chapter1Screen,
		Chapter2Screen,
		Chapter3Screen,
		Quest1Screen,

		CloningQuestScreen,

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
			case Chapter1Screen:
				return Chapter1;
			case Chapter2Screen:
				return Chapter2;
			case Chapter3Screen:
				return Chapter3;
			case Quest1Screen:
				return quest1;
			case CloningQuestScreen:
				return cloningQuest;
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

		/*
		Application.LOG_NONE: mutes all logging.
		Application.LOG_DEBUG: logs all messages.
		Application.LOG_ERROR: logs only error messages.
		Application.LOG_INFO: logs error and normal messages
		*/
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		// set width and height to fill screen
		if (isAndroid()) {
			V_WIDTH = 512;
			V_HEIGHT = 320;
		}
		else {
			V_WIDTH = Gdx.graphics.getWidth();
			V_HEIGHT = Gdx.graphics.getHeight();
		}

		Utility.parseQuestXMLFile("RPGGame/maps/Game/Quests/Quests.graphml");

		float width = (float) Gdx.graphics.getWidth();
		float height = (float) Gdx.graphics.getHeight();
		ASPECT_RATIO = (float) Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		V_HEIGHT = (int)((float)V_WIDTH / ASPECT_RATIO);

		Gdx.app.log("tag", String.format("screen width = %d, height = %d", V_WIDTH, V_HEIGHT));

		statusUI = new StatusUI();
		battleState = new BattleState(this);

		splashScreen = new SplashScreen(this);
		startScreen = new StartScreen(this);
		_mainGameScreen = new MainGameScreen(this, true);
		_gameOverScreen = new GameOverScreen(this);
		_creditScreen = new CreditScreen(this);
		battleScreen = new BattleScreen(this);
		Chapter1 = new Chapter1(this, _mainGameScreen._playerHUD);
		Chapter2 = new Chapter2(this, _mainGameScreen._playerHUD);
		Chapter3 = new Chapter3(this, _mainGameScreen._playerHUD);

		quest1 = new Quest1(this, _mainGameScreen._playerHUD);
		cloningQuest = new CloningQuest(this, _mainGameScreen._playerHUD);

		partyList = new Array<>();

		Utility.parseAllConversationXMLFiles("RPGGame/maps/Game/Text");
		//Utility.parseConversationXMLFile("n18", "RPGGame/maps/Game/Text/Dialog/Chapter_2_P2.graphml", "RPGGame/maps/Game/Text/Dialog/Chapter_2_P2.json");

		// Uncomment the following line for cut scenes. This is needed for previous save profile info.
		//ProfileManager.getInstance().setCurrentProfile(ProfileManager.SAVED_GAME_PROFILE);
		//setScreen(Chapter1);
		//setScreen(Chapter2);

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
		/*
		Json json = new Json();
		Array<KeyItem> array = new Array<>();
		KeyItem keyItem = new KeyItem();
		keyItem.id = KeyItem.ID.TEDDY_BEAR;
		keyItem.category = KeyItem.Category.QUEST;
		keyItem.name = "Teddy Bear";
		keyItem.summary = "Used for reassurance";
		keyItem.imagePath = "graphics/tedd_bear.png";
		array.add(keyItem);
		String output = json.prettyPrint(json.toJson(array));
		Gdx.app.log("TAG", output);
		*/
		//QuestGraphTest questGraphTest = new QuestGraphTest();
		//questGraphTest.main(null);
	}

	public void setChapterScreen(final int chapterNum) {

		ChapterSplashScreen chapterSplashScreen = null;

		switch (chapterNum) {
			case 1:
				chapterSplashScreen = new ChapterSplashScreen(this, ScreenType.Chapter1Screen, chapterNum, "A Rude Awakening");
				break;
			case 2:
				chapterSplashScreen = new ChapterSplashScreen(this, ScreenType.Chapter2Screen, chapterNum, "The Slow Rush for Supplies");
				break;
			case 3:
				chapterSplashScreen = new ChapterSplashScreen(this, ScreenType.Chapter3Screen, chapterNum, "First Casualty of War");
				break;
		}

		setScreen(chapterSplashScreen);
	}

	public void addPartyMember(EntityFactory.EntityName name) {
		partyList.add(name);
		ProfileManager.getInstance().setProperty("partyList", partyList);
	}

	public void removePartyMember(EntityFactory.EntityName name) {
		partyList.removeValue(name, true);
		ProfileManager.getInstance().setProperty("partyList", partyList);
	}

	public Array<EntityFactory.EntityName> getPartyList() {
		return partyList;
	}

	public void setPartyList(Array<EntityFactory.EntityName> list) {
		partyList.clear();
		for (EntityFactory.EntityName name : list) {
			addPartyMember(name);
		}
	}

	public void clearPartyList() {
		partyList.clear();
		ProfileManager.getInstance().setProperty("partyList", partyList);
	}

	@Override
	public void dispose(){
		splashScreen.dispose();;
		startScreen.dispose();
		_mainGameScreen.dispose();
		_gameOverScreen.dispose();
		_creditScreen.dispose();
		battleScreen.dispose();
		Chapter1.dispose();
		Chapter2.dispose();
		Chapter3.dispose();
		quest1.dispose();
		cloningQuest.dispose();
	}

}
