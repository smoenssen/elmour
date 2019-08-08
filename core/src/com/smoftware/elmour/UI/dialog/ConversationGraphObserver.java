package com.smoftware.elmour.UI.dialog;

import java.util.ArrayList;

public interface ConversationGraphObserver {
    public static enum ConversationCommandEvent {
        LOAD_STORE_INVENTORY,
        EXIT_CHAT,
        EXIT_CUTSCENE,
        EXIT_CONVERSATION_1,
        EXIT_CONVERSATION_2,
        EXIT_CONVERSATION_3,
        EXIT_CONVERSATION_4,
        EXIT_CONVERSATION_5,
        EXIT_CONVERSATION_6,
        NEXT_CONVERSATION_ID,
        CHARACTER_NAME,
        PLAYER_RESPONSE,
        ACCEPT_QUEST,
        DECLINE_QUEST,
        TASK_COMPLETE,
        TASK_COMPLETE_CUTSCENE,
        ADD_ENTITY_TO_INVENTORY,
        RETURN_QUEST,
        NO_CHOICE,
        //Chapter 1
        LOOK_AROUND,
        WALK_TO_MIRROR,
        WALK_BACK,
        CHASE_SEQUENCE,
        GUARDS_SURROUND,
        GUARDS_STOP,
        LOOK_AROUND_ELMOUR,
        THINK,
        STUTTER,
        FAINT,
        LOOK_AROUND_AGAIN,
        ENTER_GUARDS,
        GUARDS_MOVE_FORWARD,
        FORCE_FIELD,
        GUARD_SHAKE,
        CHAR2_NEXT_TO_CHAR1,
        DISMISSED,
        CHAR2_ANGER,
        GUARD_LEAVES,
        GET_CHAR1_NAME,
        GET_CHAR2_NAME,
        CHAR2_TURN_AROUND,
        CHAR2_WALK_RIGHT,
        GO_TO_PORTAL_ROOM,
        CHAR1_LOOK_CHAR2,
        CHAR1_SHOCK,
        CHAR2_LOOK_CHAR1,
        CHAR1_TEAR,
        START_LEAVING,
        STOP_LEAVING,

        //Chapter 2
        //P1
        PAN_TO_ARMORY,
        PAN_TO_INN,
        PAN_TO_WOODSHOP,
        PAN_TO_CHARS,
        CHAR1_LOOK_RIGHT,
        CHAR2_LOOK_LEFT,
        //P2
        WALK_INTO_ARMORY,
        JUSTIN_QUESTION,
        CHAR2_LOOK_DOWN,
        CHAR2_LOOK_UP,
        JAXON_LOOK_DOWN,
        CHAR2_LOOK_RIGHT,
        CHAR2_LOOK_UP_WAIT,
        JUSTIN_QUESTION2,
        START_WALKING,
        OPEN_ARMORY_DOOR,
        WALK_TO_DOOR,
        WALK_SWORD,
        WALK_MACE,
        WALK_STAFF,
        WALK_DAGGER,
        CHAR1_LOOK_DOWN,
        CHAR1_QUESTION,
        WALK_TO_BOOK,
        TAKE_BOOK,
        WALK_TO_CHAR2,
        ZOOM_IN,
        ZOOM_OUT,
        WALK_OUT_OF_WAY,
        //P3
        CHAR1_STUTTER,
        CHAR1_THINK,
        WALK_TO_WOODSHOP,
        GET_BACKPACKS,
        GET_FIREWOOD,
        //P4
        WALK_INTO_INN,
        DIANE_WALK,
        DIANE_WALK_BACK,
        START_LEAVE_INN,
        CHAR2_LOOK_UP_2,
        //P5
        CHAR2_LOOK_DOWN_2,
        CHAR2_TO_CASTLE,
        CHAR1_LOOK_AT_BOOK,
        CHAR2_RETURNS,

        //Chapter 3
        //P1
        CHAR1_BOOK,
        CHAR2_STAND_BACK,
        THUNDER,
        RAT_GETS_UP,
        RAT_ATTACK,


        //Quests
        //Cloning Quest
        RICK_FAILURE,
        RICK_WALK_TO_COMP,
        RICK_THINK,
        RICK_WALK_AROUND,
        RICK_SPOT_CHAR,
        RICK_LOOK_AWAY,
        RICK_LOOK_DOWN,
        RICK_WALK_DOWN,
        RICK_DESTROY_KEYBOARD,

        JAXON_LOOK_LEFT,


        WALK_AROUND_TOWN,

        WAIT_15000,
        WAIT_10000,
        WAIT_5000,
        WAIT_2000,
        WAIT_1000,
        WAIT_500,

        ALT_TEXT_1,
        ALT_TEXT_2,

        INTERRUPT,
        SET_THROWBOOK,

        // NOTE: The SET_ commands are for saving profile information
        SET_C1_CH1_N5,
        SET_C2_CH1,
        SET_C2_CH2,
        SET_C2_CH3,
        SET_CH2PATH_PLUS_1,
        SET_WEAPON_EQUALS_1,
        SET_WEAPON_EQUALS_2,
        SET_WEAPON_EQUALS_3,
        NONE
    }

    void onNotify(final ConversationGraph graph, ConversationCommandEvent event);
    void onNotify(final ConversationGraph graph, ConversationCommandEvent event, String data);
    void onNotify(final ConversationGraph graph, final ArrayList<ConversationChoice> choices);
    void onNotify(final String value, ConversationCommandEvent event);
}
