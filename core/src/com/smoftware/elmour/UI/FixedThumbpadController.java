package com.smoftware.elmour.UI;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by moenssr on 8/30/2017.
 */

public class FixedThumbpadController {
    protected Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin touchpadSkin;
    private Drawable touchBackground;
    private Drawable touchKnob;

    public FixedThumbpadController() {
        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
        touchpadSkin.add("touchBackground", new Texture("controllers/touchpadBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("controllers/touchpadKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");
        touchKnob.setMinWidth(16);
        touchKnob.setMinHeight(16);

        //Apply the Drawables to the TouchPad Style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;

        //Create new TouchPad with the created style
        //srm - first param has to do with knob sensitivity
        touchpad = new Touchpad(3, touchpadStyle);

        //setBounds(x,y,width,height)
        touchpad.setBounds(5, 5, 60, 60);
    }

    public Touchpad getTouchpad() {
        return touchpad;
    }

    public Vector2 getDirection() {
        return new Vector2(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());    }
}
