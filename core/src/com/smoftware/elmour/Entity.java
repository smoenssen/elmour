package com.smoftware.elmour;

//todo: good article on Entity Component System architecture (ECS)
//https://www.gamedev.net/articles/programming/general-and-gameplay-programming/understanding-component-entity-systems-r3013/
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.smoftware.elmour.battle.BattleObserver;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Hashtable;

public class Entity {
	private static final String TAG = Entity.class.getSimpleName();

	public static enum ConversationStatus {
		IN_CONVERSATION,
		NOT_IN_CONVERSATION;
	}

	public static enum Direction {
		UP,
		RIGHT,
		DOWN,
		LEFT;

		static public Direction getRandomNext() {
			return Direction.values()[MathUtils.random(Direction.values().length - 1)];
		}

		public Direction getOpposite() {
			if( this == LEFT){
				return RIGHT;
			}else if( this == RIGHT){
				return LEFT;
			}else if( this == UP){
				return DOWN;
			}else{
				return UP;
			}
		}
	}

	public static enum State {
		IDLE,
		WALKING,
		RUNNING,
		IN_WATER,
		IMMOBILE;//This should always be last

		static public State getRandomNext() {
			//Ignore IMMOBILE which should be last state
			return State.values()[MathUtils.random(State.values().length - 2)];
		}
	}

	public static enum ButtonState { IS_UP, IS_DOWN }
	public static enum A_ButtonAction { PRESSED, RELEASED }
	public static enum B_ButtonAction { PRESSED, RELEASED }

	public static enum AnimationType {
		WALK_LEFT,
		WALK_RIGHT,
		WALK_UP,
		WALK_DOWN,
		RUN_LEFT,
		RUN_RIGHT,
		RUN_UP,
		RUN_DOWN,
		IDLE,
		IMMOBILE
	}

	public enum BattleEntityType { PARTY, ENEMY, UNKNOWN }

	public static enum Interaction {
		COMPASSSIGN1 {
			@Override
			public String toString() { return "COMPASSSIGN1"; }
		},
		COMPASSSIGN2 {
			@Override
			public String toString() { return "COMPASSSIGN2"; }
		},
		ARMORY {
			@Override
			public String toString() { return "ARMORY"; }
		},
		GTDOOR {
			@Override
			public String toString() { return "GTDOOR"; }
		},
		INN {
			@Override
			public String toString() { return "INN"; }
		},
		M1SIGN1 {
			@Override
			public String toString() { return "M1SIGN1"; }
		},
		M1SIGN2 {
			@Override
			public String toString() { return "M1SIGN2"; }
		},
		M3SIGN1 {
			@Override
			public String toString() { return "M3SIGN1"; }
		},
		M3DOOR1 {
			@Override
			public String toString() { return "M3DOOR1"; }
		},
		M4DOOR1 {
			@Override
			public String toString() { return "M4DOOR1"; }
		},
		M4SIGN1 {
			@Override
			public String toString() { return "M4SIGN1"; }
		},
		M5SIGN1 {
			@Override
			public String toString() { return "M5SIGN1"; }
		},
		M6SIGN1 {
			@Override
			public String toString() { return "M6SIGN1"; }
		},
		M7SIGN1 {
			@Override
			public String toString() { return "M7SIGN1"; }
		},
		M7SIGN2 {
			@Override
			public String toString() { return "M7SIGN2"; }
		},
		M7SIGN3 {
			@Override
			public String toString() { return "M7SIGN3"; }
		},
		M7SIGN4 {
			@Override
			public String toString() { return "M7SIGN4"; }
		},
        M8SIGN1 {
            @Override
            public String toString() { return "M8SIGN1"; }
        },
        M8SIGN2 {
            @Override
            public String toString() { return "M8SIGN2"; }
        },
        M9SIGN1 {
            @Override
            public String toString() { return "M9SIGN1"; }
        },
		M10SIGN1 {
			@Override
			public String toString() { return "M10SIGN1"; }
		},
        M11SIGN1 {
            @Override
            public String toString() { return "M11SIGN1"; }
        },
		M14SIGN1 {
			@Override
			public String toString() { return "M14SIGN1"; }
		},
		M14SIGN2 {
			@Override
			public String toString() { return "M14SIGN2"; }
		},
		T2DOOR1 {
			@Override
			public String toString() { return "T2DOOR1"; }
		},
		T2DOOR2 {
			@Override
			public String toString() { return "T2DOOR2"; }
		},
		T2DOOR3 {
			@Override
			public String toString() { return "T2DOOR3"; }
		},
		T2SIGN1 {
			@Override
			public String toString() { return "T2SIGN1"; }
		},
		M2SWITCH {
			@Override
			public String toString() { return "M2SWITCH"; }
		},
		Portal_Room {
			@Override
			public String toString() { return "Portal_Room"; }
		},
		NONE {
			@Override
			public String toString() { return "NONE"; }
		},
	}

	public static final int FRAME_WIDTH = 16;
	public static final int FRAME_HEIGHT = 16;
	private static final int MAX_COMPONENTS = 5;

	private Json _json;
	private EntityConfig _entityConfig;
	private Array<Component> _components;
	private InputComponent _inputComponent;
	private GraphicsComponent _graphicsComponent;
	private PhysicsComponent _physicsComponent;
	private BattleEntityType battleEntityType;
	private int battlePosition;
	private boolean isAlive;

	public Entity(Entity entity){
		set(entity);
	}

	private Entity set(Entity entity) {
		_inputComponent = entity._inputComponent;
		_graphicsComponent = entity._graphicsComponent;
		_physicsComponent = entity._physicsComponent;

		if( _components == null ){
			_components = new Array<Component>(MAX_COMPONENTS);
		}
		_components.clear();
		_components.add(_inputComponent);
		_components.add(_physicsComponent);
		_components.add(_graphicsComponent);

		_json = entity._json;

		_entityConfig = new EntityConfig(entity._entityConfig);
		battleEntityType = entity.battleEntityType;
		battlePosition = entity.battlePosition;
		isAlive = entity.isAlive;
		return this;
	}

	public Entity(InputComponent inputComponent, PhysicsComponent physicsComponent, GraphicsComponent graphicsComponent){
		_entityConfig = new EntityConfig();
		_json = new Json();

		_components = new Array<Component>(MAX_COMPONENTS);

		_inputComponent = inputComponent;
		_physicsComponent = physicsComponent;
		_graphicsComponent = graphicsComponent;

		_components.add(_inputComponent);
		_components.add(_physicsComponent);
		_components.add(_graphicsComponent);

		battleEntityType = BattleEntityType.UNKNOWN;
		battlePosition = 0;
		isAlive = false;
	}

	public EntityConfig getEntityConfig() {
		return _entityConfig;
	}

	public BattleEntityType getBattleEntityType() { return battleEntityType; }

	public void setBattleEntityType(BattleEntityType battleEntityType) { this.battleEntityType = battleEntityType; }

	public int getBattlePosition() { return battlePosition; }

	public void setBattlePosition(int battlePosition) { this.battlePosition = battlePosition; }

	public void setAlive(boolean isAlive) { this.isAlive = isAlive; }

	public boolean isAlive() { return  isAlive; }

	public void sendMessage(Component.MESSAGE messageType, String ... args){
		String fullMessage = messageType.toString();

		for (String string : args) {
			fullMessage += Component.MESSAGE_TOKEN + string;
		}

		for(Component component: _components){
			component.receiveMessage(fullMessage);
		}
	}

	public void registerObserver(ComponentObserver observer){
		_inputComponent.addObserver(observer);
		_physicsComponent.addObserver(observer);
		_graphicsComponent.addObserver(observer);
	}

	public void unregisterObservers(){
		_inputComponent.removeAllObservers();
		_physicsComponent.removeAllObservers();
		_graphicsComponent.removeAllObservers();
	}

	public void update(com.smoftware.elmour.maps.MapManager mapMgr, Batch batch, float delta){
		_inputComponent.update(this, delta);
		_physicsComponent.update(this, mapMgr, delta);
		_graphicsComponent.update(this, mapMgr, batch, delta);
	}

	public void updateShadow(com.smoftware.elmour.maps.MapManager mapMgr, Batch batch, float delta, Vector2 enitityPosition){
		_graphicsComponent.updateShadow(this, mapMgr, batch, delta, enitityPosition);
	}

	public boolean	isNextPositionCollision(com.smoftware.elmour.maps.MapManager mapMgr) {
		return _physicsComponent.isNextPositionCollision(mapMgr);
	}

	public void updateInput(float delta){
		_inputComponent.update(this, delta);
	}

	public void dispose(){
		for(Component component: _components){
			component.dispose();
		}
	}

	public void setNoClipping() {
		_physicsComponent.setNoClipping();
	}

	public float getActualVelocity() { return _physicsComponent.getActualVelocity(); }

	public Rectangle getCurrentBoundingBox(){
		return _physicsComponent._boundingBox;
	}

	public Vector2 getCurrentPosition(){ return _graphicsComponent._currentPosition; }

	public void setCurrentPosition(Vector2 position) { _graphicsComponent._currentPosition = position; }

	public Vector2 getNextPosition(){ return _physicsComponent._nextEntityPosition; }

	public Entity.State getCurrentState() { return _graphicsComponent.currentState; }

	public float getSelectionAngle() { return _physicsComponent.getSelectionAngle(); }

	public InputProcessor getInputProcessor(){
		return _inputComponent;
	}

	public void setEntityConfig(EntityConfig entityConfig){
		this._entityConfig = entityConfig;
	}

	public Animation<TextureRegion> getAnimation(Entity.AnimationType type){
		return _graphicsComponent.getAnimation(type);
	}

	static public EntityConfig getEntityConfig(String configFilePath){
		Json json = new Json();
		return json.fromJson(EntityConfig.class, Gdx.files.internal(configFilePath));
	}

	static public Array<EntityConfig> getEntityConfigs(String configFilePath){
		Json json = new Json();
		Array<EntityConfig> configs = new Array<EntityConfig>();

    	ArrayList<JsonValue> list = json.fromJson(ArrayList.class, Gdx.files.internal(configFilePath));

		for (JsonValue jsonVal : list) {
			configs.add(json.readValue(EntityConfig.class, jsonVal));
		}

		return configs;
	}

	public static EntityConfig loadEntityConfigByPath(String entityConfigPath){
		EntityConfig entityConfig = Entity.getEntityConfig(entityConfigPath);
		EntityConfig serializedConfig = ProfileManager.getInstance().getProperty(entityConfig.getEntityID(), EntityConfig.class);

		if( serializedConfig == null ){
			return entityConfig;
		}else{
			return serializedConfig;
		}
	}

	public static EntityConfig loadEntityConfig(EntityConfig entityConfig){
		EntityConfig serializedConfig = ProfileManager.getInstance().getProperty(entityConfig.getEntityID(), EntityConfig.class);

		if( serializedConfig == null ){
			return entityConfig;
		}else{
			return serializedConfig;
		}
	}

	public static Entity initEntity(EntityConfig entityConfig, Vector2 position){
		Json json = new Json();
		Entity entity = EntityFactory.getEntity(EntityFactory.EntityType.NPC);
		entity.setEntityConfig(entityConfig);

		entity.sendMessage(Component.MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.getEntityConfig()));
		entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));
		entity.sendMessage(Component.MESSAGE.INIT_STATE, json.toJson(entity.getEntityConfig().getState()));
		entity.sendMessage(Component.MESSAGE.INIT_DIRECTION, json.toJson(entity.getEntityConfig().getDirection()));

		return entity;
	}

	public static Hashtable<String, Entity> initEntities(Array<EntityConfig> configs){
		Json json = new Json();
		Hashtable<String, Entity > entities = new Hashtable<String, Entity>();
		for( EntityConfig config: configs ){
			Entity entity = EntityFactory.getEntity(EntityFactory.EntityType.NPC);

			entity.setEntityConfig(config);
			entity.sendMessage(Component.MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.getEntityConfig()));
			entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(new Vector2(0,0)));
			entity.sendMessage(Component.MESSAGE.INIT_STATE, json.toJson(entity.getEntityConfig().getState()));
			entity.sendMessage(Component.MESSAGE.INIT_DIRECTION, json.toJson(entity.getEntityConfig().getDirection()));

			entities.put(entity.getEntityConfig().getEntityID(), entity);
		}

		return entities;
	}

	public static Entity initEntity(EntityConfig entityConfig){
		Json json = new Json();
		Entity entity = EntityFactory.getEntity(EntityFactory.EntityType.NPC);
		entity.setEntityConfig(entityConfig);

		entity.sendMessage(Component.MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.getEntityConfig()));
		entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(new Vector2(0,0)));
		entity.sendMessage(Component.MESSAGE.INIT_STATE, json.toJson(entity.getEntityConfig().getState()));
		entity.sendMessage(Component.MESSAGE.INIT_DIRECTION, json.toJson(entity.getEntityConfig().getDirection()));

		return entity;
	}


}
