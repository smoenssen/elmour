package com.smoftware.elmour.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.smoftware.elmour.UI.dialog.Conversation;
import com.smoftware.elmour.UI.dialog.ConversationChoice;
import com.smoftware.elmour.UI.dialog.ConversationGraph;
import com.smoftware.elmour.UI.dialog.ConversationGraphObserver;
import com.smoftware.elmour.UI.dialog.ConversationNode;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.maps.Armory;
import com.smoftware.elmour.maps.Barren_Room;
import com.smoftware.elmour.maps.Castle;
import com.smoftware.elmour.maps.Compass;
import com.smoftware.elmour.maps.Courtyard;
import com.smoftware.elmour.maps.DesertTemple;
import com.smoftware.elmour.maps.Elmour;
import com.smoftware.elmour.maps.GrassBattle;
import com.smoftware.elmour.maps.GrassTemple;
import com.smoftware.elmour.maps.Inn;
import com.smoftware.elmour.maps.Jerbadia;
import com.smoftware.elmour.maps.LostForest;
import com.smoftware.elmour.maps.LostForestA;
import com.smoftware.elmour.maps.LostForestB;
import com.smoftware.elmour.maps.LostForestC;
import com.smoftware.elmour.maps.M6_Cave;
import com.smoftware.elmour.maps.M6_Cave_A;
import com.smoftware.elmour.maps.M6_Cave_B;
import com.smoftware.elmour.maps.Map1;
import com.smoftware.elmour.maps.Map10;
import com.smoftware.elmour.maps.Map11;
import com.smoftware.elmour.maps.Map14;
import com.smoftware.elmour.maps.Map2;
import com.smoftware.elmour.maps.Map3;
import com.smoftware.elmour.maps.Map4;
import com.smoftware.elmour.maps.Map5;
import com.smoftware.elmour.maps.Map6;
import com.smoftware.elmour.maps.Map7;
import com.smoftware.elmour.maps.Map8;
import com.smoftware.elmour.maps.Map9;
import com.smoftware.elmour.maps.Portal_Room;
import com.smoftware.elmour.maps.Shnarfulapogus;
import com.smoftware.elmour.maps.T1DOOR4;
import com.smoftware.elmour.maps.TarpingTown;
import com.smoftware.elmour.maps.WeaponsRoom;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.quest.QuestDependency;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestTask;
import com.smoftware.elmour.quest.QuestTaskDependency;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public final class Utility {
	public static final AssetManager _assetManager = new AssetManager();
	private static final String TAG = Utility.class.getSimpleName();
	private static InternalFileHandleResolver _filePathResolver =  new InternalFileHandleResolver();

	private final static String STATUSUI_TEXTURE_ATLAS_PATH = "skins/statusui.atlas";
	private final static String STATUSUI_SKIN_PATH = "skins/statusui.json";
	private final static String ITEMS_TEXTURE_ATLAS_PATH = "skins/items.atlas";
	private final static String ITEMS_SKIN_PATH = "skins/items.json";

	public static TextureAtlas STATUSUI_TEXTUREATLAS = new TextureAtlas(STATUSUI_TEXTURE_ATLAS_PATH);
	public static TextureAtlas ITEMS_TEXTUREATLAS = new TextureAtlas(ITEMS_TEXTURE_ATLAS_PATH);
	public static Skin STATUSUI_SKIN = new Skin(Gdx.files.internal(STATUSUI_SKIN_PATH), STATUSUI_TEXTUREATLAS);

	private final static String ELMOUR_TEXTURE_ATLAS_PATH = "skins/elmour_ui.atlas";
	private final static String ELMOUR_SKIN_PATH = "skins/elmour_ui.json";
	public static TextureAtlas ELMOUR_UI_TEXTUREATLAS = new TextureAtlas(ELMOUR_TEXTURE_ATLAS_PATH);
	public static Skin ELMOUR_UI_SKIN;
	public static int myFontSmallSize;
	public static int myFontVerySmallSize;
	public static int myFontSuperSmallSize;

	private static BitmapFont fontSign;
	private static BitmapFont fontSmall;
	private static BitmapFont fontVerySmall;
	private static BitmapFont fontSuperSmall;
	private static BitmapFont fontRed;
	private static BitmapFont fontGraySmall;
	private static BitmapFont fontGrayVerySmall;
	private static BitmapFont fontChapterTitle;
	private static BitmapFont fontChapterTitleSubtext;

	public static void preLoadFonts() {
		FileHandleResolver resolver = new InternalFileHandleResolver();
		_assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		_assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		//NOTE!!! if elmour_ui.json is generated again, then need to replace instances of default-font with myFont:
		//	font: myFont

		int myFontSize = 18;

		// LARGE TEXT
		_assetManager.load("myFont.ttf", BitmapFont.class,
				createFont(myFontSize, Color.DARK_GRAY, Color.LIGHT_GRAY, 1, 1));

		//NOTE: apply the filter if softer font is desired
		//fontSign.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		// SMALL TEXT
		if (ElmourGame.isAndroid()) {
			myFontSmallSize = 15;
		}
		else{
			myFontSmallSize = 18;
		}

		_assetManager.load("myFontSmall.ttf", BitmapFont.class,
				createFont(myFontSmallSize, Color.DARK_GRAY, Color.LIGHT_GRAY, 1, 1));

		// VERY SMALL TEXT
		if (ElmourGame.isAndroid()) {
			myFontVerySmallSize = 12;
		}
		else{
			myFontVerySmallSize = 15;
		}

		_assetManager.load("myFontVerySmall.ttf", BitmapFont.class,
				createFont(myFontVerySmallSize, Color.DARK_GRAY, Color.LIGHT_GRAY, 1, 1));

		// SUPER SMALL TEXT
		if (ElmourGame.isAndroid()) {
			myFontSuperSmallSize = 7;
		}
		else{
			myFontSuperSmallSize = 9;
		}

		_assetManager.load("myFontSuperSmall.ttf", BitmapFont.class,
				createFont(myFontSuperSmallSize, Color.DARK_GRAY, Color.LIGHT_GRAY, 1, 1));

		// RED TEXT
		if (ElmourGame.isAndroid()) {
			myFontSize = 16;
		}
		else{
			myFontSize = 20;
		}

		_assetManager.load("myFontRed.ttf", BitmapFont.class,
				createFont(myFontSize, Color.RED, Color.BLACK, 1, 1));

		// GRAY TEXT SMALL
		_assetManager.load("myFontGraySmall.ttf", BitmapFont.class,
				createFont(myFontSmallSize, new Color(0x909090ff), Color.BLACK, 1, 1));

		// GRAY TEXT VERY SMALL
		_assetManager.load("myFontGrayVerySmall.ttf", BitmapFont.class,
				createFont(myFontVerySmallSize, new Color(0x909090ff), Color.WHITE, 1, 1));

		// CHAPTER TITLE TEXT
		if (ElmourGame.isAndroid()) {
			myFontSize = 48;
		}
		else{
			myFontSize = 64;
		}

		_assetManager.load("myFontChapterTitle.ttf", BitmapFont.class,
				createFont(myFontSize, Color.WHITE, Color.BLACK, 1, 1));

		// CHAPTER TITLE SUBTEXT
		if (ElmourGame.isAndroid()) {
			myFontSize = 32;
		}
		else{
			myFontSize = 36;
		}

		_assetManager.load("myFontChapterTitleSubtext.ttf", BitmapFont.class,
				createFont(myFontSize, Color.GRAY, Color.BLACK, 1, 1));
	}

	private static FreetypeFontLoader.FreeTypeFontLoaderParameter createFont(int size, Color color, Color shadowColor, int shadowOffsetX, int shaodowOffsetY) {
		FreetypeFontLoader.FreeTypeFontLoaderParameter font = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		font.fontFileName = "fonts/9_px.ttf";
		font.fontParameters.size = size;
		font.fontParameters.color = color;
		font.fontParameters.shadowColor = shadowColor;
		font.fontParameters.shadowOffsetX = shadowOffsetX;
		font.fontParameters.shadowOffsetY = shaodowOffsetY;
		return font;
	}

	public static void setFonts() {
		fontSign = _assetManager.get("myFont.ttf", BitmapFont.class);
		fontSmall = _assetManager.get("myFontSmall.ttf", BitmapFont.class);
		fontVerySmall = _assetManager.get("myFontVerySmall.ttf", BitmapFont.class);
		fontSuperSmall = _assetManager.get("myFontSuperSmall.ttf", BitmapFont.class);
		fontRed = _assetManager.get("myFontRed.ttf", BitmapFont.class);
		fontGraySmall = _assetManager.get("myFontGraySmall.ttf", BitmapFont.class);
		fontGrayVerySmall = _assetManager.get("myFontGrayVerySmall.ttf", BitmapFont.class);
		fontChapterTitle = _assetManager.get("myFontChapterTitle.ttf", BitmapFont.class);
		fontChapterTitleSubtext = _assetManager.get("myFontChapterTitleSubtext.ttf", BitmapFont.class);
	}

	public static void initializeElmourUISkin() {
		//Need to initialize skin before using it because of customized TT myFont that is used in .json
		ELMOUR_UI_SKIN = new Skin();

		ELMOUR_UI_SKIN.add("myFont", fontSign, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontSmall", fontSmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontVerySmall", fontVerySmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontSuperSmall", fontSuperSmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontRed", fontRed, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontGraySmall", fontGraySmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontGrayVerySmall", fontGrayVerySmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontChapterTitle", fontChapterTitle, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontChapterTitleSubtext", fontChapterTitleSubtext, BitmapFont.class);
		ELMOUR_UI_SKIN.addRegions(new TextureAtlas(Gdx.files.internal(ELMOUR_TEXTURE_ATLAS_PATH)));
		ELMOUR_UI_SKIN.load(Gdx.files.internal(ELMOUR_SKIN_PATH));

		ELMOUR_UI_SKIN.add("cursor", new Texture("graphics/black_rectangle.png"));
	}

	public static void preLoadMaps() {
		// just pre-load large maps here
		loadMapAsset(LostForest.mapPath);
/*
		loadMapAsset(Armory.mapPath);
		loadMapAsset(Barren_Room.mapPath);
		loadMapAsset(Castle.mapPath);
		loadMapAsset(Compass.mapPath);
		loadMapAsset(Courtyard.mapPath);
		loadMapAsset(DesertTemple.mapPath);
		loadMapAsset(Elmour.mapPath);
		loadMapAsset(GrassBattle.mapPath);
		loadMapAsset(GrassTemple.mapPath);
		loadMapAsset(Inn.mapPath);
		loadMapAsset(Jerbadia.mapPath);
		loadMapAsset(LostForest.mapPath);
		loadMapAsset(LostForestA.mapPath);
		loadMapAsset(LostForestB.mapPath);
		loadMapAsset(LostForestC.mapPath);
		loadMapAsset(M6_Cave.mapPath);
		loadMapAsset(M6_Cave_A.mapPath);
		loadMapAsset(M6_Cave_B.mapPath);
		loadMapAsset(Map1.mapPath);
		loadMapAsset(Map2.mapPath);
		loadMapAsset(Map3.mapPath);
		loadMapAsset(Map4.mapPath);
		loadMapAsset(Map5.mapPath);
		loadMapAsset(Map6.mapPath);
		loadMapAsset(Map7.mapPath);
		loadMapAsset(Map8.mapPath);
		loadMapAsset(Map9.mapPath);
		loadMapAsset(Map10.mapPath);
		loadMapAsset(Map11.mapPath);
		loadMapAsset(Map14.mapPath);
		loadMapAsset(Portal_Room.mapPath);
		loadMapAsset(Shnarfulapogus.mapPath);
		loadMapAsset(T1DOOR4.mapPath);
		loadMapAsset(TarpingTown.mapPath);
		loadMapAsset(WeaponsRoom.mapPath);
*/
	}

	public static void preLoadSounds() {
		for (AudioObserver.AudioTypeEvent sound : AudioObserver.AudioTypeEvent.values()) {
			loadSoundAsset(sound.getValue());
		}
	}

	public static void unloadAsset(String assetFilenamePath){
	// once the asset manager is done loading
	if( _assetManager.isLoaded(assetFilenamePath) ){
		_assetManager.unload(assetFilenamePath);
		} else {
			Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: " + assetFilenamePath );
		}
	}

	// srm: see https://github.com/libgdx/libgdx/wiki/Managing-your-assets
	public static float loadCompleted(){
		return _assetManager.getProgress();
	}

	public static int numberAssetsQueued(){
		return _assetManager.getQueuedAssets();
	}

   	public static boolean updateAssetLoading(){
		return _assetManager.update();
	}

	public static void finishLoadingAssets() { _assetManager.finishLoading(); }

	public static boolean isAssetLoaded(String fileName){
	   return _assetManager.isLoaded(fileName);

	}

	public static void loadMapAsset(String mapFilenamePath){
		if( mapFilenamePath == null || mapFilenamePath.isEmpty() ){
		   return;
		}

		if( _assetManager.isLoaded(mapFilenamePath) ){
			return;
		}

	   //load asset
		if( _filePathResolver.resolve(mapFilenamePath).exists() ){
			_assetManager.setLoader(TiledMap.class, new TmxMapLoader(_filePathResolver));
			_assetManager.load(mapFilenamePath, TiledMap.class);
			//Until we add loading screen, just block until we load the map
			_assetManager.finishLoadingAsset(mapFilenamePath);
			//Gdx.app.debug(TAG, "Map loaded!: " + mapFilenamePath);
		}
		else{
			Gdx.app.debug(TAG, "Map doesn't exist!: " + mapFilenamePath );
		}
	}


	public static TiledMap getMapAsset(String mapFilenamePath){
		TiledMap map = null;

		// once the asset manager is done loading
		if( _assetManager.isLoaded(mapFilenamePath) ){
			map = _assetManager.get(mapFilenamePath,TiledMap.class);
		} else {
			Gdx.app.debug(TAG, "Map is not loaded: " + mapFilenamePath );
		}

		return map;
	}

	public static void loadSoundAsset(String soundFilenamePath){
		if( soundFilenamePath == null || soundFilenamePath.isEmpty() ){
			return;
		}

		if( _assetManager.isLoaded(soundFilenamePath) ){
			return;
		}

		//load asset
		if( _filePathResolver.resolve(soundFilenamePath).exists() ){
			_assetManager.setLoader(Sound.class, new SoundLoader(_filePathResolver));
			_assetManager.load(soundFilenamePath, Sound.class);
			//Until we add loading screen, just block until we load the map
			//_assetManager.finishLoadingAsset(soundFilenamePath);
			Gdx.app.debug(TAG, "Sound loaded!: " + soundFilenamePath);
		}
		else{
			Gdx.app.debug(TAG, "Sound doesn't exist!: " + soundFilenamePath );
		}
	}


	public static Sound getSoundAsset(String soundFilenamePath){
		Sound sound = null;

		// once the asset manager is done loading
		if( _assetManager.isLoaded(soundFilenamePath) ){
			sound = _assetManager.get(soundFilenamePath,Sound.class);
		} else {
			Gdx.app.debug(TAG, "Sound is not loaded: " + soundFilenamePath );
		}

		return sound;
	}

	public static void loadMusicAsset(String musicFilenamePath){
		if( musicFilenamePath == null || musicFilenamePath.isEmpty() ){
			return;
		}

		if( _assetManager.isLoaded(musicFilenamePath) ){
			return;
		}

		//load asset
		if( _filePathResolver.resolve(musicFilenamePath).exists() ){
			_assetManager.setLoader(Music.class, new MusicLoader(_filePathResolver));
			_assetManager.load(musicFilenamePath, Music.class);
			//Until we add loading screen, just block until we load the map
			_assetManager.finishLoadingAsset(musicFilenamePath);
			Gdx.app.debug(TAG, "Music loaded!: " + musicFilenamePath);
		}
		else{
			Gdx.app.debug(TAG, "Music doesn't exist!: " + musicFilenamePath );
		}
	}


	public static Music getMusicAsset(String musicFilenamePath){
		Music music = null;

		// once the asset manager is done loading
		if( _assetManager.isLoaded(musicFilenamePath) ){
			music = _assetManager.get(musicFilenamePath,Music.class);
		} else {
			Gdx.app.debug(TAG, "Music is not loaded: " + musicFilenamePath );
		}

		return music;
	}


	public static void loadTextureAsset(String textureFilenamePath){
		if( textureFilenamePath == null || textureFilenamePath.isEmpty() ){
			return;
		}

		if( _assetManager.isLoaded(textureFilenamePath) ){
			return;
		}

		//load asset
		if( _filePathResolver.resolve(textureFilenamePath).exists() ){
			_assetManager.setLoader(Texture.class, new TextureLoader(_filePathResolver));
			_assetManager.load(textureFilenamePath, Texture.class);
			//Until we add loading screen, just block until we load the map
			_assetManager.finishLoadingAsset(textureFilenamePath);
		}
		else{
			Gdx.app.debug(TAG, "Texture doesn't exist!: " + textureFilenamePath );
		}
	}

	public static Texture getTextureAsset(String textureFilenamePath){
		Texture texture = null;

		// once the asset manager is done loading
		if( _assetManager.isLoaded(textureFilenamePath) ){
			texture = _assetManager.get(textureFilenamePath,Texture.class);
		} else {
			Gdx.app.debug(TAG, "Texture is not loaded: " + textureFilenamePath );
		}

		return texture;
	}

	public static void parseQuestXMLFile(String inputFileName) {

		Hashtable<String, Conversation> conversations = new Hashtable<>();
		Hashtable<String, ArrayList<ConversationChoice>> associatedChoices = new Hashtable<>();

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = null;
		try {
			xml_element = xml.parse(Gdx.files.internal(inputFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		XmlReader.Element graph = xml_element.getChildByName("graph");

		// build node graph
		Hashtable<String, QuestGraph> questGraphs = buildQuestNodeGraph(graph);

		for (Map.Entry<String, QuestGraph> entry : questGraphs.entrySet()) {
			String questID = entry.getKey();
			QuestGraph questGraph = entry.getValue();

			String outputFileName = "RPGGame/maps/Game/Quests/" + questID + ".json";
			FileHandle outFile = Gdx.files.local(outputFileName);
			outFile.writeString(questGraph.toJson(), false);
		}

		// build quest dependency graph
		Json json = new Json();
		Hashtable<String, Array<QuestDependency>> questDependencies = buildQuestDependencyGraph(graph);
		FileHandle outFile = Gdx.files.local("RPGGame/maps/Game/Quests/QuestDependencies.json");
		outFile.writeString(json.prettyPrint(questDependencies), false);
	}

	private static Hashtable<String, QuestGraph> buildQuestNodeGraph(XmlReader.Element graph) {
		Hashtable<String, QuestGraph> nodes = new Hashtable<>();

		// id
		Iterator iterator_node = graph.getChildrenByName("node").iterator();
		while(iterator_node.hasNext()){
			XmlReader.Element node_element = (XmlReader.Element)iterator_node.next();
			QuestGraph questGraph = getQuestGraph(graph, node_element);
			nodes.put(questGraph.getQuestID(), questGraph);
		}

		return nodes;
	}

	private static QuestGraph getQuestGraph(XmlReader.Element graph, XmlReader.Element node_element) {
		QuestGraph questGraph = getQuestData(node_element);

		Hashtable<String, QuestTask> questTasks = getQuestTasks(graph, node_element);

		questGraph.setTasks(questTasks);

		setTaskDependencies(graph, questGraph, questTasks);

		questGraph.setQuestStatus(QuestGraph.QuestStatus.NOT_STARTED);
		return questGraph;
	}

	private static QuestGraph getQuestData(XmlReader.Element node_element) {
		QuestGraph questGraph = new QuestGraph();
		questGraph.yedNodeId = node_element.getAttribute("id");

		// Quest data
		Iterator iterator_data = node_element.getChildrenByName("data").iterator();
		while(iterator_data.hasNext()) {
			XmlReader.Element data_element = (XmlReader.Element)iterator_data.next();
			String key = data_element.getAttribute("key");

			if (key.equals("d4")) {
				// Data URL is quest title
				questGraph.setQuestTitle(data_element.getText());
			}
			else if (key.equals("d5")) {
				// Data Description is in the form <Minimum chapter>;<Quest giver>;<Gold>;<XP>
				String [] sa = data_element.getText().split(";");
				questGraph.setChapter(Integer.parseInt(sa[0]));
				questGraph.setQuestGiver(sa[1]);
				questGraph.setGoldReward(Integer.parseInt(sa[2]));
				questGraph.setXpReward(Integer.parseInt(sa[3]));
			}
			else if (key.equals("d6")) {
				XmlReader.Element ProxyAutoBoundsNode = data_element.getChildByName("y:ProxyAutoBoundsNode");
				XmlReader.Element Realizers = ProxyAutoBoundsNode.getChildByName("y:Realizers");
				XmlReader.Element GroupNode = Realizers.getChildByName("y:GroupNode");
				XmlReader.Element NodeLabel = GroupNode.getChildByName("y:NodeLabel");

				String label = NodeLabel.getText();

				if (!label.equals("Folder 1")) {
					questGraph.setQuestID(label);

					// got everything we need from this node
					break;
				}
			}
		}

		return questGraph;
	}

	private static Hashtable<String, QuestTask> getQuestTasks(XmlReader.Element graph, XmlReader.Element node_element) {
		// Quest tasks
		Hashtable<String, QuestTask> questTasks = new Hashtable<>();
		XmlReader.Element graph_element = node_element.getChildByName("graph");

		Iterator iterator_node2 = graph_element.getChildrenByName("node").iterator();

		while (iterator_node2.hasNext()) {
			QuestTask taskNode = new QuestTask();
			boolean taskHasSubQuest = false;
			XmlReader.Element node2_element = (XmlReader.Element) iterator_node2.next();
			taskNode.yedNodeId = node2_element.getAttribute("id");
			taskNode.setIsExpanded(true);

			ObjectMap<String, String> attributes = node2_element.getAttributes();
			if (attributes.get("yfiles.foldertype") != null) {
				String foldertype = node2_element.getAttribute("yfiles.foldertype");
				if (foldertype != null && foldertype.equals("group")) {
					// This task is a sub-quest
					QuestGraph subQuestGraph = getQuestGraph(graph, node2_element);

					ArrayList<QuestTask> tasks = subQuestGraph.getAllQuestTasks();
					for (QuestTask task : tasks) {
						task.setParentQuestId(subQuestGraph.getQuestID());
					}

					taskNode.addSubQuest(subQuestGraph);
					taskNode.setId(subQuestGraph.getQuestID());
					taskNode.setTaskPhrase(subQuestGraph.getQuestTitle());
					taskNode.setTargetEntity(QuestTask.QuestTaskType.QUEST.toString());	//todo: is target entity for sub-quest necessary?
					taskNode.setQuestTaskType(QuestTask.QuestTaskType.QUEST);
					taskNode.setQuestTaskStatus(QuestTask.QuestTaskStatus.NOT_STARTED);
					questTasks.put(taskNode.getId(), taskNode);
					continue;
				}
			}

			Iterator iterator_data2 = node2_element.getChildrenByName("data").iterator();

			while (iterator_data2.hasNext()) {
				XmlReader.Element data_element = (XmlReader.Element) iterator_data2.next();
				String key = data_element.getAttribute("key");

				if (key.equals("d4")) {
					// Data URL is quest task phrase followed by an optional # delimited hint,
                    // or in the case of a TASK_COMPLETE it is the KeyItem ID
					String url = data_element.getText();
					String [] sa = url.split("#");
					taskNode.setTaskPhrase(sa[0]);
					if (sa.length > 1) {
						taskNode.setHint(sa[1]);
					}
				} else if (key.equals("d5")) {
					// Data Description is in the form <Target Entity>;<ConversationType>;<Post task ConversationType>;<Target Number>
					String[] sa = data_element.getText().split(";", 4);
					taskNode.setTargetEntity(sa[0]);
					taskNode.setConversationType(EntityConfig.ConversationType.valueOf(sa[1]));
					if (sa.length > 2 && !sa[2].equals(""))
						taskNode.setPostTaskConversationType(EntityConfig.ConversationType.valueOf(sa[2]));
					if (sa.length > 3 && !sa[3].equals(""))
						taskNode.setTargetNumber(Integer.parseInt(sa[3]));
				} else if (key.equals("d6")) {
					XmlReader.Element shapeNode = data_element.getChildByName("y:ShapeNode");
					XmlReader.Element label = shapeNode.getChildByName("y:NodeLabel");

					// task ID
					taskNode.setId(label.getText());

					// quest task type
					XmlReader.Element fill = shapeNode.getChildByName("y:Fill");
					String color = fill.getAttribute("color");

					if (color.equalsIgnoreCase("#cc99ff"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.FETCH);
					else if (color.equalsIgnoreCase("#ff0000"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.KILL);
					else if (color.equalsIgnoreCase("#c0c0c0"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.DELIVERY);
					else if (color.equalsIgnoreCase("#3366ff"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.GUARD);
					else if (color.equalsIgnoreCase("#33cccc"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.ESCORT);
					else if (color.equalsIgnoreCase("#ffcc00"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.RETURN);
					else if (color.equalsIgnoreCase("#00ff00"))
						taskNode.setQuestTaskType(QuestTask.QuestTaskType.DISCOVER);

					// check if spoiler (ellipses are spoilers)
					XmlReader.Element shape = shapeNode.getChildByName("y:Shape");
					String type = shape.getAttribute("type");
					taskNode.setIsSpoiler(type.equals("ellipse"));
				}
			}

			taskNode.setQuestTaskStatus(QuestTask.QuestTaskStatus.NOT_STARTED);
			questTasks.put(taskNode.getId(), taskNode);
			//break;
		}
		return questTasks;
	}

	private static void setTaskDependencies(XmlReader.Element graph, QuestGraph questGraph, Hashtable<String, QuestTask> questTasks) {
		// now add edge information (dependencies)
		Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
		while(iterator_edge.hasNext()){
            XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
            String source = edge_element.getAttribute("source");
            String target = edge_element.getAttribute("target");

            QuestTaskDependency qDep = new QuestTaskDependency();
            String sourceID = getTaskID(questTasks, source);
            String destinationID = getTaskID(questTasks, target);

            if (sourceID != null && destinationID != null) {
                qDep.setSourceId(sourceID);
                qDep.setDestinationId(destinationID);

                questGraph.addDependency(qDep);
            }
        }
	}

	private static String getTaskID(Hashtable<String, QuestTask> questTasks, String yedId) {
		for (Map.Entry<String, QuestTask> entry : questTasks.entrySet()) {
			String taskID = entry.getKey();
			QuestTask questTask = entry.getValue();

			if (questTask.yedNodeId.equals(yedId)) {
				return taskID;
			}
		}
		return null;
	}

	private static Hashtable<String,  Array<QuestDependency>> buildQuestDependencyGraph(XmlReader.Element graph) {
		Hashtable<String,  Array<QuestDependency>> questDependencies = new Hashtable<>();
		Hashtable<String, String> yedQuestIDMap = new Hashtable<>();

		// yEd id
		Iterator iterator_node = graph.getChildrenByName("node").iterator();
		while(iterator_node.hasNext()){
			XmlReader.Element node_element = (XmlReader.Element)iterator_node.next();
			String yedNodeId = node_element.getAttribute("id");

			// data has quest ID
			Iterator iterator_data = node_element.getChildrenByName("data").iterator();
			while(iterator_data.hasNext()) {
				XmlReader.Element data_element = (XmlReader.Element)iterator_data.next();
				String key = data_element.getAttribute("key");

				if (key.equals("d6")) {
					XmlReader.Element ProxyAutoBoundsNode = data_element.getChildByName("y:ProxyAutoBoundsNode");
					XmlReader.Element Realizers = ProxyAutoBoundsNode.getChildByName("y:Realizers");
					XmlReader.Element GroupNode = Realizers.getChildByName("y:GroupNode");
					XmlReader.Element NodeLabel = GroupNode.getChildByName("y:NodeLabel");

					String label = NodeLabel.getText();

					if (!label.equals("Folder 1")) {
						yedQuestIDMap.put(yedNodeId, label);

						// got everything we need from this node
						break;
					}
				}
			}

			// now add edge information (dependencies)
			Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
			while(iterator_edge.hasNext()){
				XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
				String source = edge_element.getAttribute("source");
				String target = edge_element.getAttribute("target");

				QuestDependency qDep = new QuestDependency();
				String sourceID = yedQuestIDMap.get(source);
				String destinationID = yedQuestIDMap.get(target);

				if (sourceID != null && destinationID != null) {
					qDep.setSourceId(sourceID);
					qDep.setDestinationId(destinationID);

					Array<QuestDependency> dependencies = questDependencies.get(sourceID);
					if (dependencies == null) {
						dependencies = new Array<>();
					}

					// don't add duplicates
					boolean inList = false;
					for (QuestDependency dependency : dependencies) {
						if (dependency.getSourceId().equals(qDep.getSourceId()) && dependency.getDestinationId().equals(qDep.getDestinationId())) {
							inList = true;
							break;
						}
					}

					if (!inList) {
						dependencies.add(qDep);
						questDependencies.put(sourceID, dependencies);
					}
				}
			}
		}

		return questDependencies;
	}

	public static void parseAllConversationXMLFiles(String folderName) {
		FileHandle dirHandle = Gdx.files.internal(folderName);

		for (FileHandle entry: dirHandle.list()) {
			if (entry.isDirectory()) {
				parseAllConversationXMLFiles(entry.path());
			}
			else {
				String inputFileName = entry.file().getName();
				String fileType = inputFileName.substring(inputFileName.lastIndexOf('.') + 1);

				if (fileType.equalsIgnoreCase("graphml")) {
					String filename = inputFileName.substring(0, inputFileName.lastIndexOf('.'));
					String outputFileName = folderName + "/" + filename + ".json";
					parseConversationXMLFile("", entry.path(), outputFileName);
				}
			}
		}
	}

	public static void parseConversationXMLFile(String currentConversationID, String inputFileName, String outputFileName) {
		FileHandle outFile = Gdx.files.local(outputFileName);

		Hashtable<String, Conversation> conversations = new Hashtable<>();
		Hashtable<String, ArrayList<ConversationChoice>> associatedChoices = new Hashtable<>();

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = null;
		try {
			xml_element = xml.parse(Gdx.files.internal(inputFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		XmlReader.Element graph = xml_element.getChildByName("graph");

		// build node graph
		Hashtable<String, ConversationNode> nodes = new Hashtable<>();
		String rootId = buildNodeGraph(nodes, graph);

		//FOR CHECKING STRUCTURE
		//Json json = new Json();
		//String output = json.prettyPrint(nodes);
		//outFile.writeString(output, false);

		// using node graph, build the conversations and associated choices
		buildConversations(graph, nodes, rootId, conversations, associatedChoices);

		// remove conversations from hash table that aren't NPC or ACTION
		// need to use iterator to avoid ConcurrentModification exception
		Iterator<String> iterate = conversations.keySet().iterator();
		while (iterate.hasNext()) {
			Conversation conv = conversations.get(iterate.next());

			if (!conv.getType().equals("NPC") && !conv.getType().equals("ACTION"))
				iterate.remove();
		}

		ConversationGraph convGraph = new ConversationGraph(conversations, associatedChoices, rootId);

		if (currentConversationID != "")
			convGraph.forceSetCurrentConversation(currentConversationID);
		outFile.writeString(convGraph.toJson(), false);
	}

	private static String buildNodeGraph(Hashtable<String, ConversationNode> nodes, XmlReader.Element graph) {
		String rootId = "";

		// id
		Iterator iterator_node = graph.getChildrenByName("node").iterator();
		while(iterator_node.hasNext()){
			ConversationNode node = new ConversationNode();
			XmlReader.Element node_element = (XmlReader.Element)iterator_node.next();
			node.id = node_element.getAttribute("id");

			// data
			Iterator iterator_data = node_element.getChildrenByName("data").iterator();
			while(iterator_data.hasNext()) {
				XmlReader.Element data_element = (XmlReader.Element)iterator_data.next();
				String key = data_element.getAttribute("key");

				if (key.equals("d5")) {
					node.character = data_element.getText();
				}
				else if (key.equals("d6")) {
					XmlReader.Element shapeNode = data_element.getChildByName("y:ShapeNode");
					XmlReader.Element label = shapeNode.getChildByName("y:NodeLabel");
					//node.data = label.getText().replace('\n', ' ').replace("  ", " ");
					String temp = label.getText().replace('\'', '^'); // single apostrophe
					node.data = temp;

					// type
					XmlReader.Element fill = shapeNode.getChildByName("y:Fill");
					String color = fill.getAttribute("color");
					if (color.equals("#999999"))
						node.type = ConversationNode.NodeType.CMD;
					else if (color.equalsIgnoreCase("#FFFF00"))
						node.type = ConversationNode.NodeType.CHOICE;
					else if (color.equalsIgnoreCase("#99CCFF")) {
						node.type = ConversationNode.NodeType.ACTION;
					}
					else {
						// all other nodes are NPC
						node.type = ConversationNode.NodeType.NPC;
						if (color.equalsIgnoreCase("#00FFFF"))
							rootId = node.id;
					}
					break;
				}
			}

			nodes.put(node.id, node);
		}

		// now add edge information to nodes
		Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
		while(iterator_edge.hasNext()){
			ConversationNode node;

			XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
			String source = edge_element.getAttribute("source");
			String target = edge_element.getAttribute("target");

			// don't add duplicates and don't allow node pointing to itself
			node = nodes.get(target);
			if (!node.previous.contains(source, false) && !node.id.equals(source))
				node.previous.add(source);

			node = nodes.get(source);
			if (!node.next.contains(target, false) && !node.id.equals(target))
				node.next.add(target);
		}

		return rootId;
	}

	private static void buildConversations(XmlReader.Element graph,
										   Hashtable<String, ConversationNode> nodes,
										   String rootId,
										   Hashtable<String, Conversation> conversations,
										   Hashtable<String, ArrayList<ConversationChoice>> associatedChoices) {

		// starting with current root id, walk through node graph
		ConversationNode rootNode = nodes.get(rootId);
		Conversation conversation = new Conversation();
		conversation.setId(rootId);
		conversation.setType(rootNode.type.toString());
		conversation.setDialog(fixWindowsLineReturns(rootNode.data));
		conversation.setData(rootNode.character);
		conversations.put(rootId, conversation);

		for (String nextId : rootNode.next) {
			String command = "";

			// go to next Choice or NPC or Action node, saving any commands we pass
			ConversationNode node = nodes.get(nextId);
			if (node.type == ConversationNode.NodeType.CMD) {
				command = node.data;
				nextId = node.next.get(0);
				node = nodes.get(nextId);
			}

			ConversationChoice choice = new ConversationChoice();
			choice.setSourceId(rootNode.id);
			choice.setDestinationId(node.id);

			if (command.isEmpty())
				choice.setConversationCommandEvent(ConversationGraphObserver.ConversationCommandEvent.NONE);
			else
				choice.setConversationCommandEvent(ConversationGraphObserver.ConversationCommandEvent.valueOf(command));

			if (node.type == ConversationNode.NodeType.CHOICE) {
				// set choice phrase for this node
				// replace special characters that need to be re-interpreted in ChoicePopUp
				String temp = node.data.replace('\'', '^'); // single apostrophe
				temp = fixWindowsLineReturns(temp);
				choice.setChoicePhrase(temp);

				// next node must be an NPC so get its id, otherwise throw exception
				nextId = node.next.get(0);
				ConversationNode nextNode = nodes.get(nextId);
				if (nextNode.type == ConversationNode.NodeType.NPC) {
					choice.setDestinationId(nextNode.id);
				}
				else {
					try { throw new Exception("Unexpected node type in choice: " + node.type.toString()); } catch (Exception e) { e.printStackTrace(); }
				}
			}
			else if (node.type == ConversationNode.NodeType.NPC || node.type == ConversationNode.NodeType.ACTION) {
				choice.setChoicePhrase(ConversationGraphObserver.ConversationCommandEvent.NO_CHOICE.toString());
			}
			else {
				try { throw new Exception("Unexpected node type: " + node.type.toString()); } catch (Exception e) { e.printStackTrace(); }
			}

			ArrayList<ConversationChoice> choices = associatedChoices.get(rootNode.id);
			if (choices == null)
				choices = new ArrayList<>();

			// don't add duplicates
			boolean inList = false;
			for (ConversationChoice ch : choices) {
				if (ch.getSourceId().equals(choice.getSourceId()) && ch.getDestinationId().equals(choice.getDestinationId())) {
					inList = true;
					break;
				}
			}
			if (!inList)
				choices.add(choice);

			associatedChoices.put(rootNode.id, choices);

			// recursive call for the current node
			buildConversations(graph, nodes, node.id, conversations, associatedChoices);
		}
	}

	private static String fixWindowsLineReturns(String text) {
		return text.replace("\r\n", "\n");
	}

	public static boolean overlapRectangles(Rectangle r1, Rectangle r2) {
		if (r1.x < r2.x + r2.width && r1.x + r1.width > r2.x && r1.y < r2.y + r2.height && r1.y + r1.height > r2.y)
			return true;
		else
			return false;
	}

	public static boolean pointInRectangle(Rectangle r, Vector2 p) {
		return r.x <= p.x && r.x + r.width >= p.x && r.y <= p.y && r.y + r.height >= p.y;
	}

	public static boolean pointInRectangle(Rectangle r, float x, float y) {
		return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
	}

	public static int getPixelLengthOfString(String str) {
		int len = 0;

		// loop through string and add up pixel lengths
		for (int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			switch(c) {
				case ' ':
				case 'i':
					len += 1;
					break;
				case 'l':
				case '1':
					len += 2;
					break;
				case 'f':
				case 'j':
				case 'k':
				case 'n':
				case 'r':
				case 's':
				case 't':
					len += 4;
					break;
				case 'Q':
					len += 6;
					break;
				default:
					// all other chars are 5 pixels
					len += 5;
			}
		}

		return len;
	}

	public static int applyPercentageAndRoundUp(int baseVal, int percentage) {
		float fVal = (float)percentage/100 * (float)baseVal;
		int iVal = (int)fVal;
		if (fVal > iVal)
			iVal++;
		return baseVal + iVal;
	}

	public static int rountUpToNextInt(float value) {
		int iVal = (int)value;
		if (value > iVal)
			iVal++;
		return iVal;
	}

	public static boolean isNullOrEmpty(String s) {
		if (s == null) {
			return true;
		}
		else if (s.isEmpty()) {
			return true;
		}
		else {
			return false;
		}
	}

	public static long getStartTime() {
		return System.nanoTime();
	}

	public static float getElapsedTime(long startNanoTime) {
		long endNanoTime = System.nanoTime();
		long elapsedNanoSeconds = endNanoTime - startNanoTime;
		return (float)elapsedNanoSeconds * 0.000001f;
	}

	public static Pixmap getScreenshot(int x, int y, int w, int h, boolean yDown){
		// Calling example:
		//		 Pixmap pixmap = Utility.getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		//
		// android 2076 x 1080
		// desktop 640 x 480
		final Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(x, y, w, h);

		if (yDown) {
			// Flip the pixmap upside down
			/*
			ByteBuffer pixels = pixmap.getPixels();
			int numBytes = w * h * 4;
			byte[] lines = new byte[numBytes];
			int numBytesPerLine = w * 4;
			for (int i = 0; i < h; i++) {
				pixels.position((h - i - 1) * numBytesPerLine);
				pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
			}
			pixels.clear();
			pixels.put(lines);
			*/
			Pixmap flipped = flipPixmap(pixmap);
			pixmap.dispose();
			return flipped;
		}

		return pixmap;
	}

	public static Pixmap flipPixmap(Pixmap src) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		Pixmap flipped = new Pixmap(width, height, src.getFormat());

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				//flipped.drawPixel(x, y, src.getPixel(width - x - 1, y));
				flipped.drawPixel(x, y, src.getPixel(x, height - y - 1));
			}
		}
		return flipped;
	}

	public static Pixmap getScreenshot2(int x, int y, int w, int h,
										boolean yDown) {

		//todo: I don't think this one works
		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

		final Pixmap pixmap1 = new Pixmap(w, h, Pixmap.Format.RGBA8888);
		ByteBuffer pixels1 = pixmap1.getPixels();
		Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE,
				pixels1);

		Pixmap pixmap = pixmap1;

		if (yDown) {
			// Flip the pixmap upside down
			ByteBuffer pixels = pixmap.getPixels();
			int numBytes = w * h * 4;
			byte[] lines = new byte[numBytes];
			int numBytesPerLine = w * 4;
			for (int i = 0; i < h; i++) {
				pixels.position((h - i - 1) * numBytesPerLine);
				pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
			}
			pixels.clear();
			pixels.put(lines);
		}

		return pixmap;
	}
}
