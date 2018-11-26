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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
	public static int myFontSize;
	public static int fontSmallSize;
	public static int myFontVerySmallSize;
	public static int myFontSuperSmallSize;

	public static void initializeElmourUISkin() {
		//NOTE!!! if elmour_ui.json is generated again, then need to replace instances of default-font with myFont:
		//	font: myFont
		//Need to initialize skin before using it because of customized TT myFont that is used in .json
		ELMOUR_UI_SKIN = new Skin();

		//FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SFPixelate.ttf"));
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/9_px.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

		// LARGE TEXT
		myFontSize = 18;
		parameter.size = myFontSize;
		parameter.color = Color.DARK_GRAY;
		parameter.shadowColor = Color.LIGHT_GRAY;
		//parameter.gamma = 200f;
		//parameter.shadowOffsetX = 2;
		//parameter.shadowOffsetY = 2;
		BitmapFont fontSign = generator.generateFont(parameter);

		//BitmapFont fontSign = new BitmapFont(Gdx.files.internal("fonts/Test18px.fnt"),
		//		Gdx.files.internal("fonts/Test18px.png"), false);

		//Gdx.gl.glEnable(GL20.GL_BLEND);
		//Color color = new Color(0,0,0,0);
		//fontSign.setColor(color);


		//NOTE: apply the filter if softer font is desired
		//fontSign.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		// SMALL TEXT
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			fontSmallSize = 15;
		}
		else{
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			fontSmallSize = 18;
		}

		parameter.size = fontSmallSize;

		BitmapFont fontSmall = generator.generateFont(parameter);
		//fontSmall.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		// VERY SMALL TEXT
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			myFontVerySmallSize = 12;
		}
		else{
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			myFontVerySmallSize = 15;
		}

		parameter.size = myFontVerySmallSize;

		BitmapFont fontVerySmall = generator.generateFont(parameter);
		//fontVerySmall.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		// SUPER SMALL TEXT
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			myFontSuperSmallSize = 7;
		}
		else{
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			myFontSuperSmallSize = 9;
		}

		parameter.size = myFontSuperSmallSize;

		BitmapFont fontSuperSmall = generator.generateFont(parameter);
		//fontSuperSmall.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		// RED TEXT
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			myFontSize = 16;
		}
		else{
			parameter.shadowOffsetX = 1;
			parameter.shadowOffsetY = 1;
			myFontSize = 20;
		}

		parameter.size = myFontSize;
		parameter.color = Color.RED;
		parameter.shadowColor = Color.BLACK;

		BitmapFont fontRed = generator.generateFont(parameter);

		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		ELMOUR_UI_SKIN.add("myFont", fontSign, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontSmall", fontSmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontVerySmall", fontVerySmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontSuperSmall", fontSuperSmall, BitmapFont.class);
		ELMOUR_UI_SKIN.add("myFontRed", fontRed, BitmapFont.class);
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
		FileHandle outFile = Gdx.files.local("conversations/Chapter_2.json");
		String fullFilenamePath = "conversations/Chapter_2.graphml";

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
					node.data = label.getText().replace('\n', ' ').replace("  ", " ");

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
		conversation.setDialog(rootNode.data);
		conversation.setCharacter(rootNode.character);
		conversations.put(rootId, conversation);

		if (rootId.equals("n18")) {
			int x;
			x=0;
		}
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
				choice.setChoicePhrase(node.data);

				// next node must be an NPC so get its id, otherwise throw exception
				nextId = node.next.get(0);
				ConversationNode nextNode = nodes.get(nextId);
				if (nextNode.type == ConversationNode.NodeType.NPC) {
					choice.setDestinationId(nextNode.id);
				}
				else {
					try { throw new Exception("Unexpected node type"); } catch (Exception e) { e.printStackTrace(); }
				}
			}
			else if (node.type == ConversationNode.NodeType.NPC || node.type == ConversationNode.NodeType.ACTION) {
				choice.setChoicePhrase(ConversationGraphObserver.ConversationCommandEvent.NO_CHOICE.toString());
			}
			else {
				try { throw new Exception("Unexpected node type"); } catch (Exception e) { e.printStackTrace(); }
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
}
