package com.smoftware.elmour.components;


public interface Component {

    public static final String MESSAGE_TOKEN = ":::::";

    public static enum MESSAGE{
        CURRENT_POSITION,
        INIT_START_POSITION,
        CURRENT_DIRECTION,
        CURRENT_JOYSTICK_POSITION,
        CURRENT_STATE,
        COLLISION_WITH_MAP,
        COLLISION_WITH_ENTITY,
        INTERACTION_COLLISION,
        NPC_COLLISION,
        LOAD_ANIMATIONS,
        INIT_DIRECTION,
        INIT_STATE,
        INIT_SELECT_ENTITY,
        ENTITY_SELECTED,
        ENTITY_DESELECTED,
        A_BUTTON_STATUS,
        B_BUTTON_STATUS,
        CONVERSATION_STATUS,
        CONVERSATION_ANGLE
    }

    void dispose();
    void receiveMessage(String message);
}
