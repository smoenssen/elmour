package com.smoftware.elmour.dialog;

public class Conversation {
    private String id;
    private String dialog = "";
    private String character = "";
    private String type = "";

    public Conversation(){
    }

    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getDialog(){
        return dialog;
    }
    public void setDialog(String dialog){
        this.dialog = dialog;
    }

    public String getCharacter(){
        return character;
    }
    public void setCharacter(String character){
        this.character = character;
    }

    public String getType() {
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
}
