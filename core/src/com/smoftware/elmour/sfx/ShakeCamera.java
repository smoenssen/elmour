package com.smoftware.elmour.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class ShakeCamera {
	private static final String TAG = ShakeCamera.class.getSimpleName();

	private enum ShakeState { START, MAX, MID, END }

	private boolean _isShaking = false;
	private float _origShakeRadius = 30.0f;
	private float _randomAngle;
	private Vector2 _offset;
	private Vector2 _currentPosition;
	private Vector2 _origPosition;

	private ShakeState shakeState = ShakeState.START;

	private float shakeRadius = 0.2f;
	private float maxShakeRadius = 0.2f;
	private float midShakeRadius = 0.15f;
	private float endShakeRadius = 0.025f;
	private float percentIncreaseToMax = 0;
	private float percentDecreaseToMid = 0.97f;
	private float percentDecreaseToEnd = 0.985f;

	
	public ShakeCamera(float x, float y, float shakeRadius){
		this._origPosition = new Vector2(x,y);
		this.shakeRadius = shakeRadius;
		this._origShakeRadius = shakeRadius;
		this._offset = new Vector2();
		this._currentPosition = new Vector2();
		reset();
	}

	public ShakeCamera(float x, float y,
					   float startShakeRadius,
					   float maxShakeRadius,
					   float midShakeRadius,
					   float endShakeRadius,
					   float percentIncreaseToMax,
					   float percentDecreaseToMid,
					   float percentDecreaseToEnd) {

		this._origPosition = new Vector2(x,y);
		this.shakeRadius = startShakeRadius;
		this._origShakeRadius = startShakeRadius;
		this.maxShakeRadius = maxShakeRadius;
		this.midShakeRadius = midShakeRadius;
		this.endShakeRadius = endShakeRadius;
		this.percentIncreaseToMax = percentIncreaseToMax;
		this.percentDecreaseToMid = percentDecreaseToMid;
		this.percentDecreaseToEnd = percentDecreaseToEnd;
		this._offset = new Vector2();
		this._currentPosition = new Vector2();
		reset();
	}

	public void setOrigPosition(float x, float y){
		this._origPosition.set(x,y);
	}
	
	public boolean isCameraShaking(){
		return _isShaking;
	}
	
	public void startShaking(){
		_isShaking = true;
	}
	
	private void seedRandomAngle(){
		_randomAngle = MathUtils.random(1, 360);
	}
	
	private void computeCameraOffset(){
		float sine = MathUtils.sinDeg(_randomAngle);
		float cosine = MathUtils.cosDeg(_randomAngle);

		//Gdx.app.debug(TAG, "Sine of " + _randomAngle + " is: " + sine);
		//Gdx.app.debug(TAG, "Cosine of " + _randomAngle + " is: " + cosine);

		_offset.x =  cosine * shakeRadius;
		_offset.y =  sine * shakeRadius;

		//Gdx.app.debug(TAG, "Offset is x:" + _offset.x + " , y: " + _offset.y );
	}
	
	private void computeCurrentPosition(){
		_currentPosition.x = _origPosition.x + _offset.x;
		_currentPosition.y = _origPosition.y + _offset.y;

		//Gdx.app.debug(TAG, "Current position is x:" + _currentPosition.x + " , y: " + _currentPosition.y );
	}

	// startShakeRadius
	// maxShakeRadius
	// midShakeRadius
	// endShakeRadius
	// percentIncreaseToMax
	// percentDecreaseToMid
	// percentDecreaseToEnd
	private void decreaseShake(){
		//Gdx.app.debug(TAG, "Current shakeRadius is: " + _shakeRadius + " randomAngle is: " + _randomAngle);

		if( shakeRadius < endShakeRadius ){
			//Gdx.app.debug(TAG, "Done shaking");
			reset();
			return;
		}
		
		_isShaking = true;

		if (shakeRadius < midShakeRadius) {
			shakeState = ShakeState.END;
			shakeRadius *= percentDecreaseToEnd;
		}
		else {
			shakeState = ShakeState.MID;
			shakeRadius *= percentDecreaseToMid;
		}
		//Gdx.app.debug(TAG, "New shakeRadius is: " + _shakeRadius);


		_randomAngle = MathUtils.random(1, 360);
		//Gdx.app.debug(TAG, "New random angle: " + _randomAngle);
	}

	private void increaseShake() {
		if (shakeRadius < maxShakeRadius) {
			shakeRadius /= percentIncreaseToMax;
		}
		else {
			shakeState = ShakeState.MAX;
		}

		_randomAngle = MathUtils.random(1, 360);
	}

	public void reset(){
		shakeRadius = _origShakeRadius;
		_isShaking = false;
		seedRandomAngle();
		_currentPosition.x = _origPosition.x;
		_currentPosition.y = _origPosition.y;
		shakeState = ShakeState.START;
	}
	
	public Vector2 getNewShakePosition(){
		computeCameraOffset();
		computeCurrentPosition();

		//Gdx.app.log(TAG, "shakeState = " + shakeState.toString() + String.format(", shakeRadius = %3.3f",  shakeRadius));

		switch (shakeState) {
			case START:
				// increase radius until max radius is reached
				increaseShake();
				break;
			case MAX:
			case MID:
			case END:
				// decrease radius
				decreaseShake();
				break;
		}

		return _currentPosition;
	}
}
