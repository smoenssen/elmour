package com.smoftware.elmour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.smoftware.elmour.UI.MobileControlsObserver;
import com.smoftware.elmour.UI.MobileControlsSubject;
import com.smoftware.elmour.screens.MainGameScreen;

public class PlayerInputComponent extends InputComponent implements MobileControlsObserver{

	private final static String TAG = PlayerInputComponent.class.getSimpleName();
	private Vector3 _lastMouseCoordinates;

	public PlayerInputComponent(){
		this._lastMouseCoordinates = new Vector3();

		if (ElmourGame.isAndroid())
			MobileControlsSubject.addObserver(this);
	}

	//todo: do I need a receiveMessage that takes a Vector2 for direction?

	@Override
	public void receiveMessage(String message) {
		String[] string = message.split(MESSAGE_TOKEN);

		if( string.length == 0 ) return;

		//Specifically for messages with 1 object payload
		if( string.length == 2 ) {
			if (string[0].equalsIgnoreCase(MESSAGE.CURRENT_DIRECTION.toString())) {
				_currentDirection = _json.fromJson(Entity.Direction.class, string[1]);
			}
		}
	}

	@Override
	public void dispose(){
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void update(Entity entity, float delta){
		if (ElmourGame.isAndroid()) {
			// Mobile control input
			//todo: how to pause or quit?
			//if (paused) {
			//
			//}
			//else if (quit) {
			//
			//}
			//else {

			// A button
			if (actionButtons.get(ActionButtons.A_BUTTON_PRESSED)) {
				entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.PRESSED));
			}
			else if (actionButtons.get(ActionButtons.A_BUTTON_RELEASED)) {
				entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.RELEASED));
			}

			//B button
			if (actionButtons.get(ActionButtons.B_BUTTON_PRESSED)) {
				entity.sendMessage(MESSAGE.B_BUTTON_STATUS, _json.toJson(Entity.B_ButtonAction.PRESSED));
			}
			else if (actionButtons.get(ActionButtons.B_BUTTON_RELEASED)) {
				entity.sendMessage(MESSAGE.B_BUTTON_STATUS, _json.toJson(Entity.B_ButtonAction.RELEASED));
			}

			// Joystick
			entity.sendMessage(MESSAGE.CURRENT_JOYSTICK_POSITION, _json.toJson(joystickPosition));
		}
		else {
			//Keyboard input
			if (keys.get(Keys.PAUSE)) {
				MainGameScreen.setGameState(MainGameScreen.GameState.PAUSED);
				pauseReleased();
			} else if (keys.get(Keys.LEFT)) {
				entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING));
				entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.LEFT));
			} else if (keys.get(Keys.RIGHT)) {
				entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING));
				entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.RIGHT));
			} else if (keys.get(Keys.UP)) {
				entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING));
				entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.UP));
			} else if (keys.get(Keys.DOWN)) {
				entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING));
				entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.DOWN));
			} else if (keys.get(Keys.SPACE)) {
				entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.PRESSED));
			} else if (keys.get(Keys.QUIT)) {
				quitReleased();
				Gdx.app.exit();
			} else {
				entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.IDLE));
				if (_currentDirection == null) {
					entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.DOWN));
				}
			}

			if (keys.get(Keys.ESCAPE)) {
				entity.sendMessage(MESSAGE.B_BUTTON_STATUS, _json.toJson(Entity.B_ButtonAction.PRESSED));
			}
			else {
				entity.sendMessage(MESSAGE.B_BUTTON_STATUS, _json.toJson(Entity.B_ButtonAction.RELEASED));
			}

			if (!keys.get(Keys.SPACE)) {
				entity.sendMessage(MESSAGE.A_BUTTON_STATUS, _json.toJson(Entity.A_ButtonAction.RELEASED));
			}

			if (keys.get(Keys.LEFT) || keys.get(Keys.RIGHT) || keys.get(Keys.UP) || keys.get(Keys.DOWN)) {
				if (keys.get(Keys.SPACE)) {
					entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.RUNNING));
				}
			}

			//Mouse input
			if (mouseButtons.get(Mouse.SELECT)) {
				//Gdx.app.debug(TAG, "Mouse LEFT click at : (" + _lastMouseCoordinates.x + "," + _lastMouseCoordinates.y + ")" );
				entity.sendMessage(MESSAGE.INIT_SELECT_ENTITY, _json.toJson(_lastMouseCoordinates));
				mouseButtons.put(Mouse.SELECT, false);
			}
		}
	}

	//srm to get correct diagonal speed
	//http://www.java-gaming.org/index.php?topic=32544.0

	@Override
	public boolean keyDown(int keycode) {
		if( keycode == Input.Keys.LEFT || keycode == Input.Keys.A){
			this.leftPressed();
		}
		if( keycode == Input.Keys.RIGHT || keycode == Input.Keys.D){
			this.rightPressed();
		}
		if( keycode == Input.Keys.UP || keycode == Input.Keys.W){
			this.upPressed();
		}
		if( keycode == Input.Keys.DOWN || keycode == Input.Keys.S){
			this.downPressed();
		}
		if( keycode == Input.Keys.Q){
			this.quitPressed();
		}
		if( keycode == Input.Keys.P ){
			this.pausePressed();
		}
		if (keycode == Input.Keys.SPACE) {
			this.spacePressed();
		}
		if (keycode == Input.Keys.ESCAPE) {
			this.escapePressed();
		}

		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if( keycode == Input.Keys.LEFT || keycode == Input.Keys.A){
			this.leftReleased();
		}
		if( keycode == Input.Keys.RIGHT || keycode == Input.Keys.D){
			this.rightReleased();
		}
		if( keycode == Input.Keys.UP || keycode == Input.Keys.W ){
			this.upReleased();
		}
		if( keycode == Input.Keys.DOWN || keycode == Input.Keys.S){
			this.downReleased();
		}
		if( keycode == Input.Keys.Q){
			this.quitReleased();
		}
		if( keycode == Input.Keys.P ){
			this.pauseReleased();
		}
		if (keycode == Input.Keys.SPACE) {
			this.spaceReleased();
		}
		if (keycode == Input.Keys.ESCAPE) {
			this.escapeReleased();
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//Gdx.app.debug(TAG, "GameScreen: MOUSE DOWN........: (" + screenX + "," + screenY + ")" );

		if( button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT ){
			this.setClickedMouseCoordinates(screenX, screenY);
		}

		//left is selection, right is context menu
		if( button == Input.Buttons.LEFT){
			this.selectMouseButtonPressed(screenX, screenY);
		}
		if( button == Input.Buttons.RIGHT){
			this.doActionMouseButtonPressed(screenX, screenY);
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		//left is selection, right is context menu
		if( button == Input.Buttons.LEFT){
			this.selectMouseButtonReleased(screenX, screenY);
		}
		if( button == Input.Buttons.RIGHT){
			this.doActionMouseButtonReleased(screenX, screenY);
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
	//Key/button presses
	public void leftPressed() { keys.put(Keys.LEFT, true); }
	
	public void rightPressed(){
		keys.put(Keys.RIGHT, true);
	}
	
	public void upPressed(){
		keys.put(Keys.UP, true);
	}
	
	public void downPressed(){
		keys.put(Keys.DOWN, true);
	}

	public void quitPressed(){
		keys.put(Keys.QUIT, true);
	}

	public void pausePressed() {
		keys.put(Keys.PAUSE, true);
	}

	public void spacePressed() {
		keys.put(Keys.SPACE, true);
	}

	public void escapePressed() {
		keys.put(Keys.ESCAPE, true);
	}
	
	public void setClickedMouseCoordinates(int x,int y){
		_lastMouseCoordinates.set(x, y, 0);
	}
	
	public void selectMouseButtonPressed(int x, int y){
		mouseButtons.put(Mouse.SELECT, true);
	}
	
	public void doActionMouseButtonPressed(int x, int y){
		mouseButtons.put(Mouse.DOACTION, true);
	}

	public void a_Pressed() {
		actionButtons.put(ActionButtons.A_BUTTON_PRESSED, true);
		actionButtons.put(ActionButtons.A_BUTTON_RELEASED, false);
	}

	public void b_Pressed() {
		actionButtons.put(ActionButtons.B_BUTTON_PRESSED, true);
		actionButtons.put(ActionButtons.B_BUTTON_RELEASED, false);
	}
	
	//Releases
	public void leftReleased(){
		keys.put(Keys.LEFT, false);
	}
	
	public void rightReleased(){
		keys.put(Keys.RIGHT, false);
	}
	
	public void upReleased(){
		keys.put(Keys.UP, false);
	}
	
	public void downReleased(){
		keys.put(Keys.DOWN, false);
	}
	
	public void quitReleased(){
		keys.put(Keys.QUIT, false);
	}

	public void pauseReleased() { keys.put(Keys.PAUSE, false); }

	public void spaceReleased() { keys.put(Keys.SPACE, false); }

	public void escapeReleased() { keys.put(Keys.ESCAPE, false); }
	
	public void selectMouseButtonReleased(int x, int y){
		mouseButtons.put(Mouse.SELECT, false);
	}
	
	public void doActionMouseButtonReleased(int x, int y){
		mouseButtons.put(Mouse.DOACTION, false);
	}

	public void a_Released() {
		actionButtons.put(ActionButtons.A_BUTTON_RELEASED, true);
		actionButtons.put(ActionButtons.A_BUTTON_PRESSED, false);
	}

	public void b_Released() {
		actionButtons.put(ActionButtons.B_BUTTON_RELEASED, true);
		actionButtons.put(ActionButtons.B_BUTTON_PRESSED, false);
	}

	//Joystick
	public void setJoystickPosition(Vector2 position) { joystickPosition = position; }

	public static void clear(){
		keys.put(Keys.LEFT, false);
		keys.put(Keys.RIGHT, false);
		keys.put(Keys.UP, false);
		keys.put(Keys.DOWN, false);
		keys.put(Keys.QUIT, false);
		keys.put(Keys.SPACE, false);
		actionButtons.put(ActionButtons.A_BUTTON_PRESSED, false);
		actionButtons.put(ActionButtons.A_BUTTON_RELEASED, false);
		actionButtons.put(ActionButtons.B_BUTTON_PRESSED, false);
		actionButtons.put(ActionButtons.B_BUTTON_RELEASED, false);
		joystickPosition.x = 0;
		joystickPosition.y = 0;
	}

	@Override
	public void onMobileControlsNotify(Object data, MobileControlEvent event) {
		//Gdx.app.log(TAG, "onMobileControlsNotify " + event.toString());
		if (event == MobileControlEvent.A_BUTTON_PRESSED) {
			Gdx.app.log("tag", "A pressed");
			this.a_Pressed();
		}
		else if (event == MobileControlEvent.A_BUTTON_RELEASED) {
			Gdx.app.log("tag", "A released");
			this.a_Released();
		}
		else if (event == MobileControlEvent.B_BUTTON_PRESSED) {
			Gdx.app.log("tag", "B pressed");
			this.b_Pressed();
		}
		else if (event == MobileControlEvent.B_BUTTON_RELEASED) {
			Gdx.app.log("tag", "B released");
			this.b_Released();
		}
		else if (event == MobileControlEvent.JOYSTICK_POSITION) {
			this.setJoystickPosition((Vector2)data);
		}
	}
}
