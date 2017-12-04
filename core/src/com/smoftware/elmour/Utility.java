package com.smoftware.elmour;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;
import com.smoftware.elmour.dialog.Conversation;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

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

	public static void initializeElmourUISkin() {
		//NOTE!!! if elmour_ui.json is generated again, then need to replace instances of default-font with myFont:
		//	font: myFont
		//Need to initialize skin before using it because of customized TT myFont that is used in .json
		ELMOUR_UI_SKIN = new Skin();

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/9_px.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

		// LARGE TEXT
		if (Gdx.app.getType() == Application.ApplicationType.Android)
			parameter.size = 18;
		else
			parameter.size = 24;

		parameter.color = Color.DARK_GRAY;
		parameter.shadowColor = Color.LIGHT_GRAY;
		parameter.shadowOffsetX = 2;
		parameter.shadowOffsetY = 2;
		BitmapFont fontSign = generator.generateFont(parameter);

		// SMALL TEXT
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			parameter.size = 13;
		}
		else
			parameter.size = 18;

		BitmapFont fontSmall = generator.generateFont(parameter);


		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		ELMOUR_UI_SKIN.add("myFont", fontSign, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontSmall", fontSmall, BitmapFont.class);
		ELMOUR_UI_SKIN.addRegions(new TextureAtlas(Gdx.files.internal(ELMOUR_TEXTURE_ATLAS_PATH)));
		ELMOUR_UI_SKIN.load(Gdx.files.internal(ELMOUR_SKIN_PATH));
	}

	public static void unloadAsset(String assetFilenamePath){
	// once the asset manager is done loading
	if( _assetManager.isLoaded(assetFilenamePath) ){
		_assetManager.unload(assetFilenamePath);
		} else {
			Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: " + assetFilenamePath );
		}
	}

	public static float loadCompleted(){
		return _assetManager.getProgress();
	}

	public static int numberAssetsQueued(){
		return _assetManager.getQueuedAssets();
	}

   	public static boolean updateAssetLoading(){
		return _assetManager.update();
	}

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
			Gdx.app.debug(TAG, "Map loaded!: " + mapFilenamePath);
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
			_assetManager.finishLoadingAsset(soundFilenamePath);
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

	public static void parseConversationXMLFiles() {
		FileHandle outFile = Gdx.files.local("conversations/testing.json");
		String fullFilenamePath = "conversations/testing.graphml";

		Hashtable<String, Conversation> conversations = new Hashtable<String, Conversation>();
		Hashtable<String, ArrayList<ConversationChoice>> associatedChoices = new Hashtable<String, ArrayList<ConversationChoice>>();
		String rootId = "n4"; //todo

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = null;
		try {
			xml_element = xml.parse(Gdx.files.internal(fullFilenamePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		XmlReader.Element graph = xml_element.getChildByName("graph");

		// process nodes
		// id
		Iterator iterator_node = graph.getChildrenByName("node").iterator();
		while(iterator_node.hasNext()){
			Conversation conversation = new Conversation();
			XmlReader.Element node_element = (XmlReader.Element)iterator_node.next();
			conversation.setId(node_element.getAttribute("id"));

			// data
			Iterator iterator_data = node_element.getChildrenByName("data").iterator();
			while(iterator_data.hasNext()) {
				XmlReader.Element data_element = (XmlReader.Element)iterator_data.next();
				String key = data_element.getAttribute("key");

				if (key.equals("d6")) {
					// dialog
					XmlReader.Element shapeNode = data_element.getChildByName("y:ShapeNode");
					XmlReader.Element label = shapeNode.getChildByName("y:NodeLabel");
					conversation.setDialog(label.getText());;

					// type
					XmlReader.Element fill = shapeNode.getChildByName("y:Fill");
					String color = fill.getAttribute("color");
					if (color.equals("#FF99CC"))
						conversation.setType("NPC");
					else if (color.equals("#999999"))
						conversation.setType("CMD");
					else if (color.equals("#FFFF00"))
						conversation.setType("CHOICE");
					break;
				}
			}

			conversations.put(conversation.getId(), conversation);
		}


		// process edges (associatedChoices)
		outFile.writeString("associatedChoices: {\n", true);

		Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
		while(iterator_edge.hasNext()){
			XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
			String source = edge_element.getAttribute("source");
			String target = edge_element.getAttribute("target");

			// see if target node is CHOICE
			Conversation tmp = conversations.get(target);
			if (tmp != null) {
				if (tmp.getType().equals("CHOICE")) {
					ConversationChoice choice = new ConversationChoice();
					choice.setSourceId(source);
					choice.setDestinationId(target);
					choice.setChoicePhrase(tmp.getDialog());

					ArrayList<ConversationChoice> choices = associatedChoices.get(source);
					if (choices == null)
						choices = new ArrayList<>();

					choices.add(choice);
					associatedChoices.put(source, choices);
				}
			}
		}

		// remove conversations from hash table that aren't NPC
		Set<String> keys = conversations.keySet();
		for(String key: keys){
			Conversation conv = conversations.get(key);
			if (!conv.getType().equals("NPC"))
				conversations.remove(key);
		}

		ConversationGraph convGraph = new ConversationGraph(conversations, associatedChoices, rootId);
		outFile.writeString(convGraph.toJson(), false);
	}
}
