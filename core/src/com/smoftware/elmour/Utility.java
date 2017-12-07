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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.XmlReader;
import com.smoftware.elmour.dialog.Conversation;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.dialog.ConversationNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

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

		Hashtable<String, Conversation> conversations = new Hashtable<>();
		Hashtable<String, ArrayList<ConversationChoice>> associatedChoices = new Hashtable<>();

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = null;
		try {
			xml_element = xml.parse(Gdx.files.internal(fullFilenamePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		XmlReader.Element graph = xml_element.getChildByName("graph");

		// build node graph
		Hashtable<String, ConversationNode> nodes = new Hashtable<>();
		String rootId = buildNodeGraph(nodes, graph);

		Json json = new Json();
		String output = json.prettyPrint(nodes);
		outFile.writeString(output, false);

		// using node graph, build the conversations and associated choices
		buildConversations(graph, nodes, rootId, conversations, associatedChoices);

		// Loop through edges again and update the targets to be correct.
		// The previous iteration set all of the targets to the node's id.
		// The function will also update the source and/or target if
		// necessary due to either node being a command.
		Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
		while(iterator_edge.hasNext()){
			XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
			String source = edge_element.getAttribute("source");
			String target = edge_element.getAttribute("target");

			Conversation sourceType = conversations.get(source);
			Conversation targetType = conversations.get(target);
			if (sourceType != null) {
				// see if source node is CHOICE or CMD
				if (sourceType.getType().equals("CHOICE") || sourceType.getType().equals("CMD")) {
					ArrayList<ConversationChoice> choices = getConversationChoicesWithUpdatedIds(graph, associatedChoices, source, target);
					associatedChoices.put(source, choices);
				}
			}
		}

		// remove conversations from hash table that aren't NPC
		// need to use iterator to avoid ConcurrentModification exception
		Iterator<String> iterate = conversations.keySet().iterator();
		while (iterate.hasNext()) {
			Conversation conv = conversations.get(iterate.next());

			if (!conv.getType().equals("NPC"))
				iterate.remove();
		}

		// remove choices that aren't associated with a conversation
		Iterator<String> choiceIterate = associatedChoices.keySet().iterator();
		while (choiceIterate.hasNext()) {
			boolean isAssociated = false;
			String id = choiceIterate.next();

			Iterator<String> convListIterate = conversations.keySet().iterator();
			while (convListIterate.hasNext()) {
				String convId = convListIterate.next();
				if (id.equals(convId)) {
					isAssociated = true;
					break;
				}
			}

			if (!isAssociated)
				choiceIterate.remove();
		}

		ConversationGraph convGraph = new ConversationGraph(conversations, associatedChoices, rootId);
		//outFile.writeString(convGraph.toJson(), false);
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

				if (key.equals("d6")) {
					XmlReader.Element shapeNode = data_element.getChildByName("y:ShapeNode");
					XmlReader.Element label = shapeNode.getChildByName("y:NodeLabel");
					node.data = label.getText();

					// type
					XmlReader.Element fill = shapeNode.getChildByName("y:Fill");
					String color = fill.getAttribute("color");
					if (color.equals("#999999"))
						node.type = ConversationNode.NodeType.CMD;
					else if (color.equals("#FFFF00"))
						node.type = ConversationNode.NodeType.CHOICE;
					else {
						// all other nodes are NPC
						node.type = ConversationNode.NodeType.NPC;
						if (color.equals("#00FFFF"))
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

			node = nodes.get(target);
			node.previous.add(source);

			node = nodes.get(source);
			node.next.add(target);
		}

		return rootId;
	}

	private static void buildConversations(XmlReader.Element graph,
										   Hashtable<String, ConversationNode> nodes,
										   String rootId,
										   Hashtable<String, Conversation> conversations,
										   Hashtable<String, ArrayList<ConversationChoice>> associatedChoices) {

		// starting with root node, walk through node graph
		ConversationNode rootNode = nodes.get(rootId);
		Conversation conversation = new Conversation();
		conversation.setId(rootId);
		conversation.setDialog(rootNode.data);
		conversations.put(rootId, conversation);

		for (String nextId : rootNode.next) {
			String command = "";

			// go to next choice node
			ConversationNode node = nodes.get(nextId);
			if (node.type == ConversationNode.NodeType.CMD) {
				command = node.data;
				nextId = node.next.get(0);
				node = nodes.get(nextId);
			}

			if (node.type == ConversationNode.NodeType.CHOICE) {
				ConversationChoice choice = new ConversationChoice();
				choice.setChoicePhrase(node.data);
				if (command.isEmpty())
					choice.setConversationCommandEvent(ConversationGraphObserver.ConversationCommandEvent.NONE);
				else
					choice.setConversationCommandEvent(ConversationGraphObserver.ConversationCommandEvent.valueOf(command));
			}
			else {
				try { throw new Exception("Unexpected node type"); } catch (Exception e) { e.printStackTrace(); }
			}
		}
	}

	private static void processEdges(Hashtable<String, Conversation> conversations, Hashtable<String, ArrayList<ConversationChoice>> associatedChoices, XmlReader.Element graph) {
		Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
		while(iterator_edge.hasNext()){
			XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
			String source = edge_element.getAttribute("source");
			String target = edge_element.getAttribute("target");

			// ignore if source id equals target id
			if (source.equals(target))
				continue;

			Conversation sourceType = conversations.get(source);
			Conversation targetType = conversations.get(target);
			if (targetType != null) {
				// see if target node is CHOICE
				if (targetType.getType().equals("CHOICE")) {
					ArrayList<ConversationChoice> choices = getConversationChoicesFromChoicePhrase(associatedChoices, source, target, targetType.getDialog());
					associatedChoices.put(source, choices);
				}

				// see if target node is CMD
				if (targetType.getType().equals("CMD")) {
					// save this CMD in the choices for the target node
					ArrayList<ConversationChoice> choices = getConversationChoicesFromCommand(associatedChoices, source, target, targetType.getDialog());
					associatedChoices.put(source, choices);
				}
			}
		}
	}

	private static ArrayList<ConversationChoice> getConversationChoicesFromCommand(Hashtable<String, ArrayList<ConversationChoice>> associatedChoices, String source, String target, String command) {
		ConversationChoice choice = new ConversationChoice();
		choice.setSourceId(source);
		choice.setDestinationId(target);
		choice.setConversationCommandEvent(ConversationGraphObserver.ConversationCommandEvent.valueOf(command));

		ArrayList<ConversationChoice> choices = associatedChoices.get(source);
		if (choices == null) {
			choices = new ArrayList<>();
			choices.add(choice);
		}
		else {
			boolean inList = false;
			for (int i = 0; i < choices.size(); i++) {
				ConversationChoice ch = choices.get(i);
				if (ch.getSourceId().equals(choice.getSourceId()) && ch.getDestinationId().equals(choice.getDestinationId())) {
					// update choice with phrase
					choice.setChoicePhrase(ch.getChoicePhrase());
					choices.set(i, choice);
					inList = true;
					break;
				}
			}
			if (!inList)
				choices.add(choice);
		}
		return choices;
	}

	private static ArrayList<ConversationChoice> getConversationChoicesFromChoicePhrase(Hashtable<String, ArrayList<ConversationChoice>> associatedChoices, String source, String target, String choicePhrase) {
		ConversationChoice choice = new ConversationChoice();
		choice.setSourceId(source);
		choice.setDestinationId(target);
		choice.setChoicePhrase(choicePhrase);

		ArrayList<ConversationChoice> choices = associatedChoices.get(source);
		if (choices == null) {
			choices = new ArrayList<>();
			choices.add(choice);
		}
		else {
			boolean inList = false;
			for (int i = 0; i < choices.size(); i++) {
				ConversationChoice ch = choices.get(i);
				if (ch.getSourceId().equals(choice.getSourceId()) && ch.getDestinationId().equals(choice.getDestinationId())) {
					// update choice with command
					choice.setConversationCommandEvent(ch.getConversationCommandEvent());
					choices.set(i, choice);
					inList = true;
					break;
				}
			}
			if (!inList)
				choices.add(choice);
		}
		return choices;
	}

	private static ArrayList<ConversationChoice> getConversationChoicesWithUpdatedIds(XmlReader.Element graph, Hashtable<String, ArrayList<ConversationChoice>> associatedChoices, String source, String target) {
		// need to get original source first to see where this choice came from
		// so get original source where target equals the passed in source
		String originalSource = "";
		Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
		while(iterator_edge.hasNext()){
			XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
			if (source.equals(edge_element.getAttribute("target"))) {
				originalSource = edge_element.getAttribute("source");
				break;
			}
		}

		ArrayList<ConversationChoice> choices = associatedChoices.get(originalSource);
		if (choices != null) {
			for (int i = 0; i < choices.size(); i++) {
				ConversationChoice ch = choices.get(i);
				if (ch.getDestinationId().equals(source)) {
					// if source or target is a command node, then
					// update choice's source and target
					String newSource = getCorrectSource(graph, originalSource);
					String newTarget = getCorrectTarget(graph, target);
					ch.setSourceId(newSource);
					ch.setDestinationId(newTarget);
					choices.set(i, ch);
					break;
				}
			}
		}
		return choices;
	}

	private static String getCorrectSource(XmlReader.Element graph, String source) {
		String newSource = source;

		if (isCommandNode(graph, source)) {
			Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
			while(iterator_edge.hasNext()){
				XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
				if (source.equals(edge_element.getAttribute("target"))) {
					newSource = edge_element.getAttribute("source");
					break;
				}
			}
		}

		return newSource;
	}

	private static String getCorrectTarget(XmlReader.Element graph, String target) {
		String newTarget = target;

		if (isCommandNode(graph, target)) {
			Iterator iterator_edge = graph.getChildrenByName("edge").iterator();
			while(iterator_edge.hasNext()){
				XmlReader.Element edge_element = (XmlReader.Element)iterator_edge.next();
				if (target.equals(edge_element.getAttribute("target"))) {
					newTarget = edge_element.getAttribute("source");
					break;
				}
			}
		}

		return newTarget;
	}

	private static boolean isCommandNode(XmlReader.Element graph, String id) {
		boolean retVal = false;

		Iterator iterator_node = graph.getChildrenByName("node").iterator();
		while(iterator_node.hasNext()) {
			XmlReader.Element node_element = (XmlReader.Element) iterator_node.next();
			String id2 = node_element.getAttribute("id");

			if (id2.equals(id)) {
				// data
				Iterator iterator_data = node_element.getChildrenByName("data").iterator();
				while (iterator_data.hasNext()) {
					XmlReader.Element data_element = (XmlReader.Element) iterator_data.next();
					String key = data_element.getAttribute("key");

					if (key.equals("d6")) {
						// type
						XmlReader.Element shapeNode = data_element.getChildByName("y:ShapeNode");
						XmlReader.Element fill = shapeNode.getChildByName("y:Fill");
						String color = fill.getAttribute("color");
						if (color.equals("#999999")) {
							// this is a command node
							retVal = true;
						}
					}
				}
			}
		}
		return retVal;
	}
}
