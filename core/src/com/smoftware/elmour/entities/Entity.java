package com.smoftware.elmour.entities;

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
import com.smoftware.elmour.inventory.InventoryElement;
import com.smoftware.elmour.inventory.InventoryElementFactory;
import com.smoftware.elmour.components.Component;
import com.smoftware.elmour.components.ComponentObserver;
import com.smoftware.elmour.components.GraphicsComponent;
import com.smoftware.elmour.components.InputComponent;
import com.smoftware.elmour.components.PhysicsComponent;
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
		SLOW_WALK_RIGHT,
		IDLE,
		STAT_ARROW,
		BATTLE_BURST,
		IMMOBILE,
		SPELL_LEFT,
		SPELL_RIGHT,
		BLUNT_LEFT,
		BLUNT_RIGHT,
		STAB_LEFT,
		STAB_RIGHT,
		KNUCKLES_LEFT,
		KNUCKLES_RIGHT,
		BLOCK_LEFT,
		BLOCK_RIGHT,
		NONE_LEFT,
		NONE_RIGHT,

		FALL_DOWN, FALL_LEFT, FALL_RIGHT, FALL_UP, REACH_LEFT, REACH_RIGHT, LAY_DOWN,

		GLITCH_LEFT_IDLE,

		STEEL_DOOR_OPEN, BOOK_STAND, CLONING_DEVICE_SCAN, CLONING_DEVICE_UP, CLONING_DEVICE_DOWN, CLONING_COMP, CLONING_COMP_BLINK, CLONING_TANK_BLINK,
		HIDDEN_ITEM, HIDDEN_ITEM_FULL, FRYPAN, FRYPAN_FALL,

		THINK, FORCEFIELD, SLEEP_LOOP, THINK_LOOP, IDEA_LOOP, BOOK, BOOK_CLOSE, PUSH_BUTTON,

		LOVE_ON, SHOCK_ON, QUESTION_ON, SLEEP_ON, SMUG_ON, HAPPY_ON, VERY_HAPPY_ON, NEUTRAL_ON, SAD_ON, CRY_ON, TEAR_ON, ANGER_ON, THINK_ON, IDEA_ON,
		LOVE_OFF, SHOCK_OFF, QUESTION_OFF, SLEEP_OFF, SMUG_OFF, HAPPY_OFF, VERY_HAPPY_OFF, NEUTRAL_OFF, SAD_OFF, CRY_OFF, TEAR_OFF, ANGER_OFF, THINK_OFF, IDEA_OFF,

		SWORD1_LEFT, SWORD1_RIGHT, SWORD2_LEFT, SWORD2_RIGHT, SWORD3_LEFT, SWORD3_RIGHT, SWORD4_LEFT, SWORD4_RIGHT, SWORD5_LEFT, SWORD5_RIGHT,
		MACE1_LEFT, MACE1_RIGHT, MACE2_LEFT, MACE2_RIGHT, MACE3_LEFT, MACE3_RIGHT, MACE4_LEFT, MACE4_RIGHT, MACE5_LEFT, MACE5_RIGHT,
		STAFF1_LEFT, STAFF1_RIGHT, STAFF2_LEFT, STAFF2_RIGHT, STAFF3_LEFT, STAFF3_RIGHT, STAFF4_LEFT, STAFF4_RIGHT, STAFF5_LEFT, STAFF5_RIGHT,
		DAGGER1_LEFT, DAGGER1_RIGHT, DAGGER2_LEFT, DAGGER2_RIGHT, DAGGER3_LEFT, DAGGER3_RIGHT, DAGGER4_LEFT, DAGGER4_RIGHT, DAGGER5_LEFT, DAGGER5_RIGHT,
		KNUCKLES1_LEFT, KNUCKLES1_RIGHT, KNUCKLES2_LEFT, KNUCKLES2_RIGHT, KNUCKLES3_LEFT, KNUCKLES3_RIGHT, KNUCKLES4_LEFT, KNUCKLES4_RIGHT, KNUCKLES5_LEFT, KNUCKLES5_RIGHT,
		THROW1_LEFT, THROW1_RIGHT, THROW2_LEFT, THROW2_RIGHT, THROW3_LEFT, THROW3_RIGHT, THROW4_LEFT, THROW4_RIGHT, THROW5_LEFT, THROW5_RIGHT,

		BITE_LEFT, BITE_RIGHT,

		THUNDER,

		BATTLE_EQUIP, BATTLE_KEY, EQUIP_BATTLE, EQUIP_KEY, KEY_BATTLE, KEY_EQUIP, OPEN,

		SWORD1_BLOCK_LEFT, SWORD1_BLOCK_RIGHT, SWORD2_BLOCK_LEFT, SWORD2_BLOCK_RIGHT, SWORD3_BLOCK_LEFT, SWORD3_BLOCK_RIGHT, SWORD4_BLOCK_LEFT, SWORD4_BLOCK_RIGHT, SWORD5_BLOCK_LEFT, SWORD5_BLOCK_RIGHT,
		MACE1_BLOCK_LEFT, MACE1_BLOCK_RIGHT, MACE2_BLOCK_LEFT, MACE2_BLOCK_RIGHT, MACE3_BLOCK_LEFT, MACE3_BLOCK_RIGHT, MACE4_BLOCK_LEFT, MACE4_BLOCK_RIGHT, MACE5_BLOCK_LEFT, MACE5_BLOCK_RIGHT,
		STAFF1_BLOCK_LEFT, STAFF1_BLOCK_RIGHT, STAFF2_BLOCK_LEFT, STAFF2_BLOCK_RIGHT, STAFF3_BLOCK_LEFT, STAFF3_BLOCK_RIGHT, STAFF4_BLOCK_LEFT, STAFF4_BLOCK_RIGHT, STAFF5_BLOCK_LEFT, STAFF5_BLOCK_RIGHT
	}

	public enum BattleEntityType { PARTY, ENEMY, UNKNOWN }

	public enum IdType {
		WEAPON_ID,
		HELMET_ID,
		BREASTPLATE_ID,
		LEGGINGS_ID
	}

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
		CUTSCENE_Chapter2Screen_Dagger {
			@Override
			public String toString() { return "CUTSCENE_Chapter2Screen_Dagger"; }
		},
		CUTSCENE_Chapter2Screen_Mace {
			@Override
			public String toString() { return "CUTSCENE_Chapter2Screen_Mace"; }
		},
		CUTSCENE_Chapter2Screen_Staff {
			@Override
			public String toString() { return "CUTSCENE_Chapter2Screen_Staff"; }
		},
		CUTSCENE_Chapter2Screen_Sword {
			@Override
			public String toString() { return "CUTSCENE_Chapter2Screen_Sword"; }
		},
		EXIT {
			@Override
			public String toString() { return "EXIT"; }
		},
		GTDOOR {
			@Override
			public String toString() { return "GTDOOR"; }
		},
		INN {
			@Override
			public String toString() { return "INN"; }
		},
		LFASIGN1 {
			@Override
			public String toString() { return "LFASIGN1"; }
		},
		LFASIGN2 {
			@Override
			public String toString() { return "LFASIGN2"; }
		},
		LFBSIGN1 {
			@Override
			public String toString() { return "LFBSIGN1"; }
		},
		LFCSIGN1 {
			@Override
			public String toString() { return "LFCSIGN1"; }
		},
		LFCSIGN2 {
			@Override
			public String toString() { return "LFCSIGN2"; }
		},
		LFSIGN1 {
			@Override
			public String toString() { return "LFSIGN1"; }
		},
		LFSIGN2 {
			@Override
			public String toString() { return "LFSIGN2"; }
		},
		LFSIGN3 {
			@Override
			public String toString() { return "LFSIGN3"; }
		},
		LFSIGN4 {
			@Override
			public String toString() { return "LFSIGN4"; }
		},
		LFSIGN5 {
			@Override
			public String toString() { return "LFSIGN5"; }
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
		STORAGE_ROOM_SIGN {
			@Override
			public String toString() { return "STORAGE_ROOM_SIGN"; }
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
		T1DOOR4 {
			@Override
			public String toString() { return "T1DOOR4"; }
		},
		WEAPONS_ROOM {
			@Override
			public String toString() { return "WEAPONS_ROOM"; }
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
	private InventoryElement enemyWeapon;

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
        enemyWeapon = entity.enemyWeapon;
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
        enemyWeapon = new InventoryElement();
	}

	public EntityConfig getEntityConfig() {
		return _entityConfig;
	}

	public BattleEntityType getBattleEntityType() { return battleEntityType; }

	public void setBattleEntityType(BattleEntityType battleEntityType) { this.battleEntityType = battleEntityType; }

	public int getBattlePosition() { return battlePosition; }

	public void setBattlePosition(int battlePosition) { this.battlePosition = battlePosition; }

	public void setAlive(boolean isAlive) { this.isAlive = isAlive; }

	public boolean isAlive() { return isAlive; }

	public void setWeapon(InventoryElement weapon) {
        if (battleEntityType.equals(BattleEntityType.PARTY)) {
            // Party weapons are always saved in the profile
            ProfileManager.getInstance().setProperty(_entityConfig.getEntityID() + IdType.WEAPON_ID.toString(), weapon.id);
        }
		else if (battleEntityType.equals(BattleEntityType.ENEMY)) {
            // Don't save Enemy weapons to profile
            this.enemyWeapon = weapon;
        }
	}

	public InventoryElement getWeapon() {
		InventoryElement weapon = null;

        if (battleEntityType.equals(BattleEntityType.PARTY)) {
            // Party weapons are always retrieved from the profile
            InventoryElement.ElementID weaponId = ProfileManager.getInstance().getProperty(_entityConfig.getEntityID() + IdType.WEAPON_ID.toString(), InventoryElement.ElementID.class);
            if (weaponId != null)
                weapon = InventoryElementFactory.getInstance().getInventoryElement(weaponId);
        }
        else if (battleEntityType.equals(BattleEntityType.ENEMY)) {
            weapon = enemyWeapon;
        }

		return weapon;
	}

	public void setArmor(InventoryElement armorItem) {
		switch (armorItem.category) {
			case Helmet:
				ProfileManager.getInstance().setProperty(_entityConfig.getEntityID() + IdType.HELMET_ID.toString(), armorItem.id);
				break;
			case Breastplate:
				ProfileManager.getInstance().setProperty(_entityConfig.getEntityID() + IdType.BREASTPLATE_ID.toString(), armorItem.id);
				break;
			case Leggings:
				ProfileManager.getInstance().setProperty(_entityConfig.getEntityID() + IdType.LEGGINGS_ID.toString(), armorItem.id);
				break;
		}
	}

	public InventoryElement getArmor(InventoryElement.InventoryCategory category) {
		InventoryElement armorItem = null;
		InventoryElement.ElementID armorId = null;

		switch (category) {
			case Helmet:
				armorId = ProfileManager.getInstance().getProperty(_entityConfig.getEntityID() + IdType.HELMET_ID.toString(), InventoryElement.ElementID.class);
				break;
			case Breastplate:
				armorId = ProfileManager.getInstance().getProperty(_entityConfig.getEntityID() + IdType.BREASTPLATE_ID.toString(), InventoryElement.ElementID.class);
				break;
			case Leggings:
				armorId = ProfileManager.getInstance().getProperty(_entityConfig.getEntityID() + IdType.LEGGINGS_ID.toString(), InventoryElement.ElementID.class);
				break;
		}

		if (armorId != null)
			armorItem = InventoryElementFactory.getInstance().getInventoryElement(armorId);
		return armorItem;
	}

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

	public void toggleNoClipping() {
		_physicsComponent.toggleNoClipping();
	}

	public float getActualVelocity() { return _physicsComponent.getActualVelocity(); }

	public Rectangle getCurrentBoundingBox(){
		return _physicsComponent._boundingBox;
	}

	public Vector2 getCurrentPosition(){ return _graphicsComponent.getCurrentPosition(); }

	public void setCurrentPosition(Vector2 position) { _graphicsComponent.setCurrentPosition(position); }

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

	public static Entity initEntity(EntityConfig entityConfig, boolean loadAnimations){
		Json json = new Json();
		Entity entity = EntityFactory.getEntity(EntityFactory.EntityType.NPC);
		entity.setEntityConfig(entityConfig);

		if (loadAnimations)
			entity.sendMessage(Component.MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.getEntityConfig()));

		entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(new Vector2(0,0)));
		entity.sendMessage(Component.MESSAGE.INIT_STATE, json.toJson(entity.getEntityConfig().getState()));
		entity.sendMessage(Component.MESSAGE.INIT_DIRECTION, json.toJson(entity.getEntityConfig().getDirection()));

		return entity;
	}


}
