package com.smoftware.elmour.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.audio.AudioManager;
import com.smoftware.elmour.audio.AudioObserver;
import com.smoftware.elmour.audio.AudioSubject;
import com.smoftware.elmour.sfx.ParticleEffectFactory;

import java.util.Hashtable;

public abstract class Map implements AudioSubject{
    private static final String TAG = Map.class.getSimpleName();

    public final static float UNIT_SCALE  = 1 / 16f;//ElmourGame.PPM;

    private Array<AudioObserver> _observers;

    //Map layers
    protected final static String COLLISION_LAYER = "COLLISION_LAYER";
    protected final static String SPAWNS_LAYER = "SPAWNS_LAYER";
    protected final static String PORTAL_LAYER = "PORTAL_LAYER";
    protected final static String QUEST_ITEM_SPAWN_LAYER = "MAP_QUEST_ITEM_SPAWN_LAYER";
    protected final static String QUEST_DISCOVER_LAYER = "MAP_QUEST_DISCOVER_LAYER";
    protected final static String ENEMY_SPAWN_LAYER = "ENEMY_SPAWNS_LAYER";
    protected final static String PARTICLE_EFFECT_SPAWN_LAYER = "PARTICLE_EFFECT_SPAWN_LAYER";
    protected final static String INTERACTION_LAYER = "INTERACTION";
    protected final static String ZERO_OPACITY_LAYER = "0_OPACITY";
    protected final static String WATER_OBSTACLE_LAYER = "WATER_OBSTACLE";
    protected final static String WATERFALL_OBSTACLE_LAYER = "WATERFALL_OBSTACLE";
    protected final static String UNDERBRIDGE_OBSTACLE_LAYER = "UNDERBRIDGE_OBSTACLE";
    protected final static String NPC_BOUNDS_LAYER = "NPC_BOUNDS";
    protected final static String Z_GATES_LAYER = "Z_GATES";

    public final static String BACKGROUND_LAYER = "Background_Layer";
    public final static String GROUND_LAYER = "Ground_Layer";
    public final static String DECORATION_LAYER = "Decoration_Layer";

    public final static String LIGHTMAP_DAWN_LAYER = "MAP_LIGHTMAP_LAYER_DAWN";
    public final static String LIGHTMAP_AFTERNOON_LAYER = "MAP_LIGHTMAP_LAYER_AFTERNOON";
    public final static String LIGHTMAP_DUSK_LAYER = "MAP_LIGHTMAP_LAYER_DUSK";
    public final static String LIGHTMAP_NIGHT_LAYER = "MAP_LIGHTMAP_LAYER_NIGHT";

    //Starting locations
    protected final static String PLAYER_START = "PLAYER_START";
    protected final static String NPC_START = "NPC_START";
    protected final static String NPC1 = "NPC1";

    protected Json _json;

    protected Vector2 _playerStartPositionRect;
    protected Vector2 _closestPlayerStartPosition;
    protected Vector2 _convertedUnits;
    protected TiledMap _currentMap = null;
    protected Vector2 _playerStart;
    protected String  playerZLayer = "ZDOWN";
    protected String  shadowZLayer = "ZSHADOWDOWN";
    protected Array<Vector2> _npcStartPositions;
    protected Hashtable<String, Vector2> _specialNPCStartPositions;

    protected MapLayer _collisionLayer = null;
    protected MapLayer _portalLayer = null;
    protected MapLayer _spawnsLayer = null;
    protected MapLayer _questItemSpawnLayer = null;
    protected MapLayer _questDiscoverLayer = null;
    protected MapLayer _enemySpawnLayer = null;
    protected MapLayer _particleEffectSpawnLayer = null;
    protected MapLayer interactionLayer = null;
    protected MapLayer zeroOpacityLayer = null;
    protected MapLayer waterObstacleLayer = null;
    protected MapLayer waterfallObstacleLayer = null;
    protected MapLayer underBridgeObstacleLayer = null;
    protected MapLayer npcBoundsLayer = null;
    protected MapLayer zGatesLayer = null;

    protected MapLayer _lightMapDawnLayer = null;
    protected MapLayer _lightMapAfternoonLayer = null;
    protected MapLayer _lightMapDuskLayer = null;
    protected MapLayer _lightMapNightLayer = null;

    protected MapFactory.MapType _currentMapType;
    protected Array<Entity> mapEntities;
    protected Array<Entity> _mapQuestEntities;
    protected Array<ParticleEffect> _mapParticleEffects;

    Map( MapFactory.MapType mapType, String fullMapPath){
        _json = new Json();
        mapEntities = new Array<Entity>(10);
        _observers = new Array<AudioObserver>();
        _mapQuestEntities = new Array<Entity>();
        _mapParticleEffects = new Array<ParticleEffect>();
        _currentMapType = mapType;
        _playerStart = new Vector2(0,0);
        _playerStartPositionRect = new Vector2(0,0);
        _closestPlayerStartPosition = new Vector2(0,0);
        _convertedUnits = new Vector2(0,0);

        if( fullMapPath == null || fullMapPath.isEmpty() ) {
            Gdx.app.debug(TAG, "Map is invalid");
            return;
        }

        Utility.loadMapAsset(fullMapPath);
        if( Utility.isAssetLoaded(fullMapPath) ) {
            _currentMap = Utility.getMapAsset(fullMapPath);
        }else{
            Gdx.app.debug(TAG, "Map not loaded");
            return;
        }

        _collisionLayer = _currentMap.getLayers().get(COLLISION_LAYER);
        if( _collisionLayer == null ){
            Gdx.app.debug(TAG, "No collision layer!");
        }

        _portalLayer = _currentMap.getLayers().get(PORTAL_LAYER);
        if( _portalLayer == null ){
            Gdx.app.debug(TAG, "No portal layer!");
        }

        _spawnsLayer = _currentMap.getLayers().get(SPAWNS_LAYER);
        if( _spawnsLayer == null ){
            Gdx.app.debug(TAG, "No spawn layer!");
        }else{
            setClosestStartPosition(_playerStart);
        }

        _questItemSpawnLayer = _currentMap.getLayers().get(QUEST_ITEM_SPAWN_LAYER);
        if( _questItemSpawnLayer == null ){
            Gdx.app.debug(TAG, "No quest item spawn layer!");
        }

        _questDiscoverLayer = _currentMap.getLayers().get(QUEST_DISCOVER_LAYER);
        if( _questDiscoverLayer == null ){
            Gdx.app.debug(TAG, "No quest discover layer!");
        }

        _enemySpawnLayer = _currentMap.getLayers().get(ENEMY_SPAWN_LAYER);
        if( _enemySpawnLayer == null ){
            Gdx.app.debug(TAG, "No enemy layer found!");
        }

        _lightMapDawnLayer = _currentMap.getLayers().get(LIGHTMAP_DAWN_LAYER);
        if( _lightMapDawnLayer == null ){
            Gdx.app.debug(TAG, "No dawn lightmap layer found!");
        }

        _lightMapAfternoonLayer = _currentMap.getLayers().get(LIGHTMAP_AFTERNOON_LAYER);
        if( _lightMapAfternoonLayer == null ){
            Gdx.app.debug(TAG, "No afternoon lightmap layer found!");
        }


        _lightMapDuskLayer = _currentMap.getLayers().get(LIGHTMAP_DUSK_LAYER);
        if( _lightMapDuskLayer == null ){
            Gdx.app.debug(TAG, "No dusk lightmap layer found!");
        }

        _lightMapNightLayer = _currentMap.getLayers().get(LIGHTMAP_NIGHT_LAYER);
        if( _lightMapNightLayer == null ){
            Gdx.app.debug(TAG, "No night lightmap layer found!");
        }

        _particleEffectSpawnLayer = _currentMap.getLayers().get(PARTICLE_EFFECT_SPAWN_LAYER);
        if( _particleEffectSpawnLayer == null ){
            Gdx.app.debug(TAG, "No particle effect spawn layer!");
        }

        waterObstacleLayer = _currentMap.getLayers().get(WATER_OBSTACLE_LAYER);
        if( waterObstacleLayer == null ){
            Gdx.app.debug(TAG, "No water obstacle layer!");
        }

        waterfallObstacleLayer = _currentMap.getLayers().get(WATERFALL_OBSTACLE_LAYER);
        if( waterfallObstacleLayer == null ){
            Gdx.app.debug(TAG, "No waterfall obstacle layer!");
        }

        underBridgeObstacleLayer = _currentMap.getLayers().get(UNDERBRIDGE_OBSTACLE_LAYER);
        if( underBridgeObstacleLayer == null ){
            Gdx.app.debug(TAG, "No underbridge obstacle layer!");
        }

        interactionLayer = _currentMap.getLayers().get(INTERACTION_LAYER);
        if( interactionLayer == null ){
            Gdx.app.debug(TAG, "No interaction layer!");
        }

        zeroOpacityLayer = _currentMap.getLayers().get(ZERO_OPACITY_LAYER);
        if( zeroOpacityLayer == null ){
            Gdx.app.debug(TAG, "No 0 opacity layer!");
        }

        npcBoundsLayer = _currentMap.getLayers().get(NPC_BOUNDS_LAYER);
        if( npcBoundsLayer == null ){
            Gdx.app.debug(TAG, "No NPC bounds layerr!");
        }

        zGatesLayer = _currentMap.getLayers().get(Z_GATES_LAYER);
        if( zGatesLayer == null ){
            Gdx.app.debug(TAG, "No level gates layerr!");
        }

        _npcStartPositions = getNPCStartPositions();
        _specialNPCStartPositions = getSpecialNPCStartPositions();

        //Observers
        this.addObserver(AudioManager.getInstance());
    }

    public String getPlayerZLayer() { return playerZLayer; }

    public void setPlayerZLayer(String playerZLayer) { this.playerZLayer = playerZLayer; }

    public String getShadowZLayer() { return shadowZLayer; }

    public void setShadowZLayer(String shadowZLayer) { this.shadowZLayer = shadowZLayer; }

    public MapLayer getLightMapDawnLayer(){
        return _lightMapDawnLayer;
    }

    public MapLayer getLightMapAfternoonLayer(){
        return _lightMapAfternoonLayer;
    }

    public MapLayer getLightMapDuskLayer(){
        return _lightMapDuskLayer;
    }

    public MapLayer getLightMapNightLayer(){
        return _lightMapNightLayer;
    }

    public Array<Vector2> getParticleEffectSpawnPositions(ParticleEffectFactory.ParticleEffectType particleEffectType) {
        Array<MapObject> objects = new Array<MapObject>();
        Array<Vector2> positions = new Array<Vector2>();

        for( MapObject object: _particleEffectSpawnLayer.getObjects()){
            String name = object.getName();

            if(     name == null || name.isEmpty() ||
                    !name.equalsIgnoreCase(particleEffectType.toString())){
                continue;
            }

            Rectangle rect = ((RectangleMapObject)object).getRectangle();
            //Get center of rectangle
            float x = rect.getX() + (rect.getWidth()/2);
            float y = rect.getY() + (rect.getHeight()/2);

            //scale by the unit to convert from map coordinates
            x *= UNIT_SCALE;
            y *= UNIT_SCALE;

            positions.add(new Vector2(x,y));
        }
        return positions;
    }

    public Array<Vector2> getQuestItemSpawnPositions(String objectName, String objectTaskID) {
        Array<MapObject> objects = new Array<MapObject>();
        Array<Vector2> positions = new Array<Vector2>();

        for( MapObject object: _questItemSpawnLayer.getObjects()){
            String name = object.getName();
            String taskID = (String)object.getProperties().get("taskID");

            if(        name == null || taskID == null ||
                       name.isEmpty() || taskID.isEmpty() ||
                       !name.equalsIgnoreCase(objectName) ||
                       !taskID.equalsIgnoreCase(objectTaskID)){
                continue;
            }
            //Get center of rectangle
            float x = ((RectangleMapObject)object).getRectangle().getX();
            float y = ((RectangleMapObject)object).getRectangle().getY();

            //scale by the unit to convert from map coordinates
            x *= UNIT_SCALE;
            y *= UNIT_SCALE;

            positions.add(new Vector2(x,y));
        }
        return positions;
    }

    public Array<Entity> getMapEntities(){
        return mapEntities;
    }

    public Array<Entity> getMapQuestEntities(){
        return _mapQuestEntities;
    }

    public Array<ParticleEffect> getMapParticleEffects(){
        return _mapParticleEffects;
    }

    public void addMapQuestEntities(Array<Entity> entities){
        _mapQuestEntities.addAll(entities);
    }

    public MapFactory.MapType getCurrentMapType(){
        return _currentMapType;
    }

    public Vector2 getPlayerStart() {
        return _playerStart;
    }

    public void setPlayerStart(Vector2 playerStart) {
        this._playerStart = playerStart;
    }

    public MapObject getNpcBoundsObject(Entity entity){
        if (npcBoundsLayer != null) {
            for (MapObject object : npcBoundsLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    if (object.getName().equals(entity.getEntityConfig().getEntityBoundsName()))
                        return object;
                }
            }
        }

        return null;
    }

    protected void updateMapEntities(MapManager mapMgr, Batch batch, float delta){
        for(int i = 0; i < mapEntities.size; i++){
            mapEntities.get(i).update(mapMgr, batch, delta);
        }
        for( int i=0; i < _mapQuestEntities.size; i++){
            _mapQuestEntities.get(i).update(mapMgr, batch, delta);
        }
    }

    protected void updateMapEffects(MapManager mapMgr, Batch batch, float delta){
        for( int i=0; i < _mapParticleEffects.size; i++){
            batch.begin();
            _mapParticleEffects.get(i).draw(batch, delta);
            batch.end();
        }
    }

    protected void dispose(){
        for(int i = 0; i < mapEntities.size; i++){
            mapEntities.get(i).dispose();
        }
        for( int i=0; i < _mapQuestEntities.size; i++){
            _mapQuestEntities.get(i).dispose();
        }
        for( int i=0; i < _mapParticleEffects.size; i++){
            _mapParticleEffects.get(i).dispose();
        }
    }

    public MapLayer getCollisionLayer(){
        return _collisionLayer;
    }

    public MapLayer getInteractionLayer(){ return interactionLayer; }

    public MapLayer getWaterObstacleLayer() { return waterObstacleLayer; }

    public MapLayer getWaterfallObstacleLayer() { return waterfallObstacleLayer; }

    public MapLayer getUnderBridgeObstacleLayer() { return underBridgeObstacleLayer; }

    public MapLayer getZeroOpacityLayer() { return zeroOpacityLayer; }

    public MapLayer getPortalLayer(){
        return _portalLayer;
    }

    public MapLayer getNpcBoundsLayer () { return npcBoundsLayer; }

    public MapLayer getZGatesLayer() { return zGatesLayer; }

    public MapLayer getQuestItemSpawnLayer(){
        return _questItemSpawnLayer;
    }

    public MapLayer getQuestDiscoverLayer(){
        return _questDiscoverLayer;
    }

    public MapLayer getEnemySpawnLayer() {
        return _enemySpawnLayer;
    }

    public TiledMap getCurrentTiledMap() {
        return _currentMap;
    }

    public Vector2 getPlayerStartUnitScaled(){
        Vector2 playerStart = _playerStart.cpy();
        playerStart.set(_playerStart.x * UNIT_SCALE, _playerStart.y * UNIT_SCALE);
        return playerStart;
    }

    private Array<Vector2> getNPCStartPositions(){
        Array<Vector2> npcStartPositions = new Array<Vector2>();

        for( MapObject object: _spawnsLayer.getObjects()){
            String objectName = object.getName();

            if( objectName == null || objectName.isEmpty() ){
                continue;
            }

            if( objectName.equalsIgnoreCase(NPC1) ){
                //Get center of rectangle
                float x = ((RectangleMapObject)object).getRectangle().getX();
                float y = ((RectangleMapObject)object).getRectangle().getY();

                //scale by the unit to convert from map coordinates
                x *= UNIT_SCALE;
                y *= UNIT_SCALE;

                npcStartPositions.add(new Vector2(x,y));
            }
        }
        return npcStartPositions;
    }

    private Hashtable<String, Vector2> getSpecialNPCStartPositions(){
        Hashtable<String, Vector2> specialNPCStartPositions = new Hashtable<String, Vector2>();

        for( MapObject object: _spawnsLayer.getObjects()){
            String objectName = object.getName();

            if( objectName == null || objectName.isEmpty() ){
                continue;
            }

            //This is meant for all the special spawn locations, a catch all, so ignore known ones
            if(     objectName.equalsIgnoreCase(NPC_START) ||
                    objectName.equalsIgnoreCase(PLAYER_START) ){
                continue;
            }

            //Get center of rectangle
            float x = ((RectangleMapObject)object).getRectangle().getX();
            float y = ((RectangleMapObject)object).getRectangle().getY();

            //scale by the unit to convert from map coordinates
            x *= UNIT_SCALE;
            y *= UNIT_SCALE;

            specialNPCStartPositions.put(objectName, new Vector2(x,y));
        }
        return specialNPCStartPositions;
    }

    private void setClosestStartPosition(final Vector2 position){
         Gdx.app.debug(TAG, "setClosestStartPosition INPUT: (" + position.x + "," + position.y + ") " + _currentMapType.toString());

        //Get last known position on this map
        _playerStartPositionRect.set(0,0);
        _closestPlayerStartPosition.set(0,0);
        float shortestDistance = 0f;

        //Go through all player start positions and choose closest to last known position
        for( MapObject object: _spawnsLayer.getObjects()){
            String objectName = object.getName();

            if( objectName == null || objectName.isEmpty() ){
                continue;
            }

            if( objectName.equalsIgnoreCase(PLAYER_START) ){
                ((RectangleMapObject)object).getRectangle().getPosition(_playerStartPositionRect);
                float distance = position.dst2(_playerStartPositionRect);

                Gdx.app.debug(TAG, "DISTANCE: " + distance + " for " + _currentMapType.toString());

                if( distance < shortestDistance || shortestDistance == 0 ){
                    _closestPlayerStartPosition.set(_playerStartPositionRect);
                    shortestDistance = distance;
                    Gdx.app.debug(TAG, "setClosestStartPosition: closest START is: (" + _closestPlayerStartPosition.x + "," + _closestPlayerStartPosition.y + ") " +  _currentMapType.toString());
                }
            }
        }
        _playerStart =  _closestPlayerStartPosition.cpy();
    }

    public void setClosestStartPositionFromScaledUnits(Vector2 position){
        if( UNIT_SCALE <= 0 )
            return;

        _convertedUnits.set(position.x/UNIT_SCALE, position.y/UNIT_SCALE);
        setClosestStartPosition(_convertedUnits);
    }

    public void setStartPositionFromPreviousMap(MapFactory.MapType previousMap) {
        float shortestDistance = 0f;
        Vector2 portalObjectPos = new Vector2(0f, 0f);

        // Find location of portal for the previous map
        for (MapObject object : _portalLayer.getObjects()){
            String objectName = object.getName();

            if( objectName == null || objectName.isEmpty() ){
                continue;
            }

            if( objectName.equalsIgnoreCase(previousMap.toString())) {
                // found portal object
                Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
                portalObjectPos.set(rectangle.x, rectangle.y);
                break;
            }
        }

        // Go through all player start positions and choose closest to map portal position
        for( MapObject object: _spawnsLayer.getObjects()){
            String objectName = object.getName();

            if( objectName == null || objectName.isEmpty() ){
                continue;
            }

            if( objectName.equalsIgnoreCase(PLAYER_START) ){
                ((RectangleMapObject)object).getRectangle().getPosition(_playerStartPositionRect);
                float distance = portalObjectPos.dst2(_playerStartPositionRect);

                Gdx.app.debug(TAG, "DISTANCE: " + distance + " for " + _currentMapType.toString());

                if( distance < shortestDistance || shortestDistance == 0 ){
                    _closestPlayerStartPosition.set(_playerStartPositionRect);
                    shortestDistance = distance;
                    Gdx.app.debug(TAG, "setStartPositionFromPreviousMap: closest START is: (" + _closestPlayerStartPosition.x + "," + _closestPlayerStartPosition.y + ") " +  _currentMapType.toString());
                }
            }
        }
        _playerStart =  _closestPlayerStartPosition.cpy();
    }

    abstract public void unloadMusic();
    abstract public void loadMusic();

    abstract public void handleInteractionInit(Entity.Interaction interaction);
    abstract public void handleInteraction();
    abstract public void handleInteractionFinished();

    @Override
    public void addObserver(AudioObserver audioObserver) {
        _observers.add(audioObserver);
    }

    @Override
    public void removeObserver(AudioObserver audioObserver) {
        _observers.removeValue(audioObserver, true);
    }

    @Override
    public void removeAllObservers() {
        _observers.removeAll(_observers, true);
    }

    @Override
    public void notify(AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event) {
        for(AudioObserver observer: _observers){
            observer.onNotify(command, event);
        }
    }
}
