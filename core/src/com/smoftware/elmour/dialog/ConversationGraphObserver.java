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

        // NOTE: The SET_ commands are for saving profile information
        SET_C1_CH1_N5,
        NONE
    }

    void onNotify(final ConversationGraph graph, ConversationCommandEvent event);
    void onNotify(final ConversationGraph graph, final ArrayList<ConversationChoice> choices);
    void onNotify(final String value, ConversationCommandEvent event);
}
