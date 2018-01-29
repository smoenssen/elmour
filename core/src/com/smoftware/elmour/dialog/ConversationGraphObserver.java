package com.smoftware.elmour.dialog;

import java.util.ArrayList;

public interface ConversationGraphObserver {
    public static enum ConversationCommandEvent {
        LOAD_STORE_INVENTORY,
        EXIT_CONVERSATION,
        NEXT_CONVERSATION_ID,
        CHARACTER_NAME,
        PLAYER_RESPONSE,
        ACCEPT_QUEST,
        ADD_ENTITY_TO_INVENTORY,
        RETURN_QUEST,
        NO_CHOICE,

        WALK_TO_ARMORY,

        WAIT_15000,
        WAIT_10000,
        WAIT_5000,
        WAIT_2000,
        WAIT_1000,

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
    void onNotify(final ConversationGraph graph, ConversationCommandEvent event, String conversationId);
    void onNotify(final ConversationGraph graph, final ArrayList<ConversationChoice> choices);
    void onNotify(final String value, ConversationCommandEvent event);
}
